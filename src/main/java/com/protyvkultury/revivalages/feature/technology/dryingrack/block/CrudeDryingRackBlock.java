package com.protyvkultury.revivalages.feature.technology.dryingrack.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.feature.technology.dryingrack.blockentity.DryingRackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CrudeDryingRackBlock extends AbstractDryingRackBlock {

    public static final MapCodec<CrudeDryingRackBlock> CODEC = simpleCodec(CrudeDryingRackBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape NORTH_SHAPE = Block.box(0.0D, 11.0D, 0.0D, 16.0D, 16.0D, 5.0D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0.0D, 11.0D, 11.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape EAST_SHAPE = Block.box(11.0D, 11.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 11.0D, 0.0D, 5.0D, 16.0D, 16.0D);

    public CrudeDryingRackBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected MapCodec<CrudeDryingRackBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        Direction facing = clickedFace.getAxis().isHorizontal()
                ? clickedFace.getOpposite()
                : context.getHorizontalDirection();
        BlockState state = defaultBlockState().setValue(FACING, facing);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing);
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing.getOpposite());
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
        return direction == state.getValue(FACING) && !state.canSurvive(level, pos)
                ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> Shapes.block();
        };
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return DryingRackBlockEntity.createCrude(pos, state);
    }

    @Override
    protected int slotFromHit(BlockState state, BlockHitResult hitResult) {
        return 0;
    }
}
