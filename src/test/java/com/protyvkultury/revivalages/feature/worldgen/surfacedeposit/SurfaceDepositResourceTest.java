package com.protyvkultury.revivalages.feature.worldgen.surfacedeposit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

final class SurfaceDepositResourceTest {

    private static final Pattern WEIGHT = Pattern.compile("\\\"weight\\\"\\s*:\\s*(\\d+)");
    private static final Pattern COUNT = Pattern.compile("\\\"type\\\"\\s*:\\s*\\\"minecraft:count\\\"\\s*,\\s*\\\"count\\\"\\s*:\\s*(\\d+)");

    private static final List<String> ROCKS = List.of(
            "rock", "granite_rock", "diorite_rock", "andesite_rock", "sand_rock",
            "red_sand_rock", "gravel_rock", "end_stone_rock", "netherrack_rock", "soul_soil_rock"
    );
    private static final List<String> STICKS = List.of(
            "oak_stick", "spruce_stick", "birch_stick", "acacia_stick", "jungle_stick",
            "dark_oak_stick", "mangrove_stick", "cherry_stick", "bamboo_stick", "crimson_stick",
            "warped_stick"
    );

    @Test
    void everyDepositHasItsPublicAndWorldgenResources() {
        for (String id : ROCKS) {
            assertResource("assets/revivalages/blockstates/" + id + ".json");
            assertResource("assets/revivalages/models/item/" + id + ".json");
            assertResource("data/revivalages/loot_table/blocks/" + id + ".json");
            String worldgenId = id.equals("end_stone_rock") ? "endstone_rock" : id;
            assertResource("data/revivalages/worldgen/configured_feature/" + worldgenId + ".json");
            assertResource("data/revivalages/worldgen/placed_feature/" + worldgenId + ".json");
            assertResource("data/revivalages/neoforge/biome_modifier/" + worldgenId + ".json");
        }
        for (String id : STICKS) {
            assertResource("assets/revivalages/blockstates/" + id + ".json");
            assertResource("assets/revivalages/models/item/" + id + ".json");
            assertResource("data/revivalages/loot_table/blocks/" + id + ".json");
            assertResource("data/revivalages/worldgen/configured_feature/" + id + ".json");
            assertResource("data/revivalages/worldgen/placed_feature/" + id + ".json");
            assertResource("data/revivalages/neoforge/biome_modifier/" + id + ".json");
        }
    }

    @Test
    void configuredFeaturesPreserveReferenceVariantWeights() {
        assertEquals(List.of(10, 7, 5, 1), weights("rock"));
        assertEquals(List.of(7, 5, 1), weights("oak_stick"));
    }

    @Test
    void correctedPlacementCountsDoNotRegress() {
        assertEquals(List.of(7), placementCounts("red_sand_rock"));
        assertEquals(List.of(3), placementCounts("endstone_rock"));
        assertEquals(List.of(3), placementCounts("gravel_rock"));
    }

    @Test
    void redSandRockDropsItsMaterialSplitter() {
        String loot = text("data/revivalages/loot_table/blocks/red_sand_rock.json");

        assertEquals(1, occurrences(loot, "\"name\": \"revivalages:red_sandstone_splitter\""));
    }

    private static List<Integer> weights(String id) {
        String configured = text("data/revivalages/worldgen/configured_feature/" + id + ".json");
        return WEIGHT.matcher(configured).results()
                .map(MatchResult::group)
                .map(value -> value.substring(value.lastIndexOf(':') + 1).trim())
                .map(Integer::parseInt)
                .toList();
    }

    private static List<Integer> placementCounts(String id) {
        String placed = text("data/revivalages/worldgen/placed_feature/" + id + ".json");
        return COUNT.matcher(placed).results()
                .map(result -> result.group(1))
                .map(Integer::parseInt)
                .toList();
    }

    private static int occurrences(String value, String needle) {
        return value.split(Pattern.quote(needle), -1).length - 1;
    }

    private static void assertResource(String path) {
        assertNotNull(SurfaceDepositResourceTest.class.getClassLoader().getResource(path), path);
    }

    private static String text(String path) {
        try (InputStream stream = SurfaceDepositResourceTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                StringBuilder result = new StringBuilder();
                char[] buffer = new char[4096];
                int read;
                while ((read = reader.read(buffer)) >= 0) {
                    result.append(buffer, 0, read);
                }
                return result.toString();
            }
        } catch (IOException exception) {
            throw new AssertionError("Unable to read " + path, exception);
        }
    }
}
