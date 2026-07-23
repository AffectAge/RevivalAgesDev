package com.protyvkultury.revivalages.feature.technology.animalpower.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalMachineKind;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature;
import com.protyvkultury.revivalages.feature.technology.animalpower.blockentity.AnimalMachineBlockEntity;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AnimalMachineBlock extends BaseEntityBlock {

    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private static final VoxelShape GRINDSTONE_SELECTION_SHAPE = box(0, 0, 0, 16, 13, 16);
    private static final VoxelShape GRINDSTONE_COLLISION_SHAPE = box(0, 0, 0, 16, 8, 16);
    private static final VoxelShape FULL_SHAPE = box(0, 0, 0, 16, 16, 16);
    private static final VoxelShape PRESS_UPPER_SELECTION_SHAPE = box(0, 0, 0, 16, 12, 16);
    private static final VoxelShape PRESS_UPPER_COLLISION_SHAPE = box(0, 0, 0, 16, 3, 16);

    private final AnimalMachineKind kind;

    public AnimalMachineBlock(AnimalMachineKind kind, BlockBehaviour.Properties properties) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public AnimalMachineKind kind() {
        return kind;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        if (kind.tall() && !context.getLevel().getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        if (kind.tall()) {
            level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        }
        if (kind == AnimalMachineKind.CHOPPING_BLOCK
                && level.getBlockEntity(pos) instanceof AnimalMachineBlockEntity machine) {
            machine.setWoodVariant(stack.getOrDefault(
                    AnimalPowerFeature.WOOD_VARIANT.get(),
                    net.minecraft.resources.ResourceLocation.withDefaultNamespace("oak_log")
            ));
        }
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbor,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (!kind.tall()) {
            return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
        }
        DoubleBlockHalf half = state.getValue(HALF);
        Direction counterpartDirection = half == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
        if (direction == counterpartDirection
                && (!neighbor.is(this) || neighbor.getValue(HALF) == half)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!kind.tall()) {
            return GRINDSTONE_SELECTION_SHAPE;
        }
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER || kind == AnimalMachineKind.CHOPPING_BLOCK) {
            return FULL_SHAPE;
        }
        return PRESS_UPPER_SELECTION_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        if (!kind.tall()) {
            return GRINDSTONE_COLLISION_SHAPE;
        }
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER || kind == AnimalMachineKind.CHOPPING_BLOCK) {
            return FULL_SHAPE;
        }
        return PRESS_UPPER_COLLISION_SHAPE;
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
        AnimalMachineBlockEntity machine = machine(level, pos, state);
        if (machine == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.is(Items.LEAD)) {
            if (!level.isClientSide && machine.attachWorker(player)) {
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.CONSUME;
        }
        if (machine.canInsert(stack)) {
            return ItemStackInteraction.insert(level, true, () -> machine.insert(
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
        AnimalMachineBlockEntity machine = machine(level, pos, state);
        if (machine == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && machine.workerId().isEmpty() && machine.attachWorker(player)) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown() && machine.workerId().isPresent()) {
            if (!level.isClientSide) {
                machine.detachWorker();
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        for (int slot = 2; slot >= 0; slot--) {
            if (!machine.item(slot).isEmpty()) {
                int selected = slot;
                BlockPos base = basePos(pos, state);
                return ItemStackInteraction.extract(
                        level, base, player, machine.item(slot), () -> machine.extract(selected));
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && state.getValue(HALF) == DoubleBlockHalf.LOWER
                && level.getBlockEntity(pos) instanceof AnimalMachineBlockEntity machine) {
            machine.dropContents();
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER
                ? new AnimalMachineBlockEntity(pos, state)
                : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide || state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return null;
        }
        return createTickerHelper(
                type,
                AnimalPowerFeature.ANIMAL_MACHINE_BLOCK_ENTITY.get(),
                AnimalMachineBlockEntity::serverTick
        );
    }

    private static BlockPos basePos(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    @Nullable
    private static AnimalMachineBlockEntity machine(Level level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(basePos(pos, state));
        return blockEntity instanceof AnimalMachineBlockEntity machine ? machine : null;
    }
}
