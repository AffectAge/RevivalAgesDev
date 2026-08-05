package com.protyvkultury.revivalages.feature.technology.knapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

final class KnappingResourceContractTest {

    @Test
    void splitterKnappingUsesTheDedicatedMaterialTag() {
        String type = text("data/revivalages/revivalages/knapping_type/rock.json");
        String recipe = text("data/revivalages/recipe/stone_saw_blade_knapping.json");
        String tag = text("data/revivalages/tags/item/knapping_splitters.json");

        assertTrue(type.contains("\"tag\": \"revivalages:knapping_splitters\""));
        assertTrue(recipe.contains("\"tag\": \"revivalages:knapping_splitters\""));
        assertFalse(type.contains("\"item\": \"revivalages:rock\""));
        assertFalse(recipe.contains("\"item\": \"revivalages:rock\""));
        assertEquals(9, occurrences(tag, "_splitter\""));
    }

    @Test
    void menuCoordinatesMatchTheCompleteBackground() {
        assertEquals(176, KnappingLayout.WIDTH);
        assertEquals(186, KnappingLayout.HEIGHT);
        assertEquals(128, KnappingLayout.OUTPUT_X);
        assertEquals(46, KnappingLayout.OUTPUT_Y);
        assertEquals(104, KnappingLayout.PLAYER_INVENTORY_Y);
        assertEquals(162, KnappingLayout.HOTBAR_Y);
        assertResource("assets/revivalages/textures/gui/knapping_screen.png");
    }

    private static int occurrences(String value, String needle) {
        return value.split(Pattern.quote(needle), -1).length - 1;
    }

    private static void assertResource(String path) {
        assertNotNull(KnappingResourceContractTest.class.getClassLoader().getResource(path), path);
    }

    private static String text(String path) {
        try (InputStream stream = KnappingResourceContractTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new AssertionError("Unable to read " + path, exception);
        }
    }
}
