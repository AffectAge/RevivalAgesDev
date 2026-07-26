package com.protyvkultury.revivalages.integration.kubejs;

import com.protyvkultury.revivalages.api.weight.WeightApi;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Read-only script boundary for canonical Carried Weight queries.
 */
public enum CarriedWeightScriptBindings {
    INSTANCE;

    public double weightOf(ItemStack stack) {
        return WeightApi.getWeight(stack).weight();
    }

    public double weightOf(ItemStack stack, Player player) {
        return WeightApi.getWeight(stack, player).weight();
    }

    public double currentWeight(Player player) {
        return WeightApi.getCurrentWeight(player);
    }

    public double capacity(ServerPlayer player) {
        return WeightApi.getCapacity(player);
    }

    public int pockets(ItemStack stack, Player wearer) {
        return WeightApi.getPockets(stack, wearer).orElse(0);
    }
}
