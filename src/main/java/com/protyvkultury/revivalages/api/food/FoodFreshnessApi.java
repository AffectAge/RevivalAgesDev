package com.protyvkultury.revivalages.api.food;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Public, server-authoritative access to food freshness.
 */
public final class FoodFreshnessApi {

    private static Runtime runtime = Runtime.DISABLED;

    private FoodFreshnessApi() {
    }

    public static boolean enabled() {
        return runtime.enabled();
    }

    public static long now() {
        return runtime.now();
    }

    public static Optional<FoodState> state(ItemStack stack) {
        return runtime.state(stack);
    }

    public static Optional<FoodSpoilageProfile> profile(ItemStack stack) {
        return Optional.ofNullable(stack.getItemHolder().getData(FoodSpoilageDataMaps.ITEM_SPOILAGE));
    }

    public static long lifetime(ItemStack stack) {
        return runtime.lifetime(stack);
    }

    public static long remaining(ItemStack stack) {
        return runtime.remaining(stack);
    }

    public static boolean expired(ItemStack stack) {
        return runtime.expired(stack);
    }

    public static ItemStack initialize(ItemStack stack) {
        return runtime.initialize(stack);
    }

    public static ItemStack materialize(ItemStack stack) {
        return runtime.materialize(stack);
    }

    public static boolean materializeAll(List<ItemStack> stacks) {
        boolean changed = false;
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack before = stacks.get(slot);
            ItemStack after = materialize(before);
            if (after != before) {
                stacks.set(slot, after);
                changed = true;
            }
        }
        return changed;
    }

    public static ItemStack copyOldest(ItemStack output, List<ItemStack> inputs) {
        return runtime.copyOldest(output, inputs);
    }

    public static ItemStack transformOutput(
            ItemStack output,
            List<ItemStack> inputs,
            ResourceLocation recipeId
    ) {
        return runtime.transformOutput(output, inputs, recipeId);
    }

    public static ItemStack applyTrait(ItemStack stack, ResourceLocation trait) {
        return runtime.applyTrait(stack, trait);
    }

    public static ItemStack removeTrait(ItemStack stack, ResourceLocation trait) {
        return runtime.removeTrait(stack, trait);
    }

    public static void install(Runtime installed) {
        runtime = installed == null ? Runtime.DISABLED : installed;
    }

    public interface Runtime {

        Runtime DISABLED = new Runtime() {
            @Override
            public boolean enabled() {
                return false;
            }

            @Override
            public long now() {
                return 0L;
            }

            @Override
            public Optional<FoodState> state(ItemStack stack) {
                return Optional.empty();
            }

            @Override
            public long lifetime(ItemStack stack) {
                return Long.MAX_VALUE;
            }

            @Override
            public long remaining(ItemStack stack) {
                return Long.MAX_VALUE;
            }

            @Override
            public boolean expired(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack initialize(ItemStack stack) {
                return stack;
            }

            @Override
            public ItemStack materialize(ItemStack stack) {
                return stack;
            }

            @Override
            public ItemStack copyOldest(ItemStack output, List<ItemStack> inputs) {
                return output;
            }

            @Override
            public ItemStack transformOutput(ItemStack output, List<ItemStack> inputs, ResourceLocation recipeId) {
                return output;
            }

            @Override
            public ItemStack applyTrait(ItemStack stack, ResourceLocation trait) {
                return stack;
            }

            @Override
            public ItemStack removeTrait(ItemStack stack, ResourceLocation trait) {
                return stack;
            }
        };

        boolean enabled();

        long now();

        Optional<FoodState> state(ItemStack stack);

        long lifetime(ItemStack stack);

        long remaining(ItemStack stack);

        boolean expired(ItemStack stack);

        ItemStack initialize(ItemStack stack);

        ItemStack materialize(ItemStack stack);

        ItemStack copyOldest(ItemStack output, List<ItemStack> inputs);

        ItemStack transformOutput(ItemStack output, List<ItemStack> inputs, ResourceLocation recipeId);

        ItemStack applyTrait(ItemStack stack, ResourceLocation trait);

        ItemStack removeTrait(ItemStack stack, ResourceLocation trait);
    }
}
