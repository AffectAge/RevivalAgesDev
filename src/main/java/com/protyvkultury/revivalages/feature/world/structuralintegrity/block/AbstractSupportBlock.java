package com.protyvkultury.revivalages.feature.world.structuralintegrity.block;

import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityConfig;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityTags;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractSupportBlock extends Block implements SimpleWaterloggedBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final Map<Direction, BooleanProperty> CONNECTIONS = new EnumMap<>(Direction.class);

    static {
        CONNECTIONS.put(Direction.NORTH, NORTH);
        CONNECTIONS.put(Direction.EAST, EAST);
        CONNECTIONS.put(Direction.SOUTH, SOUTH);
        CONNECTIONS.put(Direction.WEST, WEST);
    }

    protected AbstractSupportBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(WATERLOGGED, false));
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
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (direction.getAxis().isHorizontal()) {
            state = state.setValue(CONNECTIONS.get(direction), neighborState.is(StructuralIntegrityTags.SUPPORT_BEAMS));
        }
        return state;
    }

    protected BlockState connectedState(LevelAccessor level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            state = state.setValue(
                    CONNECTIONS.get(direction),
                    level.getBlockState(pos.relative(direction)).is(StructuralIntegrityTags.SUPPORT_BEAMS)
            );
        }
        return state;
    }

    protected VoxelShape shape(BlockState state, boolean fullHeight) {
        double minimumY = fullHeight ? 0D : 10D;
        VoxelShape result = box(5D, minimumY, 5D, 11D, 16D, 11D);
        if (state.getValue(NORTH)) {
            result = Shapes.or(result, box(5D, 10D, 0D, 11D, 16D, 5D));
        }
        if (state.getValue(SOUTH)) {
            result = Shapes.or(result, box(5D, 10D, 11D, 11D, 16D, 16D));
        }
        if (state.getValue(EAST)) {
            result = Shapes.or(result, box(11D, 10D, 5D, 16D, 16D, 11D));
        }
        if (state.getValue(WEST)) {
            result = Shapes.or(result, box(0D, 10D, 5D, 5D, 16D, 11D));
        }
        return result;
    }

    protected boolean supportEnabled() {
        return StructuralIntegrityConfig.supportBeamsEnabled();
    }

    @Override
    protected abstract boolean canSurvive(BlockState state, LevelReader level, BlockPos pos);

    @Override
    protected abstract VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    );

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, WATERLOGGED);
    }
}
