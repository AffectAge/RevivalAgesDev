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

    private static final double WORKER_SEARCH_RADIUS = 7.0D;
    private static final double MAX_WORKER_DISTANCE_SQUARED = 45.0D;
    private UUID workerId;
    private int waypointIndex;
    private int retryTicks;
    private boolean running = true;
    private boolean wasRunning;
    private int origin = -1;

    public Optional<UUID> workerId() {
        return Optional.ofNullable(workerId);
    }

    public int waypointIndex() {
        return waypointIndex;
    }

    public boolean attach(ServerLevel level, BlockPos machinePos, AnimalMachineKind kind, Player player) {
        if (workerId != null) {
            return false;
        }
        Optional<Mob> candidate = level.getEntitiesOfClass(
                        Mob.class,
                        new AABB(
                                machinePos.getX() - WORKER_SEARCH_RADIUS,
                                machinePos.getY() - WORKER_SEARCH_RADIUS,
                                machinePos.getZ() - WORKER_SEARCH_RADIUS,
                                machinePos.getX() + WORKER_SEARCH_RADIUS,
                                machinePos.getY() + WORKER_SEARCH_RADIUS,
                                machinePos.getZ() + WORKER_SEARCH_RADIUS
                        ),
                        mob -> mob.getType().is(AnimalPowerTags.WORKERS)
                                && player.equals(mob.getLeashHolder()))
                .stream()
                .min(Comparator.comparingDouble(mob -> mob.distanceToSqr(player)));
        if (candidate.isEmpty()) {
            return false;
        }
        Mob worker = candidate.get();
        // The machine supplies its own tether renderer. Broadcast removal of the
        // player's vanilla tether so clients do not render both connections.
        worker.dropLeash(true, false);
        // Horse Power records a home radius here, but its explicit navigator route is
        // not constrained by that home. Modern restrictions do constrain navigation,
        // so keeping restrictTo() would change the original behavior.
        workerId = worker.getUUID();
        waypointIndex = nearestWaypoint(worker, machinePos, kind);
        retryTicks = 0;
        running = true;
        wasRunning = false;
        origin = -1;
        return true;
    }

    public void detach(ServerLevel level, BlockPos machinePos, boolean returnLead) {
        resolve(level).ifPresent(worker -> {
            worker.getNavigation().stop();
            worker.clearRestriction();
        });
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
        running = true;
        wasRunning = false;
        origin = -1;
    }

    public boolean releaseToPlayer(ServerLevel level, BlockPos machinePos, Player player) {
        Optional<Mob> resolved = resolve(level);
        if (resolved.isEmpty()) {
            detach(level, machinePos, true);
            return false;
        }
        Mob worker = resolved.get();
        if (!worker.isAlive() || !worker.canBeLeashed()) {
            detach(level, machinePos, true);
            return false;
        }
        worker.getNavigation().stop();
        worker.clearRestriction();
        worker.setLeashedTo(player, true);
        workerId = null;
        waypointIndex = 0;
        retryTicks = 0;
        running = true;
        wasRunning = false;
        origin = -1;
        return true;
    }

    public boolean tick(ServerLevel level, BlockPos machinePos, AnimalMachineKind kind, boolean canWork) {
        if (workerId == null) {
            return false;
        }

        Optional<Mob> resolved = resolve(level);
        if (resolved.isEmpty()) {
            if (retryTicks <= 0) {
                retryTicks = AnimalPowerConfig.WORKER_RETRY_INTERVAL.get();
            } else {
                retryTicks--;
            }
            return false;
        }
        retryTicks = 0;
        Mob worker = resolved.get();

        // Horse Power's hasWorker(): an attached worker is valid only while alive,
        // not vanilla-leashed, and within distanceSq < 45 from the machine.
        if (!worker.isAlive()
                || !worker.getType().is(AnimalPowerTags.WORKERS)
                || worker.isLeashed()
                || worker.distanceToSqr(machinePos.getX(), machinePos.getY(), machinePos.getZ())
                        >= MAX_WORKER_DISTANCE_SQUARED) {
            detach(level, machinePos, true);
            return false;
        }

        // Mirror TileEntityHPHorseBase's running/wasRunning transition exactly.
        if (!running && canWork) {
            running = true;
        } else if (running && !canWork) {
            running = false;
        }
        if (running != wasRunning) {
            waypointIndex = nearestWaypoint(worker, machinePos, kind);
            wasRunning = running;
        }
        if (!running) {
            return false;
        }

        BlockPos target = AnimalWorkArea.waypoint(machinePos, kind, waypointIndex);
        AABB searchArea = new AABB(
                target.getX() - 0.5D, target.getY() - 0.5D, target.getZ() - 0.5D,
                target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);

        boolean reached = false;
        if (worker.getBoundingBox().intersects(searchArea)) {
            if (origin != waypointIndex) {
                origin = waypointIndex;
                reached = true;
            }
            waypointIndex = (waypointIndex + 1) % AnimalWorkArea.waypointCount();
            target = AnimalWorkArea.waypoint(machinePos, kind, waypointIndex);
        }

        // Horse Power only asks the navigator for a route when it has no path.
        // Do not impose a modern Mob restriction here: the original home radius
        // did not constrain its explicit tryMoveToXYZ route.
        if (worker.getNavigation().isDone()) {
            worker.getNavigation().moveTo(
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    1.0D
            );
        }
        return reached;
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
        running = true;
        wasRunning = false;
        origin = -1;
    }

    public void save(CompoundTag tag) {
        if (workerId != null) {
            tag.putUUID("Worker", workerId);
        }
        tag.putInt("Waypoint", waypointIndex);
        tag.putInt("WorkerRetry", retryTicks);
    }

    private static int nearestWaypoint(Mob worker, BlockPos machinePos, AnimalMachineKind kind) {
        int nearest = 0;
        double nearestDistance = Double.MAX_VALUE;
        for (int index = 0; index < AnimalWorkArea.waypointCount(); index++) {
            BlockPos waypoint = AnimalWorkArea.waypoint(machinePos, kind, index);
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
