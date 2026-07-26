package com.protyvkultury.revivalages.api.size;

import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Stable query boundary for item-size-aware features and optional integrations.
 */
public final class SizeApi {

    private static volatile RuntimePolicy runtimePolicy = RuntimePolicy.DEFAULT;

    private SizeApi() {
    }

    /**
     * Resolves a stack size using providers, synchronized data maps, then stable
     * vanilla-type fallbacks.
     */
    public static Size getSize(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemSizeProvider provider) {
            return provider.getSize(stack);
        }
        if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ItemSizeProvider provider) {
            return provider.getSize(stack);
        }
        Size dataSize = BuiltInRegistries.ITEM.wrapAsHolder(item).getData(ItemSizeDataMaps.ITEM_SIZE);
        if (dataSize != null) {
            return dataSize;
        }
        if (item instanceof TieredItem || item instanceof BucketItem) {
            return Size.LARGE;
        }
        if (item instanceof ArmorItem || item instanceof AnimalArmorItem) {
            return Size.LARGE;
        }
        return item instanceof BlockItem ? Size.SMALL : Size.VERY_SMALL;
    }

    /**
     * Returns whether the stack may be inserted into a data-map-enabled block
     * container. A missing policy is unrestricted.
     */
    public static boolean canInsert(BlockState container, ItemStack stack) {
        RuntimePolicy policyResolver = runtimePolicy;
        if (!policyResolver.enabled()) {
            return true;
        }
        ContainerSizePolicy policy = blockPolicy(container);
        return policy == null || getSize(stack).isEqualOrSmallerThan(
                policyResolver.maximumForBlock(container.getBlock(), policy.maxSize())
        );
    }

    /**
     * Returns whether the stack may be inserted into a data-map-enabled item
     * container. A missing policy is unrestricted.
     */
    public static boolean canInsert(ItemStack container, ItemStack stack) {
        RuntimePolicy policyResolver = runtimePolicy;
        if (!policyResolver.enabled()) {
            return true;
        }
        ContainerSizePolicy policy = itemPolicy(container);
        return policy == null || getSize(stack).isEqualOrSmallerThan(
                policyResolver.maximumForItem(container.getItem(), policy.maxSize())
        );
    }

    /**
     * Returns the configured count for a size-aware placed-item input, bounded by
     * that inventory's absolute implementation limit.
     */
    public static int maximumPlacedInputCount(ItemStack stack, int absoluteMaximum) {
        RuntimePolicy policyResolver = runtimePolicy;
        if (!policyResolver.enabled()) {
            return absoluteMaximum;
        }
        return Math.min(absoluteMaximum, policyResolver.maximumPlacedInputCount(getSize(stack)));
    }

    public static boolean enabled() {
        return runtimePolicy.enabled();
    }

    public static Size effectiveMaximum(Block block, Size dataDefault) {
        return runtimePolicy.maximumForBlock(block, dataDefault);
    }

    public static Size effectiveMaximum(Item item, Size dataDefault) {
        return runtimePolicy.maximumForItem(item, dataDefault);
    }

    /**
     * Installs the synchronized server policy used by common API checks.
     * Intended for the Revival Ages Item Size module.
     */
    public static void installRuntimePolicy(RuntimePolicy policy) {
        runtimePolicy = Objects.requireNonNull(policy);
    }

    @Nullable
    public static ContainerSizePolicy blockPolicy(BlockState container) {
        return BuiltInRegistries.BLOCK.wrapAsHolder(container.getBlock())
                .getData(ItemSizeDataMaps.BLOCK_CONTAINER);
    }

    @Nullable
    public static ContainerSizePolicy itemPolicy(ItemStack container) {
        return BuiltInRegistries.ITEM.wrapAsHolder(container.getItem())
                .getData(ItemSizeDataMaps.ITEM_CONTAINER);
    }

    public interface RuntimePolicy {

        RuntimePolicy DEFAULT = new RuntimePolicy() {
            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public Size maximumForBlock(Block block, Size dataDefault) {
                return dataDefault;
            }

            @Override
            public Size maximumForItem(Item item, Size dataDefault) {
                return dataDefault;
            }

            @Override
            public int maximumPlacedInputCount(Size size) {
                return Integer.MAX_VALUE;
            }
        };

        boolean enabled();

        Size maximumForBlock(Block block, Size dataDefault);

        Size maximumForItem(Item item, Size dataDefault);

        int maximumPlacedInputCount(Size size);
    }
}
