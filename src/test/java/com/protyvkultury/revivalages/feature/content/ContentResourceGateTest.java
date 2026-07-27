package com.protyvkultury.revivalages.feature.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ContentResourceGateTest {

    private static final Pattern CONTENT_ID =
            Pattern.compile("\"(?:content|contents)\"\\s*:\\s*(?:\\[\\s*)?\"revivalages:([a-z_]+)");
    private static final Pattern CONTENT_KEY_DECLARATION =
            Pattern.compile("^\\s*[A-Z_]+\\(\"([a-z_]+)\"", Pattern.MULTILINE);

    @Test
    void everyRecipeHasAValidContentGate() throws IOException {
        Set<String> knownKeys = contentKeys();
        Path root = Path.of("src/main/resources/data/revivalages/recipe");
        int count = 0;
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".json")).toList()) {
                String json = Files.readString(path);
                assertTrue(json.contains("\"neoforge:conditions\""), path + " has no conditions");
                assertTrue(
                        json.matches("(?s).*revivalages:(?:content_enabled|any_content_enabled|"
                                + "knapping_enabled|construction_frame_enabled|structural_enabled).*"),
                        path + " has no recognized content condition"
                );
                Matcher matcher = CONTENT_ID.matcher(json);
                while (matcher.find()) {
                    assertTrue(knownKeys.contains(matcher.group(1)), path + " has unknown content key");
                }
                count++;
            }
        }
        assertEquals(158, count, "recipe count");
    }

    @Test
    void everyBlockLootTableHasAValidContentGate() throws IOException {
        Set<String> knownKeys = contentKeys();
        Path root = Path.of("src/main/resources/data/revivalages/loot_table/blocks");
        int count = 0;
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".json")).toList()) {
                String json = Files.readString(path);
                assertTrue(json.contains("\"neoforge:conditions\""), path + " has no conditions");
                assertTrue(
                        json.contains("\"type\": \"revivalages:content_enabled\""),
                        path + " has no content condition"
                );
                Matcher matcher = CONTENT_ID.matcher(json);
                assertTrue(matcher.find(), path + " has no content key");
                assertTrue(knownKeys.contains(matcher.group(1)), path + " has unknown content key");
                count++;
            }
        }
        assertEquals(60, count, "block loot table count");
    }

    @Test
    void everySurfaceBiomeModifierDeclaresItsContentKey() throws IOException {
        Path root = Path.of("src/main/resources/data/revivalages/neoforge/biome_modifier");
        int count = 0;
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".json")).toList()) {
                String json = Files.readString(path);
                String expected = path.getFileName().toString().endsWith("_stick.json")
                        ? "\"content\": \"revivalages:surface_sticks\""
                        : "\"content\": \"revivalages:surface_rocks\"";
                assertTrue(json.contains(expected), path + " has the wrong worldgen gate");
                count++;
            }
        }
        assertEquals(22, count, "surface biome modifier count");
    }

    @Test
    void everyContentKeyHasEnglishAndRussianNames() throws IOException {
        String english = Files.readString(Path.of("src/main/resources/assets/revivalages/lang/en_us.json"));
        String russian = Files.readString(Path.of("src/main/resources/assets/revivalages/lang/ru_ru.json"));
        for (String key : contentKeys()) {
            String translation = "\"content.revivalages." + key + "\"";
            assertTrue(english.contains(translation), "missing English " + translation);
            assertTrue(russian.contains(translation), "missing Russian " + translation);
        }
        assertTrue(english.contains("\"message.revivalages.content.disabled\""));
        assertTrue(russian.contains("\"message.revivalages.content.disabled\""));
    }

    private static Set<String> contentKeys() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/protyvkultury/revivalages/feature/content/ContentKey.java"
        ));
        Matcher matcher = CONTENT_KEY_DECLARATION.matcher(source);
        Set<String> keys = new HashSet<>();
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }
        assertEquals(40, keys.size(), "content key declaration count");
        return Set.copyOf(keys);
    }
}
