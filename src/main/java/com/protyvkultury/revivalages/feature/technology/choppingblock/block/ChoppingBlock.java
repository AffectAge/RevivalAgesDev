package com.protyvkultury.revivalages.feature.technology.choppingblock.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingBlockFeature;
import com.protyvkultury.revivalages.feature.technology.choppingblock.blockentity.ChoppingBlockEntity;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveTags;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ChoppingBlock extends BaseEntityBlock {

    public static final MapCodec<ChoppingBlock> CODEC = simpleCodec(ChoppingBlock::new);
    public static final IntegerProperty DAMAGE = IntegerProperty.create("damage", 0, 5);
    private static final VoxelShape SHAPE = box(1, 0, 1, 15, 12, 15);

    public ChoppingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(DAMAGE, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(DAMAGE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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
        if (!(level.getBlockEntity(pos) instanceof ChoppingBlockEntity chopping)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.is(net.minecraft.tags.ItemTags.SHOVELS) && chopping.sawdust() > 0) {
            if (!level.isClientSide) {
                ItemStackInteraction.giveOrDrop(level, pos, player,
                        new ItemStack(PrimitiveMaterialsFeature.WOOD_CHIPS.get()));
                chopping.removeSawdust();
                player.causeFoodExhaustion(com.protyvkultury.revivalages.feature.technology.primitive.config
                        .PrimitiveTechnologyConfig.CHOPPING_EXHAUSTION_PER_CHIP_SCOOP.get().floatValue());
                if (!player.hasInfiniteMaterials()) {
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                }
                level.playSound(null, pos, SoundEvents.SAND_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!chopping.input().isEmpty()
                && stack.is(PrimitiveTags.CHOPPING_AXES)
                && !stack.is(PrimitiveTags.INVALID_CHOPPING_AXES)) {
            if (player.getFoodData().getFoodLevel() < com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig.CHOPPING_MINIMUM_HUNGER.get()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                        "message.revivalages.chopping.not_enough_hunger"), true);
                return ItemInteractionResult.CONSUME;
            }
            if (!level.isClientSide) {
                chopping.chop(player, stack, hand);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!chopping.input().isEmpty()) {
            return ItemInteractionResult.CONSUME;
        }
        if (chopping.canInsert(stack)) {
            return ItemStackInteraction.insert(level, true, () -> {
                chopping.insert(stack, player.hasInfiniteMaterials());
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.7F, 1.0F);
            });
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof ChoppingBlockEntity chopping && !chopping.input().isEmpty()) {
            return ItemStackInteraction.extract(level, pos, player, chopping.input(), chopping::extract);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ChoppingBlockEntity chopping) {
            Containers.dropContents(level, pos, new net.minecraft.world.SimpleContainer(chopping.input()));
            if (chopping.sawdust() > 0) {
                popResource(level, pos, new ItemStack(PrimitiveMaterialsFeature.WOOD_CHIPS.get(), chopping.sawdust()));
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChoppingBlockEntity(pos, state);
    }

}
