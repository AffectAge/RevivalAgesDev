package com.protyvkultury.revivalages.feature.technology.choppingblock;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

final class ChoppingBlockVisualContractTest {

    @Test
    void woodChipsUseFiveStageBlockModels() {
        String blockState = text("assets/revivalages/blockstates/chopping_block.json");

        assertTrue(blockState.contains("\"sawdust\": \"1|2|3|4|5\""));
        assertTrue(blockState.contains("\"sawdust\": \"5\""));
        assertTrue(blockState.contains("revivalages:block/chopping_block_sawdust_side"));
        assertTrue(blockState.contains("revivalages:block/chopping_block_sawdust_top"));
        assertResource("assets/revivalages/models/block/chopping_block_sawdust_side.json");
        assertResource("assets/revivalages/models/block/chopping_block_sawdust_top.json");
    }

    @Test
    void coreAndBarkAreIndependentMultipartLayers() {
        String blockState = text("assets/revivalages/blockstates/chopping_block.json");

        assertTrue(blockState.contains(
                "\"damage\": \"0\" }, \"apply\": { \"model\": \"revivalages:block/chopping_block_0\" }"
        ));
        assertTrue(blockState.contains(
                "\"damage\": \"0\" }, \"apply\": { \"model\": \"revivalages:block/chopping_block_bark_a\" }"
        ));
        assertFalse(blockState.contains(
                "\"apply\": [{ \"model\": \"revivalages:block/chopping_block_0\" }"
        ));
    }

    @Test
    void accumulatedWoodChipsUseTheRevivalAgesTexture() {
        String side = text("assets/revivalages/models/block/chopping_block_sawdust_side.json");
        String top = text("assets/revivalages/models/block/chopping_block_sawdust_top.json");

        assertTrue(side.contains("\"all\": \"revivalages:item/wood_chips\""));
        assertTrue(top.contains("\"all\": \"revivalages:item/wood_chips\""));
    }

    private static void assertResource(String path) {
        assertNotNull(ChoppingBlockVisualContractTest.class.getClassLoader().getResource(path), path);
    }

    private static String text(String path) {
        try (InputStream input = ChoppingBlockVisualContractTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new AssertionError("Unable to read " + path, exception);
        }
    }
}
