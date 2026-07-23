package com.protyvkultury.revivalages.feature.technology.animalpower;

import java.util.List;

/** Immutable, loader-neutral definition of the eight-point worker circuit. */
public final class AnimalWaypointCircuit {

    private static final List<Offset> OFFSETS = List.of(
            new Offset(-3, -3),
            new Offset(0, -3),
            new Offset(3, -3),
            new Offset(3, 0),
            new Offset(3, 3),
            new Offset(0, 3),
            new Offset(-3, 3),
            new Offset(-3, 0)
    );

    private AnimalWaypointCircuit() {
    }

    public static Offset offset(int index) {
        return OFFSETS.get(Math.floorMod(index, OFFSETS.size()));
    }

    public static int size() {
        return OFFSETS.size();
    }

    public record Offset(int x, int z) {
    }
}
