package com.protyvkultury.revivalages.feature.technology.tanningrack.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import com.protyvkultury.revivalages.feature.technology.tanningrack.TanningRackFeature;
import com.protyvkultury.revivalages.feature.technology.tanningrack.blockentity.TanningRackBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class TanningRackBlock extends BaseEntityBlock {

    public static final MapCodec<TanningRackBlock> CODEC = simpleCodec(TanningRackBlock::new);
    private static final VoxelShape NORTH_SOUTH = box(0, 0, 2, 16, 17, 12);
    private static final VoxelShape EAST_WEST = box(4, 0, 0, 14, 17, 16);
    private static final VoxelShape NORTH_SOUTH_COLLISION = Shapes.or(
            box(0, 0, 10, 2, 16, 12),
            box(14, 0, 10, 16, 16, 12),
            box(2, 0, 3, 3, 17, 6),
            box(13, 0, 3, 14, 17, 6),
            box(0.5, 2, 4, 15.5, 3, 6),
            box(0.5, 14, 9, 15.5, 15, 11)
    ).optimize();
    private static final VoxelShape EAST_WEST_COLLISION = Shapes.or(
            box(4, 0, 0, 6, 16, 2),
            box(4, 0, 14, 6, 16, 16),
            box(10, 0, 2, 13, 17, 3),
            box(10, 0, 13, 13, 17, 14),
            box(10, 2, 0.5, 12, 3, 15.5),
            box(5, 14, 0.5, 7, 15, 15.5)
    ).optimize();

    public TanningRackBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
        return direction.getAxis() == Direction.Axis.Z ? NORTH_SOUTH : EAST_WEST;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context
    ) {
        Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
        return direction.getAxis() == Direction.Axis.Z ? NORTH_SOUTH_COLLISION : EAST_WEST_COLLISION;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(pos) instanceof TanningRackBlockEntity rack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (ItemStackInteraction.shouldDeferEmptyMainHand(hand, stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!rack.output().isEmpty()) {
            return ItemInteractionResult.CONSUME;
        }
        if (!rack.input().isEmpty()) {
            return ItemInteractionResult.CONSUME;
        }
        if (rack.canInsert(stack)) {
            return ItemStackInteraction.insert(level, true,
                    () -> rack.insert(stack, player.hasInfiniteMaterials()));
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TanningRackBlockEntity rack) {
            ItemStack result = !rack.output().isEmpty() ? rack.output() : rack.input();
            if (!result.isEmpty()) {
                return ItemStackInteraction.extract(level, pos, player, result,
                        () -> !rack.output().isEmpty() ? rack.extractOutput() : rack.extractInput());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TanningRackBlockEntity rack) {
            rack.dropContents();
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TanningRackBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, TanningRackFeature.BLOCK_ENTITY.get(), TanningRackBlockEntity::clientTick)
                : createTickerHelper(type, TanningRackFeature.BLOCK_ENTITY.get(), TanningRackBlockEntity::serverTick);
    }

}
