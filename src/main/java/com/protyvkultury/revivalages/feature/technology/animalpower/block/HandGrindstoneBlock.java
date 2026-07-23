package com.protyvkultury.revivalages.feature.technology.animalpower.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import com.protyvkultury.revivalages.feature.technology.animalpower.blockentity.HandGrindstoneBlockEntity;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class HandGrindstoneBlock extends BaseEntityBlock {

    public static final MapCodec<HandGrindstoneBlock> CODEC = simpleCodec(HandGrindstoneBlock::new);
    private static final VoxelShape SELECTION_SHAPE = box(1, 0, 1, 15, 14, 15);
    private static final VoxelShape COLLISION_SHAPE = box(1, 0, 1, 15, 10, 15);

    public HandGrindstoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
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
    protected VoxelShape getShape(
            BlockState state,
            net.minecraft.world.level.BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SELECTION_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            net.minecraft.world.level.BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return COLLISION_SHAPE;
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
        if (!(level.getBlockEntity(pos) instanceof HandGrindstoneBlockEntity grindstone)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (grindstone.canInsert(stack)) {
            return ItemStackInteraction.insert(level, true, () -> grindstone.insert(
                    stack, player.hasInfiniteMaterials()));
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(pos) instanceof HandGrindstoneBlockEntity grindstone)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            for (int slot = 2; slot >= 0; slot--) {
                if (!grindstone.item(slot).isEmpty()) {
                    int selected = slot;
                    return ItemStackInteraction.extract(
                            level, pos, player, grindstone.item(slot), () -> grindstone.extract(selected));
                }
            }
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && grindstone.turn(player)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof HandGrindstoneBlockEntity grindstone) {
            grindstone.dropContents();
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HandGrindstoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return level.isClientSide ? null : createTickerHelper(
                type,
                com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature
                        .HAND_GRINDSTONE_BLOCK_ENTITY.get(),
                HandGrindstoneBlockEntity::serverTick
        );
    }
}
