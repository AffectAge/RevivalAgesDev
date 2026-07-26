package com.protyvkultury.revivalages.feature.inventory.carriedweight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

final class PrioritizedProviderRegistry<T> {

    private final List<Entry<T>> entries = new ArrayList<>();
    private final Set<ResourceLocation> ids = new HashSet<>();
    private boolean frozen;

    void register(ResourceLocation id, int priority, T value) {
        if (frozen) {
            throw new IllegalStateException("Carried Weight provider registration is already closed");
        }
        Objects.requireNonNull(id);
        Objects.requireNonNull(value);
        if (!ids.add(id)) {
            throw new IllegalArgumentException("Duplicate Carried Weight provider: " + id);
        }
        entries.add(new Entry<>(id, priority, value));
    }

    void freeze() {
        entries.sort(Comparator
                .comparingInt((Entry<T> entry) -> entry.priority()).reversed()
                .thenComparing(Entry::id));
        frozen = true;
    }

    List<Entry<T>> entries() {
        return List.copyOf(entries);
    }

    record Entry<T>(ResourceLocation id, int priority, T value) {
    }
}
