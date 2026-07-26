package com.protyvkultury.revivalages.api.weight;

import net.minecraft.world.entity.player.Player;

/**
 * Contributes carried stacks or another bounded weight source for a player.
 */
@FunctionalInterface
public interface PlayerWeightSource {

    WeightResult getWeight(Player player, WeightContext context, WeightLookup lookup);
}
