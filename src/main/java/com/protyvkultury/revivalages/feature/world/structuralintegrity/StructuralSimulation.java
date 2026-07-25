package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import com.protyvkultury.revivalages.api.event.StructuralCollapseEvent;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.recipe.BlockTransformationRecipe;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.network.CollapseShakePayload;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class StructuralSimulation {

    private StructuralSimulation() {
    }

    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = event.getPos();
        if (StructuralIntegrityConfig.collapsesEnabled()
                && event.getState().is(StructuralIntegrityTags.CAN_TRIGGER_COLLAPSE)) {
            tryTriggerCollapse(level, pos);
        }
        if (StructuralIntegrityConfig.landslidesEnabled()) {
            enqueueLandslidesAround(level, pos);
        }
    }

    public static void onNeighborUpdate(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !StructuralIntegrityConfig.landslidesEnabled()) {
            return;
        }
        for (Direction direction : event.getNotifiedSides()) {
            enqueueLandslide(level, event.getPos().relative(direction));
        }
    }

    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !StructuralIntegrityConfig.collapsesEnabled()) {
            return;
        }
        BlockPos center = BlockPos.containing(event.getExplosion().center());
        List<BlockPos> frontier = new ArrayList<>();
        double radiusSquared = 0.0D;
        for (BlockPos affected : event.getAffectedBlocks()) {
            radiusSquared = Math.max(radiusSquared, affected.distSqr(center));
            if (level.random.nextDouble() < StructuralIntegrityConfig.EXPLOSION_PROPAGATE_CHANCE.get()) {
                frontier.add(affected.above());
            }
        }
        addCollapseRun(level, center, frontier, radiusSquared);
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        StructuralSavedData pending = work(level);
        int budget = StructuralIntegrityConfig.UPDATE_BUDGET_PER_TICK.get();
        if (StructuralIntegrityConfig.collapsesEnabled()
                && !pending.collapses().isEmpty()
                && level.random.nextInt(StructuralIntegrityConfig.PROPAGATION_INTERVAL.get()) == 0) {
            budget -= processCollapseRuns(level, pending, budget);
        }
        if (!StructuralIntegrityConfig.landslidesEnabled()) {
            return;
        }
        while (budget-- > 0) {
            BlockPos landslide = pending.pollReadyLandslide(level.getGameTime());
            if (landslide == null) {
                break;
            }
            processLandslide(level, landslide);
        }
    }

    static boolean tryTriggerCollapse(ServerLevel level, BlockPos mined) {
        if (!level.isAreaLoaded(mined, 32)) {
            return false;
        }
        RandomSource random = level.random;
        boolean real = random.nextDouble() < StructuralIntegrityConfig.COLLAPSE_TRIGGER_CHANCE.get();
        boolean fake = !real
                && random.nextDouble() < StructuralIntegrityConfig.COLLAPSE_FAKE_TRIGGER_CHANCE.get();
        if (!real && !fake) {
            return false;
        }

        int radiusX = (random.nextInt(5) + 4) / 2;
        int radiusY = (random.nextInt(3) + 2) / 2;
        int radiusZ = (random.nextInt(5) + 4) / 2;
        List<BlockPos> candidates = findUnsupportedOrigins(level, mined, radiusX, radiusY, radiusZ);
        if (candidates.isEmpty()) {
            return false;
        }
        if (fake) {
            List<BlockPos> displayed = uniqueRandomSample(candidates, random);
            level.playSound(
                    null,
                    mined,
                    StructuralIntegrityFeature.COLLAPSE_WARNING.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
            displayed.forEach(pos -> level.levelEvent(
                    null,
                    LevelEvent.PARTICLES_DESTROY_BLOCK,
                    pos,
                    Block.getId(level.getBlockState(pos))
            ));
            NeoForge.EVENT_BUS.post(new StructuralCollapseEvent(level, mined, displayed, 0.0D, true));
            sendCameraShake(
                    level,
                    mined,
                    StructuralIntegrityConfig.WARNING_SHAKE_STRENGTH.get(),
                    StructuralIntegrityConfig.WARNING_SHAKE_DURATION.get()
            );
            return false;
        }

        BlockPos candidate = candidates.getFirst();
        boolean started = startCollapse(level, candidate);
        if (started) {
            level.playSound(
                    null,
                    mined,
                    StructuralIntegrityFeature.COLLAPSE_START.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
        }
        return started;
    }

    private static List<BlockPos> findUnsupportedOrigins(
            ServerLevel level,
            BlockPos mined,
            int radiusX,
            int radiusY,
            int radiusZ
    ) {
        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos cursor : BlockPos.betweenClosed(
                mined.offset(-radiusX, -radiusY, -radiusZ),
                mined.offset(radiusX, radiusY, radiusZ)
        )) {
            if (!cursor.equals(mined)
                    && !SupportService.isSupported(level, cursor)
                    && canStartCollapse(level, cursor)) {
                candidates.add(cursor.immutable());
            }
        }
        return candidates;
    }

    private static boolean canStartCollapse(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(StructuralIntegrityTags.CAN_START_COLLAPSE)) {
            return false;
        }
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return StructuralFallingBlockEntity.canFallThrough(level, below, Direction.DOWN, state)
                || !belowState.isCollisionShapeFullBlock(level, below)
                || belowState.is(StructuralIntegrityTags.NOT_SOLID_SUPPORTING);
    }

    static boolean startCollapse(ServerLevel level, BlockPos center) {
        int radius = StructuralIntegrityConfig.COLLAPSE_MIN_RADIUS.get()
                + level.random.nextInt(StructuralIntegrityConfig.COLLAPSE_RADIUS_VARIANCE.get());
        double radiusSquared = (double) radius * radius;
        List<BlockPos> frontier = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (BlockPos column : BlockPos.betweenClosed(
                center.offset(-radius, -4, -radius),
                center.offset(radius, -4, radius)
        )) {
            boolean foundEmpty = false;
            for (int y = 0; y <= 8; y++) {
                cursor.setWithOffset(column, 0, y, 0);
                BlockState state = level.getBlockState(cursor);
                if (foundEmpty
                        && canCollapse(state)
                        && cursor.distSqr(center) < radiusSquared
                        && level.random.nextDouble() < StructuralIntegrityConfig.COLLAPSE_PROPAGATE_CHANCE.get()
                        && collapseBlock(level, cursor, state, true)) {
                    frontier.add(cursor.above().immutable());
                    break;
                }
                foundEmpty = !state.isCollisionShapeFullBlock(level, cursor);
            }
        }
        return addCollapseRun(level, center, frontier, radiusSquared);
    }

    private static boolean addCollapseRun(
            ServerLevel level,
            BlockPos center,
            List<BlockPos> frontier,
            double radiusSquared
    ) {
        if (frontier.isEmpty()) {
            return false;
        }
        CollapseRun run = new CollapseRun(center, frontier, radiusSquared);
        if (!work(level).addCollapse(run)) {
            return false;
        }
        NeoForge.EVENT_BUS.post(new StructuralCollapseEvent(level, center, frontier, radiusSquared, false));
        sendCameraShake(
                level,
                center,
                StructuralIntegrityConfig.COLLAPSE_SHAKE_STRENGTH.get(),
                StructuralIntegrityConfig.COLLAPSE_SHAKE_DURATION.get()
        );
        return true;
    }

    private static int processCollapseRuns(ServerLevel level, StructuralSavedData data, int budget) {
        int processed = 0;
        Iterator<CollapseRun> iterator = data.collapses().iterator();
        while (iterator.hasNext() && processed < budget) {
            CollapseRun collapse = iterator.next();
            int generationChecks = Math.min(collapse.currentSize(), budget - processed);
            for (int index = 0; index < generationChecks; index++) {
                BlockPos pos = collapse.pollCurrent();
                processed++;
                if (!chunkLoaded(level, pos)) {
                    collapse.deferCurrent(pos);
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                if (canCollapse(state)
                        && StructuralFallingBlockEntity.canFallThrough(
                                level,
                                pos.below(),
                                Direction.DOWN,
                                state
                        )
                        && pos.distSqr(collapse.center()) < collapse.radiusSquared()
                        && level.random.nextDouble() < StructuralIntegrityConfig.COLLAPSE_PROPAGATE_CHANCE.get()
                        && collapseBlock(level, pos, state, false)) {
                    data.addNext(collapse, pos.above());
                }
            }
            if (!collapse.hasCurrentFrontier()) {
                boolean continues = collapse.finishGeneration();
                if (!continues) {
                    iterator.remove();
                } else {
                    level.playSound(
                            null,
                            collapse.center(),
                            StructuralIntegrityFeature.COLLAPSE_IMPACT.get(),
                            SoundSource.BLOCKS,
                            0.6F,
                            1.0F
                    );
                    sendCameraShake(
                            level,
                            collapse.center(),
                            StructuralIntegrityConfig.PROPAGATION_SHAKE_STRENGTH.get(),
                            StructuralIntegrityConfig.PROPAGATION_SHAKE_DURATION.get()
                    );
                }
            }
            data.collapseChanged();
        }
        return processed;
    }

    private static boolean collapseBlock(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            boolean destroyBlockBelow
    ) {
        if (!canCollapse(state)) {
            return false;
        }
        if (destroyBlockBelow) {
            BlockPos below = pos.below();
            BlockState belowState = level.getBlockState(below);
            if (!StructuralFallingBlockEntity.canFallThrough(
                    level,
                    below,
                    Direction.DOWN,
                    net.minecraft.world.level.block.Blocks.BEDROCK.defaultBlockState()
            ) && belowState.getDestroySpeed(level, below) >= 0.0F) {
                level.destroyBlock(below, true);
            }
        }
        BlockState result = transformation(level, state, StructuralIntegrityFeature.COLLAPSE_TYPE.get())
                .orElse(state);
        level.setBlockAndUpdate(pos, result);
        StructuralFallingBlockEntity.fall(
                level,
                pos,
                result,
                StructuralIntegrityConfig.COLLAPSE_DAMAGE_PER_BLOCK.get().floatValue(),
                StructuralIntegrityConfig.COLLAPSE_MAX_DAMAGE.get()
        );
        return true;
    }

    private static boolean canCollapse(BlockState state) {
        return state.is(StructuralIntegrityTags.CAN_COLLAPSE);
    }

    private static void processLandslide(ServerLevel level, BlockPos pos) {
        if (!chunkLoaded(level, pos)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.is(StructuralIntegrityTags.CAN_LANDSLIDE)
                || SupportService.isSupported(level, pos)
                || heldBySurroundings(level, pos)) {
            return;
        }
        BlockPos destination = findLandslideDestination(level, pos, state);
        if (destination == null) {
            return;
        }
        BlockState result = transformation(level, state, StructuralIntegrityFeature.LANDSLIDE_TYPE.get())
                .orElse(state);
        if (!destination.equals(pos)) {
            level.removeBlock(pos, false);
            BlockState occupied = level.getBlockState(destination);
            if (!occupied.isAir() && !occupied.getFluidState().isEmpty()) {
                level.destroyBlock(destination, true);
            }
        }
        level.setBlock(destination, result, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        level.playSound(
                null,
                pos,
                StructuralIntegrityFeature.LANDSLIDE_IMPACT.get(),
                SoundSource.BLOCKS,
                0.4F,
                1.0F
        );
        StructuralFallingBlockEntity.fall(
                level,
                destination,
                result,
                StructuralIntegrityConfig.LANDSLIDE_DAMAGE_PER_BLOCK.get().floatValue(),
                StructuralIntegrityConfig.LANDSLIDE_MAX_DAMAGE.get()
        );
    }

    private static boolean heldBySurroundings(ServerLevel level, BlockPos pos) {
        if (isSupportedOnSide(level, pos, Direction.UP)) {
            return true;
        }
        int supportingSides = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (isSupportedOnSide(level, pos, direction) && ++supportingSides >= 2) {
                return true;
            }
        }
        return false;
    }

    private static BlockPos findLandslideDestination(ServerLevel level, BlockPos pos, BlockState fallingState) {
        if (StructuralFallingBlockEntity.canFallThrough(level, pos.below(), Direction.DOWN, fallingState)) {
            return pos;
        }
        List<BlockPos> possible = new ArrayList<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos side = pos.relative(direction);
            if (!isSupportedOnSide(level, pos, direction)
                    && StructuralFallingBlockEntity.canFallThrough(level, side, direction, fallingState)
                    && StructuralFallingBlockEntity.canFallThrough(
                            level,
                            side.below(),
                            Direction.DOWN,
                            fallingState
                    )) {
                possible.add(side);
            }
        }
        return possible.isEmpty() ? null : possible.get(level.random.nextInt(possible.size()));
    }

    private static boolean isSupportedOnSide(ServerLevel level, BlockPos pos, Direction side) {
        BlockPos sidePos = pos.relative(side);
        BlockState sideState = level.getBlockState(sidePos);
        return sideState.isFaceSturdy(level, sidePos, side.getOpposite())
                || sideState.is(StructuralIntegrityTags.SUPPORTS_LANDSLIDE);
    }

    private static Optional<BlockState> transformation(
            ServerLevel level,
            BlockState state,
            net.minecraft.world.item.crafting.RecipeType<BlockTransformationRecipe> type
    ) {
        return level.getRecipeManager().getAllRecipesFor(type).stream()
                .map(net.minecraft.world.item.crafting.RecipeHolder::value)
                .filter(recipe -> recipe.matches(state))
                .map(BlockTransformationRecipe::result)
                .findFirst();
    }

    private static List<BlockPos> uniqueRandomSample(List<BlockPos> candidates, RandomSource random) {
        if (candidates.size() < 4) {
            return List.copyOf(candidates);
        }
        int count = Math.min(12, 3 + random.nextInt(candidates.size() - 3));
        List<BlockPos> remaining = new ArrayList<>(candidates);
        List<BlockPos> selected = new ArrayList<>(count);
        while (selected.size() < count) {
            selected.add(remaining.remove(random.nextInt(remaining.size())));
        }
        return List.copyOf(selected);
    }

    private static void enqueueLandslidesAround(ServerLevel level, BlockPos center) {
        for (Direction direction : Direction.values()) {
            enqueueLandslide(level, center.relative(direction));
        }
    }

    private static void enqueueLandslide(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).is(StructuralIntegrityTags.CAN_LANDSLIDE)) {
            work(level).enqueueLandslide(
                    pos,
                    level.getGameTime() + StructuralIntegrityConfig.LANDSLIDE_DELAY.get()
            );
        }
    }

    static void scheduleLandslide(ServerLevel level, BlockPos pos) {
        enqueueLandslide(level, pos);
    }

    private static StructuralSavedData work(ServerLevel level) {
        return StructuralSavedData.get(level);
    }

    private static boolean chunkLoaded(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static void sendCameraShake(
            ServerLevel level,
            BlockPos origin,
            double strength,
            int durationTicks
    ) {
        double radius = StructuralIntegrityConfig.CAMERA_SHAKE_RADIUS.get();
        if (radius <= 0.0D || strength <= 0.0D || durationTicks <= 0) {
            return;
        }
        PacketDistributor.sendToPlayersNear(
                level,
                null,
                origin.getX() + 0.5D,
                origin.getY() + 0.5D,
                origin.getZ() + 0.5D,
                radius,
                new CollapseShakePayload(
                        origin,
                        (float) strength,
                        durationTicks,
                        (float) radius
                )
        );
    }
}
