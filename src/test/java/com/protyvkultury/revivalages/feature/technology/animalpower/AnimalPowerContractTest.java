package com.protyvkultury.revivalages.feature.technology.animalpower;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingChance;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class AnimalPowerContractTest {

    private static final List<String> MACHINES = List.of(
            "hand_grindstone",
            "horse_grindstone",
            "horse_chopping_block",
            "horse_press"
    );

    @Test
    void allPublicMachinesHaveUnconditionalResources() {
        for (String machine : MACHINES) {
            assertResource("assets/revivalages/blockstates/" + machine + ".json");
            assertResource("assets/revivalages/models/item/" + machine + ".json");
            assertResource("data/revivalages/loot_table/blocks/" + machine + ".json");
            assertResource("data/revivalages/recipe/" + machine + ".json");
        }
    }

    @Test
    void waypointCircuitIsUniqueAndSymmetric() {
        Set<AnimalWaypointCircuit.Offset> waypoints = new HashSet<>();

        for (int index = 0; index < AnimalWaypointCircuit.size(); index++) {
            AnimalWaypointCircuit.Offset waypoint = AnimalWaypointCircuit.offset(index);
            waypoints.add(waypoint);
            assertTrue(Math.abs(waypoint.x()) <= 3);
            assertTrue(Math.abs(waypoint.z()) <= 3);
        }

        assertEquals(8, waypoints.size());
        for (AnimalWaypointCircuit.Offset waypoint : waypoints) {
            AnimalWaypointCircuit.Offset opposite =
                    new AnimalWaypointCircuit.Offset(-waypoint.x(), -waypoint.z());
            assertTrue(waypoints.contains(opposite));
        }
    }

    @Test
    void tierTwoUsesFourCyclesAndProducesFourItems() {
        assertEquals(4, AnimalChoppingProfile.cyclesForTier(2));
        assertEquals(4, AnimalChoppingProfile.quantityForTier(2));
    }

    @Test
    void secondaryResultChanceUsesCanonicalBoundaryRules() {
        assertFalse(GrindingChance.shouldProduce(0.0D, 0.0D));
        assertTrue(GrindingChance.shouldProduce(1.0D, 0.999999D));
        assertTrue(GrindingChance.shouldProduce(0.25D, 0.249999D));
        assertFalse(GrindingChance.shouldProduce(0.25D, 0.25D));
        assertThrows(IllegalArgumentException.class, () -> GrindingChance.shouldProduce(-0.1D, 0.0D));
        assertThrows(IllegalArgumentException.class, () -> GrindingChance.shouldProduce(0.5D, 1.0D));
    }

    @Test
    void builtInProcessingRecipesUseCanonicalValidatedShapes() {
        String sugar = text("data/revivalages/recipe/sugar_cane_grinding.json");
        String bone = text("data/revivalages/recipe/bone_grinding.json");
        String seeds = text("data/revivalages/recipe/wheat_seeds_pressing.json");
        String leaves = text("data/revivalages/recipe/leaves_pressing.json");

        assertTrue(sugar.contains("\"type\": \"revivalages:grinding\""));
        assertTrue(sugar.contains("\"work_points\": 12"));
        assertTrue(bone.contains("\"work_points\": 12"));
        assertEquals(1, occurrences(seeds, "\"result\""));
        assertEquals(0, occurrences(seeds, "\"fluid_result\""));
        assertEquals(0, occurrences(leaves, "\"result\""));
        assertEquals(1, occurrences(leaves, "\"fluid_result\""));
        assertTrue(leaves.contains("\"amount\": 1000"));
    }

    private static String text(String path) {
        try (InputStream stream = AnimalPowerContractTest.class.getClassLoader().getResourceAsStream(path)) {
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

    private static void assertResource(String path) {
        assertNotNull(AnimalPowerContractTest.class.getClassLoader().getResource(path), path);
    }

    private static int occurrences(String value, String needle) {
        return value.split(Pattern.quote(needle), -1).length - 1;
    }
}
