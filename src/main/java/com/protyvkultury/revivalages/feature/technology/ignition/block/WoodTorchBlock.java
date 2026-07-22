package com.protyvkultury.revivalages.feature.technology.ignition.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.feature.technology.ignition.IgnitionFeature;
import com.protyvkultury.revivalages.feature.technology.ignition.blockentity.WoodTorchBlockEntity;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class WoodTorchBlock extends BaseEntityBlock {

    public static final MapCodec<WoodTorchBlock> CODEC = simpleCodec(WoodTorchBlock::new);
    public static final DirectionProperty FACING = DirectionProperty.create("facing", direction -> direction != Direction.DOWN);
    public static final EnumProperty<WoodTorchState> STATE = EnumProperty.create("state", WoodTorchState.class);
    private static final VoxelShape FLOOR = box(6, 0, 6, 10, 11, 10);
    private static final VoxelShape NORTH = box(6, 3, 11, 10, 14, 16);
    private static final VoxelShape SOUTH = box(6, 3, 0, 10, 14, 5);
    private static final VoxelShape WEST = box(11, 3, 6, 16, 14, 10);
    private static final VoxelShape EAST = box(0, 3, 6, 5, 14, 10);

    public WoodTorchBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(STATE, WoodTorchState.UNLIT));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction == Direction.DOWN) {
                continue;
            }
            BlockState state = defaultBlockState().setValue(FACING, direction);
            if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
                return state;
            }
        }
        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos support = facing == Direction.UP ? pos.below() : pos.relative(facing.getOpposite());
        Direction face = facing == Direction.UP ? Direction.UP : facing;
        return Block.canSupportCenter(level, support, face);
    }

    @Override
    protected BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos
    ) {
        Direction facing = state.getValue(FACING);
        Direction supportDirection = facing == Direction.UP ? Direction.DOWN : facing.getOpposite();
        return direction == supportDirection && !state.canSurvive(level, pos)
                ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            default -> FLOOR;
        };
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
        WoodTorchState torchState = state.getValue(STATE);
        if ((stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) && torchState != WoodTorchState.LIT) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof WoodTorchBlockEntity torch) {
                if (torch.ignite()) {
                    if (!player.hasInfiniteMaterials()) {
                        if (stack.is(Items.FLINT_AND_STEEL)) {
                            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                        } else {
                            stack.shrink(1);
                        }
                    }
                    level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.is(Items.WATER_BUCKET) && torchState == WoodTorchState.LIT) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof WoodTorchBlockEntity torch) {
                torch.douse();
                if (!player.hasInfiniteMaterials()) {
                    if (stack.getCount() == 1) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    } else {
                        stack.shrink(1);
                        if (!player.getInventory().add(new ItemStack(Items.BUCKET))) {
                            player.drop(new ItemStack(Items.BUCKET), false);
                        }
                    }
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (torchState == WoodTorchState.LIT) {
            IFluidHandler fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
            FluidStack simulatedDrain = fluidHandler == null
                    ? FluidStack.EMPTY
                    : fluidHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
            if (simulatedDrain.is(Fluids.WATER) && simulatedDrain.getAmount() == 1000) {
                if (!level.isClientSide && level.getBlockEntity(pos) instanceof WoodTorchBlockEntity torch) {
                    torch.douse();
                    if (!player.hasInfiniteMaterials()) {
                        fluidHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(STATE) == WoodTorchState.LIT
                && PrimitiveTechnologyConfig.WOOD_TORCH_FIRE_DAMAGE.get() > 0) {
            entity.igniteForSeconds(2.0F);
            entity.hurt(level.damageSources().inFire(), PrimitiveTechnologyConfig.WOOD_TORCH_FIRE_DAMAGE.get());
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(STATE) != WoodTorchState.LIT) {
            return;
        }
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.75D;
        double z = pos.getZ() + 0.5D;
        Direction facing = state.getValue(FACING);
        if (facing != Direction.UP) {
            x -= facing.getStepX() * 0.25D;
            y += 0.15D;
            z -= facing.getStepZ() * 0.25D;
        }
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public boolean hasDynamicLightEmission(BlockState state) {
        return state.getValue(STATE) == WoodTorchState.LIT;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(STATE) == WoodTorchState.LIT
                ? PrimitiveTechnologyConfig.WOOD_TORCH_LIGHT.get()
                : 0;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        Direction direction = state.getValue(FACING);
        return direction.getAxis().isHorizontal() ? state.setValue(FACING, rotation.rotate(direction)) : state;
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WoodTorchBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? null
                : createTickerHelper(type, IgnitionFeature.WOOD_TORCH_BLOCK_ENTITY.get(), WoodTorchBlockEntity::serverTick);
    }
}
