package com.protyvkultury.revivalages.api.ignition;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared directional fire-source query for mechanics affected by nearby heat.
 */
public final class FireSourceApi {

    private FireSourceApi() {
    }

    public static boolean isSourceFacing(LevelReader level, BlockPos sourcePos, BlockPos targetPos) {
        BlockState state = level.getBlockState(sourcePos);
        if (state.is(BlockTags.FIRE)) {
            return true;
        }
        int x = Integer.compare(targetPos.getX(), sourcePos.getX());
        int y = Integer.compare(targetPos.getY(), sourcePos.getY());
        int z = Integer.compare(targetPos.getZ(), sourcePos.getZ());
        if (x == 0 && y == 0 && z == 0) {
            for (Direction direction : Direction.values()) {
                if (state.isFireSource(level, sourcePos, direction)) {
                    return true;
                }
            }
            return false;
        }
        return state.isFireSource(level, sourcePos, Direction.getNearest(x, y, z));
    }
}
