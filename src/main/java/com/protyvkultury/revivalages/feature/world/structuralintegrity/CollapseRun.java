package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

final class CollapseRun {

    private final BlockPos center;
    private final Queue<Long> frontier;
    private final Set<Long> frontierIndex;
    private final Set<Long> nextFrontier;
    private double radiusSquared;

    CollapseRun(BlockPos center, Collection<BlockPos> frontier, double radiusSquared) {
        this.center = center.immutable();
        this.frontier = new ArrayDeque<>();
        this.frontierIndex = new LinkedHashSet<>();
        this.nextFrontier = new LinkedHashSet<>();
        this.radiusSquared = radiusSquared;
        frontier.forEach(this::addCurrent);
    }

    private CollapseRun(
            BlockPos center,
            Collection<Long> frontier,
            Collection<Long> nextFrontier,
            double radiusSquared
    ) {
        this.center = center.immutable();
        this.frontier = new ArrayDeque<>(frontier);
        this.frontierIndex = new LinkedHashSet<>(frontier);
        this.nextFrontier = new LinkedHashSet<>(nextFrontier);
        this.radiusSquared = radiusSquared;
    }

    BlockPos center() {
        return center;
    }

    double radiusSquared() {
        return radiusSquared;
    }

    int queuedPositions() {
        return frontier.size() + nextFrontier.size();
    }

    boolean hasCurrentFrontier() {
        return !frontier.isEmpty();
    }

    int currentSize() {
        return frontier.size();
    }

    BlockPos pollCurrent() {
        long packed = frontier.remove();
        frontierIndex.remove(packed);
        return BlockPos.of(packed);
    }

    void deferCurrent(BlockPos pos) {
        addCurrent(pos);
    }

    boolean addNext(BlockPos pos) {
        return nextFrontier.add(pos.asLong());
    }

    boolean finishGeneration() {
        if (!frontier.isEmpty()) {
            return true;
        }
        if (nextFrontier.isEmpty()) {
            return false;
        }
        for (long packed : nextFrontier) {
            frontier.add(packed);
            frontierIndex.add(packed);
        }
        nextFrontier.clear();
        radiusSquared *= 0.8D;
        return true;
    }

    CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("center", center.asLong());
        tag.putDouble("radius_squared", radiusSquared);
        tag.putLongArray("frontier", frontier.stream().mapToLong(Long::longValue).toArray());
        tag.putLongArray("next_frontier", nextFrontier.stream().mapToLong(Long::longValue).toArray());
        return tag;
    }

    static CollapseRun load(CompoundTag tag, HolderLookup.Provider registries) {
        java.util.List<Long> frontier = java.util.Arrays.stream(tag.getLongArray("frontier")).boxed().toList();
        java.util.List<Long> next = java.util.Arrays.stream(tag.getLongArray("next_frontier")).boxed().toList();
        return new CollapseRun(
                BlockPos.of(tag.getLong("center")),
                frontier,
                next,
                Math.max(0.0D, tag.getDouble("radius_squared"))
        );
    }

    private void addCurrent(BlockPos pos) {
        long packed = pos.asLong();
        if (frontierIndex.add(packed)) {
            frontier.add(packed);
        }
    }
}
