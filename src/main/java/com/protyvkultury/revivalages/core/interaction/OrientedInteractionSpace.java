package com.protyvkultury.revivalages.core.interaction;

import net.minecraft.core.Direction;

/** Coordinate conversion used by horizontally oriented in-world interactions. */
public final class OrientedInteractionSpace {

    private OrientedInteractionSpace() {
    }

    public static Point worldToLocal(Direction facing, double worldX, double worldZ) {
        double x = worldX - 0.5D;
        double z = worldZ - 0.5D;
        return switch (facing) {
            case SOUTH -> new Point(x + 0.5D, z + 0.5D);
            case WEST -> new Point(-z + 0.5D, x + 0.5D);
            case NORTH -> new Point(-x + 0.5D, -z + 0.5D);
            case EAST -> new Point(z + 0.5D, -x + 0.5D);
            default -> throw new IllegalArgumentException("Horizontal facing required: " + facing);
        };
    }

    public record Point(double x, double z) {
    }
}
