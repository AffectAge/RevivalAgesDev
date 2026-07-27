package com.protyvkultury.revivalages.api.ignition;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A block target supported by a held igniter.
 *
 * <p>The returned value controls whether the completed held-use consumes one
 * igniter use and produces feedback. Implementations may return {@code true}
 * even when their internal activation is a no-op when that is part of their
 * interaction contract.</p>
 */
public interface HeldIgnitableBlock {

    boolean igniteFromHeldItem(
            Level level,
            BlockPos pos,
            BlockState state,
            Player player,
            Direction clickedFace
    );
}
