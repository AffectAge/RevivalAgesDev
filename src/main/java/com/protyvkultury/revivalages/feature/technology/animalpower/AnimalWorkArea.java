package com.protyvkultury.revivalages.feature.technology.animalpower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;

/** Validates and describes the bounded path around an animal-powered device. */
public final class AnimalWorkArea {

    public static final int RADIUS = 3;
    private AnimalWorkArea() {
    }

    public static boolean isValid(LevelReader level, BlockPos machinePos, boolean tallMachine) {
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                BlockPos floor = machinePos.offset(x, -1, z);
                if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
                    return false;
                }
                if (x == 0 && z == 0) {
                    continue;
                }
                if (!level.getBlockState(machinePos.offset(x, 0, z)).getCollisionShape(
                        level, machinePos.offset(x, 0, z)).isEmpty()) {
                    return false;
                }
                if (!level.getBlockState(machinePos.offset(x, 1, z)).getCollisionShape(
                        level, machinePos.offset(x, 1, z)).isEmpty()) {
                    return false;
                }
            }
        }
        return !tallMachine || level.getBlockState(machinePos.above()).is(
                com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature.HORSE_CHOPPING_BLOCK.get())
                || level.getBlockState(machinePos.above()).is(
                com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature.HORSE_PRESS.get());
    }

    public static BlockPos waypoint(BlockPos machinePos, int index) {
        AnimalWaypointCircuit.Offset offset = AnimalWaypointCircuit.offset(index);
        return machinePos.offset(offset.x(), 0, offset.z());
    }

    public static int waypointCount() {
        return AnimalWaypointCircuit.size();
    }
}
