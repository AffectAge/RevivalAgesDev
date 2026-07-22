package com.protyvkultury.revivalages.feature.technology.stonemachine.block;

import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineFeature;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineKind;
import com.protyvkultury.revivalages.feature.technology.stonemachine.blockentity.StoneMachineBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;

public abstract class StoneMachineBlock extends BaseEntityBlock {

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape TOP_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D);

    protected StoneMachineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(LIT, false));
    }

    public abstract StoneMachineKind kind();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING, HALF, LIT);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? TOP_SHAPE : super.getShape(state, level, pos, context);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos above = context.getClickedPos().above();
        if (above.getY() >= context.getLevel().getMaxBuildHeight()
                || !context.getLevel().getBlockState(above).canBeReplaced(context)) {
            return null;
        }
        return defaultBlockState().setValue(
                HorizontalDirectionalBlock.FACING,
                context.getHorizontalDirection().getOpposite()
        );
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
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
        DoubleBlockHalf half = state.getValue(HALF);
        Direction counterpartDirection = half == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
        if (direction == counterpartDirection
                && (!neighborState.is(this) || neighborState.getValue(HALF) == half)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
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
        StoneMachineBlockEntity machine = machine(level, pos, state);
        if (machine == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.is(Items.WATER_BUCKET) && machine.isLit()) {
            if (!level.isClientSide) {
                machine.extinguish();
                if (!player.hasInfiniteMaterials()) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) {
            if (!level.isClientSide && machine.ignite()) {
                if (stack.is(Items.FLINT_AND_STEEL)) {
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                } else if (!player.hasInfiniteMaterials()) {
                    stack.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (kind() == StoneMachineKind.CRUCIBLE
                && FluidUtil.interactWithFluidHandler(player, hand, machine.fluidTank())) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (kind() == StoneMachineKind.SAWMILL && machine.canInsertBlade(stack)) {
            return ItemStackInteraction.insert(level, true,
                    () -> machine.insertBlade(stack, player.hasInfiniteMaterials(), player));
        }
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER && machine.canInsertInput(stack)) {
            return ItemStackInteraction.insert(level, true,
                    () -> machine.insertInput(stack, player.hasInfiniteMaterials()));
        }
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER && machine.canInsertFuel(stack)) {
            return ItemStackInteraction.insert(level, true,
                    () -> machine.insertFuel(stack, player.hasInfiniteMaterials()));
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
        StoneMachineBlockEntity machine = machine(level, pos, state);
        if (machine == null) {
            return InteractionResult.PASS;
        }
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            if (!machine.firstOutput().isEmpty()) {
                if (!level.isClientSide) {
                    machine.giveOutputs(player);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!machine.input().isEmpty()) {
                return ItemStackInteraction.extract(level, machine.getBlockPos(), player, machine.input(),
                        machine::extractInput);
            }
            if (kind() == StoneMachineKind.SAWMILL && !machine.blade().isEmpty()) {
                return ItemStackInteraction.extract(level, machine.getBlockPos(), player, machine.blade(),
                        () -> machine.extractBlade(player));
            }
        } else if (!machine.fuel().isEmpty()) {
            return ItemStackInteraction.extract(level, machine.getBlockPos(), player, machine.fuel(),
                    machine::extractFuel);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
        if (kind() == StoneMachineKind.SAWMILL
                && state.getValue(HALF) == DoubleBlockHalf.UPPER
                && level.getGameTime() % 10L == 0L
                && machine(level, pos, state) instanceof StoneMachineBlockEntity machine
                && machine.isLit()
                && !machine.blade().isEmpty()) {
            entity.hurt(level.damageSources().generic(), PrimitiveTechnologyConfig.STONE_SAWMILL_BLADE_DAMAGE.get().floatValue());
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && state.getValue(HALF) == DoubleBlockHalf.LOWER
                && level.getBlockEntity(pos) instanceof StoneMachineBlockEntity machine) {
            machine.dropContents();
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable
    private static StoneMachineBlockEntity machine(Level level, BlockPos pos, BlockState state) {
        BlockPos root = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        return level.getBlockEntity(root) instanceof StoneMachineBlockEntity machine ? machine : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StoneMachineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return null;
        }
        return level.isClientSide
                ? createTickerHelper(type, StoneMachineFeature.BLOCK_ENTITY.get(), StoneMachineBlockEntity::clientTick)
                : createTickerHelper(type, StoneMachineFeature.BLOCK_ENTITY.get(), StoneMachineBlockEntity::serverTick);
    }
}
