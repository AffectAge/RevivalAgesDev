package com.protyvkultury.revivalages.feature.world.structuralintegrity.item;

import net.minecraft.core.Direction;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Exposes the actual standing-or-wall placement choice to the client preview. */
public final class SupportBeamItem extends StandingAndWallBlockItem {

    public SupportBeamItem(Block vertical, Block horizontal, Properties properties) {
        super(vertical, horizontal, properties, Direction.DOWN);
    }

    @Nullable
    public BlockState previewState(BlockPlaceContext context) {
        if (!context.canPlace()) {
            return null;
        }
        BlockState state = getPlacementState(context);
        return state != null && canPlace(context, state) ? state : null;
    }
}
