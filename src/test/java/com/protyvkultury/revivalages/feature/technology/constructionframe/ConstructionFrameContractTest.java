package com.protyvkultury.revivalages.feature.technology.constructionframe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.protyvkultury.revivalages.feature.technology.constructionframe.recipe.FrameGridMath;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

final class ConstructionFrameContractTest {

    private static final List<String> MACHINES = List.of(
            "hand_grindstone",
            "crude_drying_rack",
            "drying_rack",
            "chopping_block",
            "pit_kiln",
            "log_pile",
            "barrel",
            "soaking_pot",
            "tanning_rack",
            "horse_grindstone",
            "horse_chopping_block",
            "horse_press",
            "stone_sawmill",
            "stone_oven",
            "stone_kiln",
            "stone_crucible",
            "anvil"
    );

    @Test
    void gridIndicesRoundTripAcrossEveryCell() {
        for (int index = 0; index < 27; index++) {
            int[] position = FrameGridMath.position(index);
            assertEquals(index, FrameGridMath.index(position[0], position[1], position[2]));
        }
        assertThrows(IllegalArgumentException.class, () -> FrameGridMath.position(-1));
        assertThrows(IllegalArgumentException.class, () -> FrameGridMath.position(27));
    }

    @Test
    void fourQuarterTurnsReturnEveryCellToItsOrigin() {
        for (int index = 0; index < 27; index++) {
            int[] position = FrameGridMath.position(index);
            assertEquals(index, FrameGridMath.rotatedIndex(position[0], position[1], position[2], 0));
            assertEquals(index, FrameGridMath.rotatedIndex(position[0], position[1], position[2], 4));
        }
    }

    @Test
    void allMachinePatternsAreExactThreeByThreeByThree() {
        for (String machine : MACHINES) {
            String recipe = resourceText(
                    "data/revivalages/recipe/frame_assembly/" + machine + ".json"
            );
            for (String layer : List.of("bottom", "middle", "top")) {
                Matcher matcher = Pattern.compile(
                        "\\\"" + layer + "\\\"\\s*:\\s*\\[(.*?)\\]",
                        Pattern.DOTALL
                ).matcher(recipe);
                assertTrue(matcher.find(), machine + " " + layer);
                Matcher rows = Pattern.compile("\\\"([^\\\"]*)\\\"").matcher(matcher.group(1));
                int count = 0;
                while (rows.find()) {
                    assertEquals(3, rows.group(1).length(), machine + " " + layer);
                    count++;
                }
                assertEquals(3, count, machine + " " + layer);
            }
            assertTrue(recipe.contains("\"type\": \"revivalages:frame_assembly\""), machine);
            assertTrue(recipe.contains("\"count\": 1"), machine);
        }
    }

    @Test
    void woodVariantSourcePointsAtTheLogCell() {
        String recipe = resourceText(
                "data/revivalages/recipe/frame_assembly/horse_chopping_block.json"
        );
        assertTrue(recipe.contains("\"x\": 1"));
        assertTrue(recipe.contains("\"y\": 0"));
        assertTrue(recipe.contains("\"z\": 1"));
        assertTrue(recipe.contains("\"RWR\""));
    }

    @Test
    void frameResourcesAndConditionalFallbacksExist() {
        assertResource("assets/revivalages/blockstates/construction_frame.json");
        assertResource("assets/revivalages/models/item/construction_frame.json");
        assertResource("data/revivalages/loot_table/blocks/construction_frame.json");
        for (String machine : MACHINES) {
            String fallback = resourceText("data/revivalages/recipe/" + machine + ".json");
            assertTrue(fallback.contains("\"neoforge:conditions\""), machine);
        }
    }

    private static String resourceText(String path) {
        try (InputStream stream = ConstructionFrameContractTest.class
                .getClassLoader()
                .getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new AssertionError("Unable to read " + path, exception);
        }
    }

    private static void assertResource(String path) {
        assertNotNull(ConstructionFrameContractTest.class.getClassLoader().getResource(path), path);
    }
}
