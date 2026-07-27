package com.protyvkultury.revivalages.feature.technology.soakingpot.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.blockentity.CampfireBlockEntity;
import com.protyvkultury.revivalages.feature.technology.soakingpot.SoakingPotFeature;
import com.protyvkultury.revivalages.feature.technology.soakingpot.blockentity.SoakingPotBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;

public final class SoakingPotBlock extends BaseEntityBlock {

    public static final MapCodec<SoakingPotBlock> CODEC = simpleCodec(SoakingPotBlock::new);
    public static final BooleanProperty CAMPFIRE = BooleanProperty.create("campfire");
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 9, 14);
    private static final VoxelShape CAMPFIRE_SHAPE = box(2, 0, 2, 14, 4, 14);

    public SoakingPotBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                .setValue(CAMPFIRE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING, CAMPFIRE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite())
                .setValue(CAMPFIRE, hasCampfire(context.getLevel(), context.getClickedPos()));
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
        return state.getValue(CAMPFIRE) ? CAMPFIRE_SHAPE : SHAPE;
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
        if (!ContentAvailability.isEnabled(ContentKey.SOAKING_POT)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.revivalages.content_disabled"), true);
            }
            return ItemInteractionResult.CONSUME;
        }
        if (hit.getDirection() != Direction.UP) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
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
        if (!ContentAvailability.isEnabled(ContentKey.SOAKING_POT)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.revivalages.content_disabled"), true);
            }
            return InteractionResult.CONSUME;
        }
        if (hit.getDirection() != Direction.UP) {
            return InteractionResult.PASS;
        }
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
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(this) && hasCampfire(level, pos)) {
            if (!state.getValue(CAMPFIRE)) {
                level.setBlock(pos, state.setValue(CAMPFIRE, true), Block.UPDATE_CLIENTS);
            }
            ejectCampfireCooking(level, pos);
        }
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
        if (direction != Direction.DOWN) {
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        }
        boolean campfire = neighborState.is(CampfireFeature.CAMPFIRE.get());
        if (campfire && !state.getValue(CAMPFIRE) && level instanceof Level concreteLevel) {
            ejectCampfireCooking(concreteLevel, pos);
        }
        return state.setValue(CAMPFIRE, campfire);
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

    private static boolean hasCampfire(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(CampfireFeature.CAMPFIRE.get());
    }

    private static void ejectCampfireCooking(Level level, BlockPos potPos) {
        if (level.isClientSide
                || !(level.getBlockEntity(potPos.below()) instanceof CampfireBlockEntity campfire)) {
            return;
        }
        ItemStack cooking = campfire.extractCookingStack();
        if (!cooking.isEmpty()) {
            Block.popResource(level, potPos.below(), cooking);
        }
    }

}
