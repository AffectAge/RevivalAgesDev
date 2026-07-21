package com.protyvkultury.revivalages.feature.technology.dryingrack.block;

import com.protyvkultury.revivalages.feature.technology.dryingrack.blockentity.DryingRackBlockEntity;
import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class AbstractDryingRackBlock extends BaseEntityBlock {

    protected AbstractDryingRackBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected abstract MapCodec<? extends AbstractDryingRackBlock> codec();

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            net.minecraft.world.InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (!(level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        int slot = slotFromHit(state, hitResult);
        if (!rack.getItem(slot).isEmpty()) {
            return ItemInteractionResult.CONSUME;
        }
        if (!rack.canInsert(slot)) {
            return ItemInteractionResult.CONSUME;
        }
        return ItemStackInteraction.insert(level, true,
                () -> rack.insert(slot, stack, player.hasInfiniteMaterials()));
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!(level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack)) {
            return InteractionResult.PASS;
        }
        int slot = slotFromHit(state, hitResult);
        return ItemStackInteraction.extract(level, pos, player, rack.getItem(slot), () -> rack.extract(slot));
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack) {
            Containers.dropContents(level, pos, rack.getItems());
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> blockEntityType
    ) {
        if (level.isClientSide) {
            return (tickerLevel, pos, tickerState, blockEntity) -> {
                if (blockEntity instanceof DryingRackBlockEntity rack) {
                    DryingRackBlockEntity.clientTick(tickerLevel, pos, tickerState, rack);
                }
            };
        }
        return (tickerLevel, pos, tickerState, blockEntity) -> {
            if (blockEntity instanceof DryingRackBlockEntity rack) {
                DryingRackBlockEntity.serverTick(tickerLevel, pos, tickerState, rack);
            }
        };
    }

    protected abstract int slotFromHit(BlockState state, BlockHitResult hitResult);

}
