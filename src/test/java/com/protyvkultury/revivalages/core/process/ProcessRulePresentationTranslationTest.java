package com.protyvkultury.revivalages.core.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ProcessRulePresentationTranslationTest {

    @Test
    void everyRuleHasOneStaticAndOneLiveTranslationInBothBundledLanguages() throws IOException {
        for (String language : new String[] {"en_us", "ru_ru"}) {
            String translations = Files.readString(Path.of(
                    "src", "main", "resources", "assets", "revivalages", "lang", language + ".json"));
            for (ProcessRuleType type : ProcessRuleType.values()) {
                String key = "gui.revivalages.process_rule." + type.getSerializedName();
                assertEquals(1, occurrences(translations, key), () -> language + " must declare " + key + " exactly once");
                assertEquals(1, occurrences(translations, key + ".status"),
                        () -> language + " must declare " + key + ".status exactly once");
            }
        }
    }

    @Test
    void animalPowerViewerDetailKeysExistInBothBundledLanguages() throws IOException {
        String[] keys = {
                "gui.revivalages.process_rule.attached_worker.animals",
                "gui.revivalages.process_rule.attached_worker.lead",
                "gui.revivalages.process_rule.valid_work_area.square",
                "gui.revivalages.process_rule.valid_work_area.floor",
                "gui.revivalages.process_rule.valid_work_area.headroom",
                "gui.revivalages.tool_requirement.anvil",
                "gui.revivalages.tool_requirement.anvil_hits",
                "gui.revivalages.tool_requirement.anvil_durability_enabled",
                "gui.revivalages.tool_requirement.anvil_durability_disabled",
                "gui.revivalages.tool_requirement.anvil_hunger"
        };
        for (String language : new String[] {"en_us", "ru_ru"}) {
            String translations = Files.readString(Path.of(
                    "src", "main", "resources", "assets", "revivalages", "lang", language + ".json"));
            for (String key : keys) {
                assertEquals(1, occurrences(translations, key), () -> language + " must declare " + key + " exactly once");
            }
        }
    }

    @Test
    void everyChanceOutcomeModeHasTranslationsInBothBundledLanguages() throws IOException {
        for (String language : new String[] {"en_us", "ru_ru"}) {
            String translations = Files.readString(Path.of(
                    "src", "main", "resources", "assets", "revivalages", "lang", language + ".json"));
            for (ProcessOutcomeMode mode : ProcessOutcomeMode.values()) {
                String key = "gui.revivalages.process_rule.random_outcome." + mode.serializedName();
                assertEquals(1, occurrences(translations, key), () -> language + " must declare " + key + " exactly once");
            }
        }
    }

    private static int occurrences(String source, String key) {
        return (int) Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:").matcher(source).results().count();
    }
}
