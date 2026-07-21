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
import net.minecraft.world.phys.shapes.VoxelShape;

public final class TanningRackBlock extends BaseEntityBlock {

    public static final MapCodec<TanningRackBlock> CODEC = simpleCodec(TanningRackBlock::new);
    private static final VoxelShape NORTH_SOUTH = box(0, 0, 4, 16, 16, 12);
    private static final VoxelShape EAST_WEST = box(4, 0, 0, 12, 16, 16);

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
