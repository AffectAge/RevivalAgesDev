package com.protyvkultury.revivalages.feature.content;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ContentDependencyGraphTest {

    @Test
    void acceptsBranchesAndSharedAncestors() {
        Map<String, Set<String>> graph = Map.of(
                "root", Set.of(),
                "left", Set.of("root"),
                "right", Set.of("root"),
                "leaf", Set.of("left", "right")
        );

        assertDoesNotThrow(() -> ContentDependencyGraph.validate(graph));
    }

    @Test
    void rejectsCycles() {
        Map<String, Set<String>> graph = Map.of(
                "a", Set.of("b"),
                "b", Set.of("c"),
                "c", Set.of("a")
        );

        assertThrows(IllegalStateException.class, () -> ContentDependencyGraph.validate(graph));
    }

    @Test
    void rejectsMissingParents() {
        Map<String, Set<String>> graph = Map.of("child", Set.of("missing"));

        assertThrows(IllegalStateException.class, () -> ContentDependencyGraph.validate(graph));
    }
}
