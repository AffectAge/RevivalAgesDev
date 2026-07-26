package com.protyvkultury.revivalages.feature.content;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Small reusable validator for content parent declarations. */
public final class ContentDependencyGraph {

    private ContentDependencyGraph() {
    }

    public static <T> void validate(Map<T, ? extends Set<T>> parents) {
        Set<T> complete = new HashSet<>();
        Set<T> active = new HashSet<>();
        for (T key : parents.keySet()) {
            validate(key, parents, active, complete);
        }
    }

    private static <T> void validate(
            T key,
            Map<T, ? extends Set<T>> parents,
            Set<T> active,
            Set<T> complete
    ) {
        if (complete.contains(key)) {
            return;
        }
        if (!parents.containsKey(key)) {
            throw new IllegalStateException("Missing content dependency: " + key);
        }
        if (!active.add(key)) {
            throw new IllegalStateException("Content dependency cycle involving " + key);
        }
        for (T parent : parents.get(key)) {
            validate(parent, parents, active, complete);
        }
        active.remove(key);
        complete.add(key);
    }
}
