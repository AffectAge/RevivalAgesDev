package com.protyvkultury.revivalages.feature.inventory.itemsize;

import com.protyvkultury.revivalages.api.size.Size;
import com.protyvkultury.revivalages.api.size.SizeApi;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

/**
 * Synchronized effective Item Size settings. The remote snapshot is cleared when
 * a physical client leaves a server.
 */
public final class ItemSizeSettings {

    private static volatile Snapshot local = Snapshot.defaults();
    private static volatile Snapshot remote;

    private ItemSizeSettings() {
    }

    public static boolean enabled() {
        return current().enabled();
    }

    public static Snapshot snapshot() {
        return current();
    }

    public static Snapshot refreshLocal() {
        Snapshot refreshed = fromConfig();
        local = refreshed;
        if (remote == null) {
            SizeApi.installRuntimePolicy(refreshed);
        }
        return refreshed;
    }

    public static void acceptRemote(Snapshot snapshot) {
        remote = snapshot;
        SizeApi.installRuntimePolicy(snapshot);
    }

    public static void clearRemote() {
        remote = null;
        SizeApi.installRuntimePolicy(local);
    }

    @Nullable
    static ParsedOverride parseOverride(String text) {
        int separator = text.indexOf('=');
        int typeSeparator = text.indexOf('|');
        if (typeSeparator <= 0 || separator <= typeSeparator + 1 || separator == text.length() - 1) {
            return null;
        }
        String type = text.substring(0, typeSeparator).trim().toLowerCase(Locale.ROOT);
        ResourceLocation id = ResourceLocation.tryParse(text.substring(typeSeparator + 1, separator).trim());
        Size size;
        try {
            size = Size.valueOf(text.substring(separator + 1).trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        if (id == null || (!type.equals("block") && !type.equals("item"))) {
            return null;
        }
        return new ParsedOverride(type.equals("block"), id, size);
    }

    public static boolean isValidOverride(String text) {
        return parseOverride(text) != null;
    }

    private static Snapshot current() {
        Snapshot remoteSnapshot = remote;
        return remoteSnapshot == null ? local : remoteSnapshot;
    }

    private static Snapshot fromConfig() {
        Map<ResourceLocation, Size> blockOverrides = new LinkedHashMap<>();
        Map<ResourceLocation, Size> itemOverrides = new LinkedHashMap<>();
        List<? extends String> configured = ItemSizeConfig.SPEC.isLoaded()
                ? ItemSizeConfig.CONTAINER_OVERRIDES.get()
                : ItemSizeConfig.CONTAINER_OVERRIDES.getDefault();
        for (String entry : configured) {
            ParsedOverride parsed = parseOverride(entry);
            if (parsed != null) {
                (parsed.block() ? blockOverrides : itemOverrides).put(parsed.id(), parsed.size());
            }
        }
        return new Snapshot(
                ContentAvailability.isEnabled(ContentKey.ITEM_SIZE),
                value(ItemSizeConfig.CHEST_MAXIMUM_SIZE),
                value(ItemSizeConfig.BUNDLE_MAXIMUM_SIZE),
                value(ItemSizeConfig.PIT_KILN_BATCHABLE_MAXIMUM_SIZE),
                value(ItemSizeConfig.PIT_KILN_BATCH_SIZE),
                value(ItemSizeConfig.PIT_KILN_OVERSIZED_BATCH_SIZE),
                blockOverrides,
                itemOverrides
        );
    }

    private static <T> T value(ModConfigSpec.ConfigValue<T> value) {
        return ItemSizeConfig.SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    public record Snapshot(
            boolean enabled,
            Size chestMaximumSize,
            Size bundleMaximumSize,
            Size pitKilnBatchableMaximumSize,
            int pitKilnBatchSize,
            int pitKilnOversizedBatchSize,
            Map<ResourceLocation, Size> blockOverrides,
            Map<ResourceLocation, Size> itemOverrides
    ) implements SizeApi.RuntimePolicy {

        public Snapshot {
            blockOverrides = Map.copyOf(blockOverrides);
            itemOverrides = Map.copyOf(itemOverrides);
        }

        public static Snapshot defaults() {
            return new Snapshot(
                    true,
                    Size.LARGE,
                    Size.NORMAL,
                    Size.LARGE,
                    4,
                    1,
                    Map.of(),
                    Map.of()
            );
        }

        @Override
        public Size maximumForBlock(Block block, Size dataDefault) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            Size override = blockOverrides.get(id);
            if (override != null) {
                return override;
            }
            return block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST ? chestMaximumSize : dataDefault;
        }

        @Override
        public Size maximumForItem(Item item, Size dataDefault) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            Size override = itemOverrides.get(id);
            if (override != null) {
                return override;
            }
            return item == Items.BUNDLE ? bundleMaximumSize : dataDefault;
        }

        @Override
        public int maximumPlacedInputCount(Size size) {
            return size.isEqualOrSmallerThan(pitKilnBatchableMaximumSize)
                    ? pitKilnBatchSize
                    : pitKilnOversizedBatchSize;
        }
    }

    record ParsedOverride(boolean block, ResourceLocation id, Size size) {
    }

}
