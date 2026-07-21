package com.protyvkultury.revivalages.feature.technology.dryingrack.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.core.interaction.OrientedInteractionSpace;
import com.protyvkultury.revivalages.feature.technology.dryingrack.DryingRackFeature;
import com.protyvkultury.revivalages.feature.technology.dryingrack.blockentity.DryingRackBlockEntity;
import com.protyvkultury.revivalages.feature.technology.dryingrack.config.DryingRackConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class DryingRackBlock extends AbstractDryingRackBlock {

    public static final MapCodec<DryingRackBlock> CODEC = simpleCodec(DryingRackBlock::new);
    public static final BooleanProperty STACKED = BooleanProperty.create("stacked");

    private static final VoxelShape SHAPE = Block.box(1.0D, 11.0D, 1.0D, 15.0D, 12.0D, 15.0D);

    public DryingRackBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(STACKED, false)
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<DryingRackBlock> codec() {
        return CODEC;
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
        if (direction == Direction.UP) {
            return state.setValue(STACKED, neighborState.is(DryingRackFeature.DRYING_RACK.get()));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (DryingRackConfig.LADDER_ENABLED.get()
                && entity instanceof LivingEntity livingEntity
                && isVerticalStack(level, pos)) {
            Vec3 movement = livingEntity.getDeltaMovement();
            double climbSpeed = DryingRackConfig.LADDER_CLIMB_SPEED.get();
            double vertical;
            if (livingEntity.isShiftKeyDown()) {
                vertical = 0.0D;
            } else if (livingEntity.horizontalCollision && livingEntity.zza > 0.0F) {
                vertical = climbSpeed;
            } else {
                vertical = Math.max(movement.y, -climbSpeed);
            }
            livingEntity.setDeltaMovement(movement.x, vertical, movement.z);
            livingEntity.resetFallDistance();
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STACKED, HorizontalDirectionalBlock.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return DryingRackBlockEntity.createNormal(pos, state);
    }

    @Override
    protected int slotFromHit(BlockState state, BlockHitResult hitResult) {
        Vec3 location = hitResult.getLocation();
        OrientedInteractionSpace.Point local = OrientedInteractionSpace.worldToLocal(
                state.getValue(HorizontalDirectionalBlock.FACING),
                location.x - Math.floor(location.x),
                location.z - Math.floor(location.z)
        );
        int x = local.x() >= 0.5D ? 1 : 0;
        int z = local.z() >= 0.5D ? 1 : 0;
        return x + z * 2;
    }

    private boolean isVerticalStack(Level level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(this) || level.getBlockState(pos.below()).is(this);
    }
}
