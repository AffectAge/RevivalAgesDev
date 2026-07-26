package com.protyvkultury.revivalages.feature.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;

class ContentStateResolverTest {

    @Test
    void effectiveStateRequiresChildAndEveryParent() {
        Map<String, Boolean> values = new LinkedHashMap<>();
        values.put("family", false);
        values.put("machine", true);
        ContentStateResolver<String> resolver = resolver(values, Map.of(
                "family", Set.of(),
                "machine", Set.of("family")
        ));

        assertFalse(resolver.isEnabled("machine"));
        assertEquals(List.of("family"), resolver.conflicts().get("machine"));
    }

    @Test
    void disabledChildIsNotAConflict() {
        Map<String, Boolean> values = new LinkedHashMap<>();
        values.put("family", true);
        values.put("machine", false);
        ContentStateResolver<String> resolver = resolver(values, Map.of(
                "family", Set.of(),
                "machine", Set.of("family")
        ));

        assertFalse(resolver.isEnabled("machine"));
        assertTrue(resolver.conflicts().isEmpty());
    }

    @Test
    void rejectsDifferentConfiguredAndDependencyKeys() {
        assertThrows(IllegalStateException.class, () -> new ContentStateResolver<>(
                Map.of("only_configured", () -> true),
                Map.of("only_parented", Set.of())
        ));
    }

    private static ContentStateResolver<String> resolver(
            Map<String, Boolean> values,
            Map<String, Set<String>> parents
    ) {
        Map<String, BooleanSupplier> suppliers = new LinkedHashMap<>();
        values.forEach((key, value) -> suppliers.put(key, () -> value));
        return new ContentStateResolver<>(suppliers, parents);
    }
}
