package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class StructuralSavedData extends SavedData {

    private static final int DATA_VERSION = 2;
    private static final String DATA_NAME = "revivalages_structural_integrity";
    private static final Factory<StructuralSavedData> FACTORY =
            new Factory<>(StructuralSavedData::new, StructuralSavedData::load);

    private final List<CollapseRun> collapses = new ArrayList<>();
    private final Queue<DelayedPosition> landslides = new ArrayDeque<>();
    private final Set<Long> knownLandslides = new HashSet<>();

    public static StructuralSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    boolean addCollapse(CollapseRun collapse) {
        if (collapse.queuedPositions() == 0
                || size() + collapse.queuedPositions() > StructuralIntegrityConfig.MAX_QUEUED_UPDATES.get()) {
            return false;
        }
        collapses.add(collapse);
        setDirty();
        return true;
    }

    boolean addNext(CollapseRun collapse, BlockPos pos) {
        if (size() >= StructuralIntegrityConfig.MAX_QUEUED_UPDATES.get()) {
            return false;
        }
        boolean added = collapse.addNext(pos);
        if (added) {
            setDirty();
        }
        return added;
    }

    List<CollapseRun> collapses() {
        return collapses;
    }

    void collapseChanged() {
        setDirty();
    }

    public boolean enqueueLandslide(BlockPos pos, long dueTick) {
        long packed = pos.asLong();
        if (size() >= StructuralIntegrityConfig.MAX_QUEUED_UPDATES.get() || !knownLandslides.add(packed)) {
            return false;
        }
        landslides.add(new DelayedPosition(packed, dueTick));
        setDirty();
        return true;
    }

    public BlockPos pollReadyLandslide(long gameTime) {
        DelayedPosition next = landslides.peek();
        if (next == null || next.dueTick() > gameTime) {
            return null;
        }
        landslides.remove();
        knownLandslides.remove(next.position());
        setDirty();
        return BlockPos.of(next.position());
    }

    public int size() {
        return collapses.stream().mapToInt(CollapseRun::queuedPositions).sum() + landslides.size();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("version", DATA_VERSION);
        ListTag collapseTags = new ListTag();
        collapses.forEach(collapse -> collapseTags.add(collapse.save(registries)));
        tag.put("collapse_runs", collapseTags);
        tag.putLongArray(
                "landslide_positions",
                landslides.stream().mapToLong(DelayedPosition::position).toArray()
        );
        tag.putLongArray(
                "landslide_due_ticks",
                landslides.stream().mapToLong(DelayedPosition::dueTick).toArray()
        );
        return tag;
    }

    private static StructuralSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        StructuralSavedData data = new StructuralSavedData();
        int version = tag.getInt("version");
        if (version >= DATA_VERSION) {
            ListTag collapseTags = tag.getList("collapse_runs", Tag.TAG_COMPOUND);
            for (Tag entry : collapseTags) {
                CollapseRun run = CollapseRun.load((CompoundTag) entry, registries);
                if (run.queuedPositions() > 0
                        && data.size() + run.queuedPositions()
                        <= StructuralIntegrityConfig.MAX_QUEUED_UPDATES.get()) {
                    data.collapses.add(run);
                }
            }
        }
        long[] landslidePositions = tag.getLongArray("landslide_positions");
        long[] landslideTicks = tag.getLongArray("landslide_due_ticks");
        int size = Math.min(landslidePositions.length, landslideTicks.length);
        for (int index = 0; index < size; index++) {
            long packed = landslidePositions[index];
            if (data.size() >= StructuralIntegrityConfig.MAX_QUEUED_UPDATES.get()) {
                break;
            }
            if (data.knownLandslides.add(packed)) {
                data.landslides.add(new DelayedPosition(packed, landslideTicks[index]));
            }
        }
        if (version < DATA_VERSION) {
            data.setDirty();
        }
        return data;
    }

    private record DelayedPosition(long position, long dueTick) {
    }
}
