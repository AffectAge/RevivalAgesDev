package com.protyvkultury.revivalages.feature.world.structuralintegrity.block;

import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityConfig;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityTags;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class HorizontalSupportBlock extends AbstractSupportBlock {

    public HorizontalSupportBlock(Properties properties) {
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
        if (level.isClientSide || placer == null || !supportEnabled()) {
            return;
        }
        Span span = findSpan(level, pos);
        if (span == null || stack.getCount() < span.positions().size()) {
            level.destroyBlock(pos, true);
            return;
        }
        for (BlockPos target : span.positions()) {
            if (target.equals(pos)) {
                continue;
            }
            BlockState existing = level.getBlockState(target);
            if (!canAutoReplace(existing) || !level.getEntities(null, new AABB(target)).isEmpty()) {
                level.destroyBlock(pos, true);
                return;
            }
        }
        for (BlockPos target : span.positions()) {
            boolean waterlogged = level.getFluidState(target).is(Fluids.WATER);
            BlockState placed = connectedState(
                    level,
                    target,
                    defaultBlockState().setValue(WATERLOGGED, waterlogged)
            );
            level.setBlock(target, placed, Block.UPDATE_CLIENTS);
        }
        for (BlockPos target : span.positions()) {
            level.updateNeighborsAt(target, this);
        }
        if (!(placer instanceof net.minecraft.world.entity.player.Player player) || !player.isCreative()) {
            stack.shrink(span.positions().size() - 1);
        }
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            net.minecraft.world.level.LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (supportEnabled() && direction.getAxis().isHorizontal() && !hasCompleteAxis(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return updated;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return !supportEnabled() || findSpan(level, pos) != null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state, false);
    }

    private boolean hasCompleteAxis(LevelReader level, BlockPos pos) {
        return connectedInBothDirections(level, pos, Direction.NORTH, Direction.SOUTH)
                || connectedInBothDirections(level, pos, Direction.EAST, Direction.WEST);
    }

    private boolean connectedInBothDirections(LevelReader level, BlockPos pos, Direction first, Direction second) {
        return findEndpoint(level, pos, first) != null && findEndpoint(level, pos, second) != null;
    }

    @Nullable
    private Span findSpan(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = pos.relative(direction.getOpposite());
            if (!level.getBlockState(adjacent).is(StructuralIntegrityTags.SUPPORT_BEAMS)) {
                continue;
            }
            BlockPos endpoint = findEndpoint(level, pos, direction);
            if (endpoint == null) {
                continue;
            }
            List<BlockPos> positions = new ArrayList<>();
            BlockPos cursor = pos;
            while (!cursor.equals(endpoint)) {
                positions.add(cursor);
                cursor = cursor.relative(direction);
            }
            return new Span(List.copyOf(positions));
        }
        return null;
    }

    @Nullable
    private BlockPos findEndpoint(LevelReader level, BlockPos pos, Direction direction) {
        int maximum = StructuralIntegrityConfig.HORIZONTAL_MAX_SPAN.get();
        for (int distance = 1; distance <= maximum; distance++) {
            BlockPos cursor = pos.relative(direction, distance);
            BlockState state = level.getBlockState(cursor);
            if (state.is(StructuralIntegrityTags.SUPPORT_BEAMS)) {
                return cursor;
            }
            if (!canAutoReplace(state)) {
                return null;
            }
        }
        return null;
    }

    private record Span(List<BlockPos> positions) {
    }

    private static boolean canAutoReplace(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER);
    }
}
