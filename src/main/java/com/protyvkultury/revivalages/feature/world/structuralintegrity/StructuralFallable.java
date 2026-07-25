package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;

/**
 * Optional callback for blocks that need to react after structural falling has
 * finished and the resulting state has been placed.
 */
public interface StructuralFallable {

    void onStructuralFallFinished(Level level, BlockPos pos, FallingBlockEntity fallingBlock);
}
