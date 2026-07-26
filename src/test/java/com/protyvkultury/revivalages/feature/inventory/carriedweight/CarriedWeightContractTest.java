package com.protyvkultury.revivalages.feature.inventory.carriedweight;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

final class CarriedWeightContractTest {

    @Test
    void sourceArtifactsContainLicenseAndFunctionalHudAssets() {
        assertResource("assets/revivalages/textures/gui/carried_weight/empty.png");
        for (int stage = 1; stage <= 12; stage++) {
            assertResource("assets/revivalages/textures/gui/carried_weight/filled_" + stage + ".png");
        }
        assertResource("assets/revivalages/textures/gui/carried_weight/overload.png");
        assertResource("assets/revivalages/textures/gui/carried_weight/strength.png");
        assertResource("assets/revivalages/textures/mob_effect/overloaded.png");
    }

    @Test
    void technicalItemsUseAnExtendableExplicitTag() {
        String tag = text("data/revivalages/tags/item/technical_weight_items.json");

        assertTrue(tag.contains("\"replace\": false"));
        assertTrue(tag.contains("\"minecraft:command_block\""));
        assertTrue(tag.contains("\"minecraft:structure_block\""));
    }

    private static void assertResource(String path) {
        assertNotNull(CarriedWeightContractTest.class.getClassLoader().getResource(path), path);
    }

    private static String text(String path) {
        try (InputStream stream = CarriedWeightContractTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new AssertionError("Unable to read " + path, exception);
        }
    }
}
