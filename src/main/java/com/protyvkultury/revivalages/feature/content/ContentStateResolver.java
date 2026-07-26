package com.protyvkultury.revivalages.feature.content;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

/** Pure dependency resolver used by the platform-facing availability catalog. */
public final class ContentStateResolver<T> {

    private final Map<T, BooleanSupplier> configured;
    private final Map<T, Set<T>> parents;

    public ContentStateResolver(
            Map<T, BooleanSupplier> configured,
            Map<T, ? extends Set<T>> parents
    ) {
        this.configured = Map.copyOf(configured);
        Map<T, Set<T>> copiedParents = new LinkedHashMap<>();
        parents.forEach((key, value) -> copiedParents.put(key, Set.copyOf(value)));
        this.parents = Collections.unmodifiableMap(copiedParents);
        if (!this.configured.keySet().equals(this.parents.keySet())) {
            throw new IllegalStateException("Configured content and dependency keys differ");
        }
        ContentDependencyGraph.validate(this.parents);
    }

    public boolean isEnabled(T key) {
        if (!configured.containsKey(key)) {
            throw new IllegalArgumentException("Unknown content key: " + key);
        }
        return isEnabled(key, new java.util.HashSet<>());
    }

    public boolean configuredEnabled(T key) {
        BooleanSupplier supplier = configured.get(key);
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown content key: " + key);
        }
        return supplier.getAsBoolean();
    }

    public Map<T, List<T>> conflicts() {
        Map<T, List<T>> result = new LinkedHashMap<>();
        for (T key : configured.keySet()) {
            if (!configuredEnabled(key)) {
                continue;
            }
            List<T> disabledParents = parents.get(key).stream()
                    .filter(parent -> !isEnabled(parent))
                    .toList();
            if (!disabledParents.isEmpty()) {
                result.put(key, disabledParents);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private boolean isEnabled(T key, Set<T> active) {
        if (!configuredEnabled(key)) {
            return false;
        }
        if (!active.add(key)) {
            throw new IllegalStateException("Content dependency cycle involving " + key);
        }
        try {
            return parents.get(key).stream().allMatch(parent -> isEnabled(parent, active));
        } finally {
            active.remove(key);
        }
    }
}
