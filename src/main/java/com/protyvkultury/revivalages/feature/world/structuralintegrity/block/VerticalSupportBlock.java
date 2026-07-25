package com.protyvkultury.revivalages.feature.world.structuralintegrity.block;

import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityConfig;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class VerticalSupportBlock extends AbstractSupportBlock {

    public VerticalSupportBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!supportEnabled()) {
            return null;
        }
        BlockState state = connectedState(context.getLevel(), context.getClickedPos(), defaultBlockState());
        return state.setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide || placer == null || placer.isShiftKeyDown() || !supportEnabled()) {
            return;
        }
        int maximum = Math.min(StructuralIntegrityConfig.VERTICAL_AUTO_STACK.get(), stack.getCount());
        int placeCount = 1;
        for (int offset = 1; offset < maximum; offset++) {
            BlockPos target = pos.above(offset);
            BlockState replaced = level.getBlockState(target);
            if (!canAutoReplace(replaced) || !level.getEntities(null, new AABB(target)).isEmpty()) {
                break;
            }
            placeCount++;
        }
        for (int offset = 1; offset < placeCount; offset++) {
            BlockPos target = pos.above(offset);
            boolean waterlogged = level.getFluidState(target).is(Fluids.WATER);
            level.setBlock(target, defaultBlockState().setValue(WATERLOGGED, waterlogged), Block.UPDATE_ALL);
        }
        if (!(placer instanceof net.minecraft.world.entity.player.Player player) || !player.isCreative()) {
            stack.shrink(placeCount - 1);
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!supportEnabled()) {
            return true;
        }
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return belowState.is(StructuralIntegrityTags.SUPPORT_BEAMS)
                || belowState.isFaceSturdy(
                        level,
                        below,
                        net.minecraft.core.Direction.UP,
                        SupportType.CENTER
                );
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (supportEnabled() && direction == Direction.DOWN && !canSurvive(updated, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return updated;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state, true);
    }

    private static boolean canAutoReplace(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER);
    }
}
