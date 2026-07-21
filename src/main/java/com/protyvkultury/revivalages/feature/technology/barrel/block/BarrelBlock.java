package com.protyvkultury.revivalages.feature.technology.barrel.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import com.protyvkultury.revivalages.feature.technology.barrel.blockentity.BarrelBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;

public final class BarrelBlock extends BaseEntityBlock {

    public static final MapCodec<BarrelBlock> CODEC = simpleCodec(BarrelBlock::new);
    public static final BooleanProperty SEALED = BooleanProperty.create("sealed");

    public BarrelBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SEALED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(SEALED);
    }

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
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(pos) instanceof BarrelBlockEntity barrel)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (state.getValue(SEALED)) {
            return ItemInteractionResult.CONSUME;
        }
        if (stack.is(BarrelFeature.BARREL_LID.get())) {
            if (!level.isClientSide && barrel.seal()) {
                if (!player.hasInfiniteMaterials()) {
                    stack.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (FluidUtil.interactWithFluidHandler(player, hand, barrel.fluidTank())) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        int slot = barrel.slotFromHit(hit.getLocation().x - pos.getX(), hit.getLocation().z - pos.getZ());
        if (!barrel.item(slot).isEmpty()) {
            return ItemInteractionResult.CONSUME;
        }
        if (barrel.canInsert(slot, stack)) {
            return ItemStackInteraction.insert(level, true,
                    () -> barrel.insert(slot, stack, player.hasInfiniteMaterials()));
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BarrelBlockEntity barrel)) {
            return InteractionResult.PASS;
        }
        if (state.getValue(SEALED)) {
            if (!level.isClientSide) {
                barrel.unseal(player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        int slot = barrel.slotFromHit(hit.getLocation().x - pos.getX(), hit.getLocation().z - pos.getZ());
        if (!barrel.item(slot).isEmpty()) {
            return ItemStackInteraction.extract(level, pos, player, barrel.item(slot), () -> barrel.extract(slot));
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())
                && !state.getValue(SEALED)
                && level.getBlockEntity(pos) instanceof BarrelBlockEntity barrel) {
            barrel.dropContents();
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BarrelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, BarrelFeature.BLOCK_ENTITY.get(), BarrelBlockEntity::clientTick)
                : createTickerHelper(type, BarrelFeature.BLOCK_ENTITY.get(), BarrelBlockEntity::serverTick);
    }

}
