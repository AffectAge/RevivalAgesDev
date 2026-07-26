package com.protyvkultury.revivalages.api.diet;

import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Public access point for diet values and item contributions.
 */
public final class DietApi {

    private static Runtime runtime = Runtime.DISABLED;

    private DietApi() {
    }

    public static boolean enabled() {
        return runtime.enabled();
    }

    public static Optional<DietContribution> contribution(ItemStack stack) {
        return Optional.ofNullable(stack.getItemHolder().getData(DietDataMaps.ITEM_DIET));
    }

    public static Map<ResourceLocation, Double> values(Player player) {
        return runtime.values(player);
    }

    public static void set(Player player, ResourceLocation group, double value) {
        runtime.set(player, group, value);
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
            public Map<ResourceLocation, Double> values(Player player) {
                return Map.of();
            }

            @Override
            public void set(Player player, ResourceLocation group, double value) {
            }
        };

        boolean enabled();

        Map<ResourceLocation, Double> values(Player player);

        void set(Player player, ResourceLocation group, double value);
    }

}
