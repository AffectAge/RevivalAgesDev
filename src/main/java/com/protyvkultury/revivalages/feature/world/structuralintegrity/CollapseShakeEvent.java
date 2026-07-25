package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.Event;

/**
 * Client-side presentation event emitted after a validated collapse shake
 * payload is received.
 */
public final class CollapseShakeEvent extends Event {

    private final BlockPos origin;
    private final float strength;
    private final int durationTicks;
    private final float radius;

    public CollapseShakeEvent(BlockPos origin, float strength, int durationTicks, float radius) {
        this.origin = origin.immutable();
        this.strength = strength;
        this.durationTicks = durationTicks;
        this.radius = radius;
    }

    public BlockPos origin() {
        return origin;
    }

    public float strength() {
        return strength;
    }

    public int durationTicks() {
        return durationTicks;
    }

    public float radius() {
        return radius;
    }
}
