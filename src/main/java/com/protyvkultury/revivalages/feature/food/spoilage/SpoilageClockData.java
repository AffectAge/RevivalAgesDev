package com.protyvkultury.revivalages.feature.food.spoilage;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class SpoilageClockData extends SavedData {

    private static final String DATA_NAME = "revivalages_food_spoilage_clock";
    private static final Factory<SpoilageClockData> FACTORY =
            new Factory<>(SpoilageClockData::new, SpoilageClockData::load);

    private long ticks;

    public static SpoilageClockData get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public long ticks() {
        return ticks;
    }

    public void advance(long amount) {
        if (amount <= 0L) {
            return;
        }
        ticks = amount >= Long.MAX_VALUE - ticks ? Long.MAX_VALUE : ticks + amount;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("spoilage_ticks", ticks);
        return tag;
    }

    private static SpoilageClockData load(CompoundTag tag, HolderLookup.Provider registries) {
        SpoilageClockData data = new SpoilageClockData();
        data.ticks = Math.max(0L, tag.getLong("spoilage_ticks"));
        return data;
    }
}
