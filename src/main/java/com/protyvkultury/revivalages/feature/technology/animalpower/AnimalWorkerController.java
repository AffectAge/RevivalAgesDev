package com.protyvkultury.revivalages.feature.technology.animalpower;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

/** Owns the server-authoritative worker attachment and bounded waypoint lifecycle. */
public final class AnimalWorkerController {

    private UUID workerId;
    private int waypointIndex;
    private int retryTicks;

    public Optional<UUID> workerId() {
        return Optional.ofNullable(workerId);
    }

    public int waypointIndex() {
        return waypointIndex;
    }

    public boolean attach(ServerLevel level, BlockPos machinePos, Player player) {
        Optional<Mob> candidate = level.getEntitiesOfClass(
                        Mob.class,
                        new AABB(machinePos).inflate(16.0D),
                        mob -> mob.getType().is(AnimalPowerTags.WORKERS)
                                && player.equals(mob.getLeashHolder()))
                .stream()
                .min(Comparator.comparingDouble(mob -> mob.distanceToSqr(player)));
        if (candidate.isEmpty()) {
            return false;
        }
        detach(level, machinePos, false);
        Mob worker = candidate.get();
        worker.dropLeash(false, false);
        workerId = worker.getUUID();
        waypointIndex = nearestWaypoint(worker, machinePos);
        retryTicks = 0;
        return true;
    }

    public void detach(ServerLevel level, BlockPos machinePos, boolean returnLead) {
        resolve(level).ifPresent(worker -> worker.getNavigation().stop());
        if (workerId != null && returnLead) {
            level.addFreshEntity(new ItemEntity(
                    level,
                    machinePos.getX() + 0.5D,
                    machinePos.getY() + 1.0D,
                    machinePos.getZ() + 0.5D,
                    new ItemStack(Items.LEAD)
            ));
        }
        workerId = null;
        waypointIndex = 0;
        retryTicks = 0;
    }

    public boolean tick(ServerLevel level, BlockPos machinePos, boolean shouldMove) {
        if (workerId == null) {
            return false;
        }
        if (retryTicks > 0 && retryTicks < AnimalPowerConfig.WORKER_RETRY_INTERVAL.get()) {
            retryTicks++;
            return false;
        }
        Optional<Mob> resolved = resolve(level);
        if (resolved.isEmpty()) {
            retryTicks = 1;
            return false;
        }
        retryTicks = 0;
        Mob worker = resolved.get();
        if (!worker.isAlive() || !worker.getType().is(AnimalPowerTags.WORKERS)) {
            detach(level, machinePos, true);
            return false;
        }
        if (!shouldMove) {
            worker.getNavigation().stop();
            return false;
        }
        BlockPos target = AnimalWorkArea.waypoint(machinePos, waypointIndex);
        double reach = AnimalPowerConfig.WAYPOINT_REACH_DISTANCE.get();
        if (worker.distanceToSqr(
                target.getX() + 0.5D,
                target.getY(),
                target.getZ() + 0.5D) <= reach * reach) {
            waypointIndex = (waypointIndex + 1) % AnimalWorkArea.waypointCount();
            target = AnimalWorkArea.waypoint(machinePos, waypointIndex);
            worker.getNavigation().moveTo(
                    target.getX() + 0.5D,
                    target.getY(),
                    target.getZ() + 0.5D,
                    AnimalPowerConfig.WORKER_SPEED.get()
            );
            return true;
        }
        if (worker.getNavigation().isDone()
                || level.getGameTime() % AnimalPowerConfig.NAVIGATION_REFRESH_INTERVAL.get() == 0L) {
            worker.getNavigation().moveTo(
                    target.getX() + 0.5D,
                    target.getY(),
                    target.getZ() + 0.5D,
                    AnimalPowerConfig.WORKER_SPEED.get()
            );
        }
        return false;
    }

    public Optional<Mob> resolve(ServerLevel level) {
        if (workerId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(level.getEntity(workerId))
                .filter(Mob.class::isInstance)
                .map(Mob.class::cast);
    }

    public void load(CompoundTag tag) {
        workerId = tag.hasUUID("Worker") ? tag.getUUID("Worker") : null;
        waypointIndex = Math.floorMod(tag.getInt("Waypoint"), AnimalWorkArea.waypointCount());
        retryTicks = Math.max(0, tag.getInt("WorkerRetry"));
    }

    public void save(CompoundTag tag) {
        if (workerId != null) {
            tag.putUUID("Worker", workerId);
        }
        tag.putInt("Waypoint", waypointIndex);
        tag.putInt("WorkerRetry", retryTicks);
    }

    private static int nearestWaypoint(Mob worker, BlockPos machinePos) {
        int nearest = 0;
        double nearestDistance = Double.MAX_VALUE;
        for (int index = 0; index < AnimalWorkArea.waypointCount(); index++) {
            BlockPos waypoint = AnimalWorkArea.waypoint(machinePos, index);
            double distance = worker.distanceToSqr(
                    waypoint.getX() + 0.5D,
                    waypoint.getY(),
                    waypoint.getZ() + 0.5D);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = index;
            }
        }
        return nearest;
    }
}
