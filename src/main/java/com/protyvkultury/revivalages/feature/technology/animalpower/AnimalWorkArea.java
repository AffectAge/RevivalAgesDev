package com.protyvkultury.revivalages.feature.technology.animalpower;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

/** Validates and describes the bounded path around an animal-powered device. */
public final class AnimalWorkArea {

    public static final int RADIUS = 3;
    private AnimalWorkArea() {
    }

    public static boolean isValid(LevelReader level, BlockPos machinePos, boolean tallMachine) {
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                    continue;
                }
                if (!level.getBlockState(machinePos.offset(x, 0, z)).canBeReplaced()) {
                    return false;
                }
                int clearanceOffset = tallMachine ? 1 : -1;
                if (!level.getBlockState(machinePos.offset(x, clearanceOffset, z)).canBeReplaced()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static BlockPos waypoint(BlockPos machinePos, AnimalMachineKind kind, int index) {
        AnimalWaypointCircuit.Offset offset = AnimalWaypointCircuit.offset(index);
        return machinePos.offset(offset.x(), kind.workerPathYOffset(), offset.z());
    }

    public static int waypointCount() {
        return AnimalWaypointCircuit.size();
    }
}
