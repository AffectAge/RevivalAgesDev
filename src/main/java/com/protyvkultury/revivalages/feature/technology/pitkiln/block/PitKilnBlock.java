package com.protyvkultury.revivalages.feature.technology.pitkiln.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import com.protyvkultury.revivalages.feature.technology.pitkiln.PitKilnFeature;
import com.protyvkultury.revivalages.feature.technology.pitkiln.blockentity.PitKilnBlockEntity;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PitKilnBlock extends BaseEntityBlock {

    public static final MapCodec<PitKilnBlock> CODEC = simpleCodec(PitKilnBlock::new);
    public static final EnumProperty<PitKilnStage> STAGE = EnumProperty.create("stage", PitKilnStage.class);
    private static final VoxelShape EMPTY_SHAPE = box(0, 0, 0, 16, 3, 16);
    private static final VoxelShape THATCH_SHAPE = box(0, 0, 0, 16, 10, 16);

    public PitKilnBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STAGE, PitKilnStage.EMPTY));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(STAGE)) {
            case EMPTY -> EMPTY_SHAPE;
            case THATCH -> THATCH_SHAPE;
            default -> net.minecraft.world.phys.shapes.Shapes.block();
        };
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
        if (!(level.getBlockEntity(pos) instanceof PitKilnBlockEntity kiln)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        PitKilnStage stage = state.getValue(STAGE);
        if (stage == PitKilnStage.ACTIVE) {
            return ItemInteractionResult.CONSUME;
        }
        if (stage == PitKilnStage.COMPLETE) {
            return ItemInteractionResult.CONSUME;
        }
        if ((stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) && stage == PitKilnStage.WOOD) {
            if (!kiln.canIgnite()) {
                return ItemInteractionResult.CONSUME;
            }
            if (!level.isClientSide) {
                kiln.ignite();
                if (!player.hasInfiniteMaterials()) {
                    if (stack.is(Items.FLINT_AND_STEEL)) {
                        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                    } else {
                        stack.shrink(1);
                    }
                }
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stage == PitKilnStage.EMPTY && stack.is(PrimitiveMaterialsFeature.THATCH_ITEM.get()) && !kiln.input().isEmpty()) {
            if (!level.isClientSide) {
                kiln.addThatch();
                if (!player.hasInfiniteMaterials()) {
                    stack.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if ((stage == PitKilnStage.THATCH || stage == PitKilnStage.WOOD)
                && (stack.is(PrimitiveTags.PIT_KILN_LOGS) || stack.is(net.minecraft.tags.ItemTags.LOGS))) {
            if (!kiln.canAddLog()) {
                return ItemInteractionResult.CONSUME;
            }
            if (!level.isClientSide) {
                kiln.addLog(stack, player.hasInfiniteMaterials());
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stage == PitKilnStage.EMPTY && kiln.canInsert(stack)) {
            if (!level.isClientSide) {
                kiln.insert(stack, player.hasInfiniteMaterials());
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PitKilnBlockEntity kiln)) {
            return InteractionResult.PASS;
        }
        PitKilnStage stage = state.getValue(STAGE);
        if (stage == PitKilnStage.COMPLETE) {
            if (!level.isClientSide) {
                kiln.giveOutputs(player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if ((stage == PitKilnStage.THATCH || stage == PitKilnStage.WOOD) && kiln.logCount() > 0) {
            return ItemStackInteraction.extract(level, pos, player, kiln.logStack(kiln.logCount() - 1), kiln::removeLog);
        }
        if (stage == PitKilnStage.EMPTY && !kiln.input().isEmpty()) {
            return ItemStackInteraction.extract(level, pos, player, kiln.input(), kiln::extractInput);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PitKilnBlockEntity kiln) {
            kiln.removeFireAbove();
            Containers.dropContents(level, pos, kiln.drops());
            if (state.getValue(STAGE) == PitKilnStage.THATCH || state.getValue(STAGE) == PitKilnStage.WOOD) {
                popResource(level, pos, new ItemStack(PrimitiveMaterialsFeature.THATCH_ITEM.get()));
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PitKilnBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, PitKilnFeature.BLOCK_ENTITY.get(), PitKilnBlockEntity::clientTick)
                : createTickerHelper(type, PitKilnFeature.BLOCK_ENTITY.get(), PitKilnBlockEntity::serverTick);
    }

    @Override
    public boolean isFireSource(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos, net.minecraft.core.Direction direction) {
        return direction == net.minecraft.core.Direction.UP && state.getValue(STAGE) == PitKilnStage.ACTIVE;
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            net.minecraft.world.level.block.Block neighborBlock,
            BlockPos neighborPos,
            boolean movedByPiston
    ) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide
                && state.getValue(STAGE) == PitKilnStage.WOOD
                && neighborPos.equals(pos.above())
                && level.getBlockState(neighborPos).is(net.minecraft.world.level.block.Blocks.FIRE)
                && level.getBlockEntity(pos) instanceof PitKilnBlockEntity kiln
                && kiln.canIgnite()) {
            kiln.ignite();
        }
    }
}
