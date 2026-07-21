package com.protyvkultury.revivalages.feature.technology.soakingpot.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import com.protyvkultury.revivalages.feature.technology.soakingpot.SoakingPotFeature;
import com.protyvkultury.revivalages.feature.technology.soakingpot.blockentity.SoakingPotBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.neoforged.neoforge.fluids.FluidUtil;

public final class SoakingPotBlock extends BaseEntityBlock {

    public static final MapCodec<SoakingPotBlock> CODEC = simpleCodec(SoakingPotBlock::new);
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 9, 14);
    private static final VoxelShape CAMPFIRE_SHAPE = box(2, 0, 2, 14, 4, 14);

    public SoakingPotBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return level.getBlockState(pos.below()).is(com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature.CAMPFIRE.get())
                ? CAMPFIRE_SHAPE
                : SHAPE;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return box(0, 0, 0, 16, 1, 16);
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
        if (!(level.getBlockEntity(pos) instanceof SoakingPotBlockEntity pot)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (FluidUtil.interactWithFluidHandler(player, hand, pot.fluidTank())) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!pot.output().isEmpty()) {
            return ItemInteractionResult.CONSUME;
        }
        if (!pot.input().isEmpty()) {
            if (pot.canInsert(stack)) {
                return ItemStackInteraction.insert(level, true,
                        () -> pot.insert(stack, player.hasInfiniteMaterials(), player.isShiftKeyDown()));
            }
            return ItemInteractionResult.CONSUME;
        }
        if (pot.canInsert(stack)) {
            return ItemStackInteraction.insert(level, true,
                    () -> pot.insert(stack, player.hasInfiniteMaterials(), player.isShiftKeyDown()));
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof SoakingPotBlockEntity pot) {
            ItemStack result = !pot.output().isEmpty() ? pot.output() : pot.input();
            if (!result.isEmpty()) {
                return ItemStackInteraction.extract(level, pos, player, result,
                        () -> !pot.output().isEmpty() ? pot.extractOutput() : pot.extractInput());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof SoakingPotBlockEntity pot) {
            pot.dropContents();
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoakingPotBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, SoakingPotFeature.BLOCK_ENTITY.get(), SoakingPotBlockEntity::clientTick)
                : createTickerHelper(type, SoakingPotFeature.BLOCK_ENTITY.get(), SoakingPotBlockEntity::serverTick);
    }

}
