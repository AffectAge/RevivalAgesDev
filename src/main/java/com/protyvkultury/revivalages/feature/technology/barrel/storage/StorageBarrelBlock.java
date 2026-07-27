package com.protyvkultury.revivalages.feature.technology.barrel.storage;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class StorageBarrelBlock extends BaseEntityBlock {

    public static final MapCodec<StorageBarrelBlock> CODEC = simpleCodec(StorageBarrelBlock::new);
    public static final BooleanProperty SEALED = BooleanProperty.create("sealed");

    public StorageBarrelBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SEALED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SEALED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit
    ) {
        if (!ContentAvailability.isEnabled(ContentKey.STORAGE_BARREL)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.revivalages.content_disabled"), true);
            }
            return ItemInteractionResult.CONSUME;
        }
        if (!(level.getBlockEntity(pos) instanceof StorageBarrelBlockEntity barrel)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (state.getValue(SEALED)) {
            return hand == InteractionHand.MAIN_HAND && stack.isEmpty() && hit.getDirection() == Direction.UP
                    ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                    : ItemInteractionResult.CONSUME;
        }
        if (stack.is(BarrelFeature.BARREL_LID.get())) {
            if (hit.getDirection() == Direction.UP && !level.isClientSide && barrel.seal()
                    && !player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit
    ) {
        if (!ContentAvailability.isEnabled(ContentKey.STORAGE_BARREL)
                || !(level.getBlockEntity(pos) instanceof StorageBarrelBlockEntity barrel)) {
            return InteractionResult.CONSUME;
        }
        if (state.getValue(SEALED)) {
            if (hit.getDirection() != Direction.UP) {
                return InteractionResult.CONSUME;
            }
            if (!level.isClientSide) {
                barrel.unseal(player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            player.openMenu(barrel);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof StorageBarrelBlockEntity barrel
                ? AbstractContainerMenu.getRedstoneSignalFromContainer(barrel)
                : 0;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof StorageBarrelBlockEntity barrel) {
            barrel.dropContents();
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StorageBarrelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type
    ) {
        return level.isClientSide ? null : createTickerHelper(
                type,
                BarrelFeature.STORAGE_BARREL_BLOCK_ENTITY.get(),
                StorageBarrelBlockEntity::serverTick
        );
    }
}
