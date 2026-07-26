package com.protyvkultury.revivalages.api.weight;

import java.util.Objects;
import java.util.OptionalInt;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Stable query boundary for carried-weight-aware features and integrations.
 */
public final class WeightApi {

    private static volatile Runtime runtime = Runtime.DISABLED;

    private WeightApi() {
    }

    public static WeightResult getWeight(ItemStack stack) {
        return runtime.getWeight(stack, null);
    }

    public static WeightResult getWeight(ItemStack stack, Player player) {
        return runtime.getWeight(stack, player);
    }

    public static WeightResult getCarriedWeight(Player player) {
        return runtime.getCarriedWeight(player);
    }

    public static double getCapacity(ServerPlayer player) {
        return runtime.getCapacity(player);
    }

    public static OptionalInt getPockets(ItemStack stack, Player wearer) {
        return runtime.getPockets(stack, wearer);
    }

    public static double getCurrentWeight(Player player) {
        return runtime.getCurrentWeight(player);
    }

    public static double getCurrentCapacity(Player player) {
        return runtime.getCurrentCapacity(player);
    }

    public static boolean isOverloaded(Player player) {
        return runtime.isOverloaded(player);
    }

    public static boolean enabled() {
        return runtime.enabled();
    }

    /**
     * Installs the feature-owned runtime implementation.
     */
    public static void installRuntime(Runtime newRuntime) {
        runtime = Objects.requireNonNull(newRuntime);
    }

    public interface Runtime {

        Runtime DISABLED = new Runtime() {
            @Override
            public WeightResult getWeight(ItemStack stack, Player player) {
                return WeightResult.ZERO;
            }

            @Override
            public WeightResult getCarriedWeight(Player player) {
                return WeightResult.ZERO;
            }

            @Override
            public double getCapacity(ServerPlayer player) {
                return 0.0D;
            }

            @Override
            public OptionalInt getPockets(ItemStack stack, Player wearer) {
                return OptionalInt.empty();
            }

            @Override
            public double getCurrentWeight(Player player) {
                return 0.0D;
            }

            @Override
            public double getCurrentCapacity(Player player) {
                return 0.0D;
            }

            @Override
            public boolean isOverloaded(Player player) {
                return false;
            }

            @Override
            public boolean enabled() {
                return false;
            }
        };

        WeightResult getWeight(ItemStack stack, Player player);

        WeightResult getCarriedWeight(Player player);

        double getCapacity(ServerPlayer player);

        OptionalInt getPockets(ItemStack stack, Player wearer);

        double getCurrentWeight(Player player);

        double getCurrentCapacity(Player player);

        boolean isOverloaded(Player player);

        boolean enabled();
    }
}
