package com.protyvkultury.revivalages.feature.technology.constructionframe;

import com.protyvkultury.revivalages.config.RevivalAgesConfig;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-owned availability and balance settings for frame assembly. */
public final class ConstructionFrameConfig {

    public static final ModConfigSpec.IntValue TOOL_DURABILITY_COST;

    static {
        ModConfigSpec.Builder builder = RevivalAgesConfig.builder();
        builder.push("constructionFrame");
        TOOL_DURABILITY_COST = builder
                .comment("Durability consumed from the assembly tool after a successful assembly.")
                .defineInRange("toolDurabilityCost", 1, 0, 1024);
        builder.pop();
    }

    private ConstructionFrameConfig() {
    }

    public static boolean enabled() {
        return ContentAvailability.isEnabled(ContentKey.CONSTRUCTION_FRAME);
    }

    public static int toolDurabilityCost() {
        return RevivalAgesConfig.isLoaded() ? TOOL_DURABILITY_COST.get() : TOOL_DURABILITY_COST.getDefault();
    }

    public static void bootstrap() {
    }
}
