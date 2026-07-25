package com.protyvkultury.revivalages.api.event;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/**
 * Fired on the server when a structural collapse starts or produces a warning.
 */
public final class StructuralCollapseEvent extends Event {

    private final ServerLevel level;
    private final BlockPos center;
    private final List<BlockPos> initialPositions;
    private final double radiusSquared;
    private final boolean fake;

    public StructuralCollapseEvent(
            ServerLevel level,
            BlockPos center,
            List<BlockPos> initialPositions,
            double radiusSquared,
            boolean fake
    ) {
        this.level = level;
        this.center = center.immutable();
        this.initialPositions = initialPositions.stream().map(BlockPos::immutable).toList();
        this.radiusSquared = radiusSquared;
        this.fake = fake;
    }

    public ServerLevel level() {
        return level;
    }

    public BlockPos center() {
        return center;
    }

    public List<BlockPos> initialPositions() {
        return initialPositions;
    }

    public double radiusSquared() {
        return radiusSquared;
    }

    public boolean fake() {
        return fake;
    }
}
