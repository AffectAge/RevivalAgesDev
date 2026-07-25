package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public final class StructuralFallingBlockEntity extends FallingBlockEntity {

    private boolean failedBreakCheck;

    public StructuralFallingBlockEntity(
            EntityType<? extends FallingBlockEntity> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    public static StructuralFallingBlockEntity fall(
            Level level,
            BlockPos pos,
            BlockState state,
            float damagePerBlock,
            int maximumDamage
    ) {
        BlockState fallingState = state.hasProperty(BlockStateProperties.WATERLOGGED)
                ? state.setValue(BlockStateProperties.WATERLOGGED, false)
                : state;
        level.setBlockAndUpdate(pos, state);
        StructuralFallingBlockEntity entity = new StructuralFallingBlockEntity(
                StructuralIntegrityFeature.FALLING_BLOCK_ENTITY.get(),
                level
        );
        CompoundTag data = new CompoundTag();
        data.put("BlockState", NbtUtils.writeBlockState(fallingState));
        entity.readAdditionalSaveData(data);
        entity.blocksBuilding = true;
        entity.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.xo = entity.getX();
        entity.yo = entity.getY();
        entity.zo = entity.getZ();
        entity.setStartPos(pos);
        entity.setHurtsEntities(damagePerBlock, maximumDamage);
        level.addFreshEntity(entity);
        return entity;
    }

    public static boolean canFallThrough(
            BlockGetter level,
            BlockPos pos,
            Direction fallingDirection,
            BlockState fallingState
    ) {
        BlockState obstacle = level.getBlockState(pos);
        return !obstacle.isFaceSturdy(level, pos, fallingDirection.getOpposite())
                && toughness(fallingState) >= toughness(obstacle)
                && obstacle.getDestroySpeed(level, pos) > -1.0F
                && !obstacle.is(Blocks.STRUCTURE_VOID);
    }

    @Override
    public void tick() {
        BlockState fallingState = getBlockState();
        if (fallingState.isAir()) {
            discard();
            return;
        }
        Block block = fallingState.getBlock();
        if (time++ == 0) {
            BlockPos start = blockPosition();
            if (level().getBlockState(start).getBlock() == block) {
                level().removeBlock(start, false);
            } else if (!level().isClientSide) {
                discard();
            }
            return;
        }

        applyGravity();
        move(MoverType.SELF, getDeltaMovement());
        handlePortal();
        if (!level().isClientSide && (isAlive() || forceTickAfterTeleportToDuplicate)) {
            BlockPos current = blockPosition();
            if (!onGround()) {
                failedBreakCheck = false;
                if ((time > 100
                        && (current.getY() < level().getMinBuildHeight()
                        || current.getY() > level().getMaxBuildHeight()))
                        || time > 600) {
                    dropAsItem(fallingState);
                    discard();
                }
            } else {
                if (!failedBreakCheck) {
                    if (destroyIfFallThrough(current, fallingState)
                            || destroyIfFallThrough(current.below(), fallingState)) {
                        failedBreakCheck = true;
                        return;
                    }
                }
                BlockState hitState = level().getBlockState(current);
                setDeltaMovement(getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
                if (!hitState.is(Blocks.MOVING_PISTON)) {
                    discard();
                    if (canPlaceAt(hitState, current, fallingState, fallingState)) {
                        placeOrDrop(hitState, current, fallingState);
                    } else {
                        BlockPos above = current.above();
                        BlockState aboveState = level().getBlockState(above);
                        if (canPlaceAt(
                                aboveState,
                                above,
                                fallingState,
                                Blocks.BEDROCK.defaultBlockState()
                        )) {
                            placeOrDrop(aboveState, above, fallingState);
                        } else if (canFallThrough(
                                level(),
                                above,
                                Direction.DOWN,
                                Blocks.BEDROCK.defaultBlockState()
                        )) {
                            level().destroyBlock(above, true, this);
                            placeOrDrop(aboveState, above, fallingState);
                        } else {
                            dropAsItem(fallingState);
                        }
                    }
                    if (fallingState.getBlock() instanceof StructuralFallable fallable) {
                        fallable.onStructuralFallFinished(level(), current, this);
                    }
                }
            }
        }
        setDeltaMovement(getDeltaMovement().scale(0.98D));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag data) {
        super.addAdditionalSaveData(data);
        data.putBoolean("FailedBreakCheck", failedBreakCheck);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag data) {
        super.readAdditionalSaveData(data);
        failedBreakCheck = data.getBoolean("FailedBreakCheck");
    }

    static int toughness(BlockState state) {
        if (state.getBlock() == Blocks.BEDROCK) {
            return 4;
        }
        if (state.is(StructuralIntegrityTags.TOUGHNESS_3)) {
            return 3;
        }
        if (state.is(StructuralIntegrityTags.TOUGHNESS_2)) {
            return 2;
        }
        if (state.is(StructuralIntegrityTags.TOUGHNESS_1)) {
            return 1;
        }
        return 0;
    }

    private boolean destroyIfFallThrough(BlockPos pos, BlockState fallingState) {
        BlockState obstacle = level().getBlockState(pos);
        if (!isAirOrEmptyFluid(obstacle)
                && canFallThrough(level(), pos, Direction.DOWN, fallingState)) {
            level().destroyBlock(pos, true, this);
            return true;
        }
        return false;
    }

    private boolean canPlaceAt(
            BlockState hitState,
            BlockPos pos,
            BlockState fallingState,
            BlockState toughnessState
    ) {
        return hitState.canBeReplaced(
                new DirectionalPlaceContext(level(), pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)
        )
                && fallingState.canSurvive(level(), pos)
                && !canFallThrough(level(), pos.below(), Direction.DOWN, toughnessState);
    }

    private void placeOrDrop(BlockState hitState, BlockPos pos, BlockState fallingState) {
        if (!level().setBlockAndUpdate(pos, fallingState)) {
            dropAsItem(fallingState);
            return;
        }
        if (fallingState.getBlock() instanceof Fallable fallable) {
            fallable.onLand(level(), pos, fallingState, hitState, this);
        }
        if (fallingState.is(StructuralIntegrityTags.CAN_LANDSLIDE)
                && level() instanceof ServerLevel server) {
            StructuralSimulation.scheduleLandslide(server, pos);
        }
        if (blockData != null && fallingState.hasBlockEntity()) {
            BlockEntity blockEntity = level().getBlockEntity(pos);
            if (blockEntity != null) {
                CompoundTag merged = blockEntity.saveWithoutMetadata(level().registryAccess());
                for (String key : blockData.getAllKeys()) {
                    merged.put(key, blockData.get(key).copy());
                }
                blockEntity.loadWithComponents(merged, level().registryAccess());
                blockEntity.setChanged();
            }
        }
    }

    private void dropAsItem(BlockState fallingState) {
        if (dropItem
                && level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)
                && level() instanceof ServerLevel server) {
            Block.dropResources(
                    fallingState,
                    server,
                    blockPosition(),
                    null,
                    this,
                    ItemStack.EMPTY
            );
        }
    }

    private static boolean isAirOrEmptyFluid(BlockState state) {
        if (state.isAir()) {
            return true;
        }
        var fluid = state.getFluidState();
        return !fluid.isEmpty()
                && state.getBlock()
                == fluid.getType().defaultFluidState().createLegacyBlock().getBlock();
    }
}
