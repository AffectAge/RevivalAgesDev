package com.protyvkultury.revivalages.core.process;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;

/** Persisted counters owned by stateful process rules, such as weather exposure. */
public final class ProcessRuleState {

    private final Map<ProcessRuleType, Integer> counters = new EnumMap<>(ProcessRuleType.class);

    public int counter(ProcessRuleType type) {
        return counters.getOrDefault(type, 0);
    }

    public void setCounter(ProcessRuleType type, int value) {
        if (value <= 0) {
            counters.remove(type);
        } else {
            counters.put(type, value);
        }
    }

    public int incrementUntil(ProcessRuleType type, int limit) {
        int current = counter(type);
        if (limit < 0 || current >= limit) {
            return current;
        }
        int next = current + 1;
        setCounter(type, next);
        return next;
    }

    public void clear() {
        counters.clear();
    }

    public void load(CompoundTag tag) {
        counters.clear();
        for (ProcessRuleType type : ProcessRuleType.values()) {
            int value = tag.getInt(type.getSerializedName());
            if (value > 0) {
                counters.put(type, value);
            }
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        counters.forEach((type, value) -> tag.putInt(type.getSerializedName(), value));
        return tag;
    }
}
