package com.protyvkultury.revivalages.feature.technology.campfire;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

final class CampfireVisualContractTest {

    @Test
    void campfireDoesNotUseVanillaCampfireModels() {
        String blockState = text("assets/revivalages/blockstates/campfire.json");

        assertFalse(blockState.contains("minecraft:block/campfire"));
        assertTrue(blockState.contains("revivalages:block/campfire_tinder"));
        assertTrue(blockState.contains("revivalages:block/campfire_active"));
        assertTrue(blockState.contains("revivalages:block/campfire_fire"));
    }

    @Test
    void allAshLevelsHavePhysicalModels() {
        String blockState = text("assets/revivalages/blockstates/campfire.json");

        for (int level = 1; level <= 8; level++) {
            String model = "revivalages:block/campfire_ash_" + level;
            assertTrue(blockState.contains(model), model);
            assertResource("assets/revivalages/models/block/campfire_ash_" + level + ".json");
        }
    }

    @Test
    void tinderAndFireUseTheAdaptedVisualLayers() {
        String tinder = text("assets/revivalages/models/block/campfire_tinder.json");
        String active = text("assets/revivalages/models/block/campfire_active.json");
        String fire = text("assets/revivalages/models/block/campfire_fire.json");

        assertTrue(tinder.contains("\"tinder\": \"revivalages:block/tinder\""));
        assertTrue(active.contains("\"tinder\": \"revivalages:block/active_pile\""));
        assertTrue(fire.contains("\"fire\": \"minecraft:block/fire_1\""));
        assertResource("assets/revivalages/textures/block/tinder.png");
    }

    private static void assertResource(String path) {
        assertNotNull(CampfireVisualContractTest.class.getClassLoader().getResource(path), path);
    }

    private static String text(String path) {
        try (InputStream input = CampfireVisualContractTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new AssertionError("Unable to read " + path, exception);
        }
    }
}
