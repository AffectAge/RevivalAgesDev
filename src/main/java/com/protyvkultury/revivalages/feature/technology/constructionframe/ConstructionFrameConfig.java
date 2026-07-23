package com.protyvkultury.revivalages.feature.technology.constructionframe;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-owned availability and balance settings for frame assembly. */
public final class ConstructionFrameConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.IntValue TOOL_DURABILITY_COST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("constructionFrame");
        ENABLED = builder
                .comment("Enables Construction Frame acquisition, frame assembly recipes, and interaction.",
                        "Changing this value requires a server restart so recipes can be rebuilt safely.")
                .define("enabled", true);
        TOOL_DURABILITY_COST = builder
                .comment("Durability consumed from the assembly tool after a successful assembly.")
                .defineInRange("toolDurabilityCost", 1, 0, 1024);
        builder.pop();
        SPEC = builder.build();
    }

    private ConstructionFrameConfig() {
    }

    public static boolean enabled() {
        return SPEC.isLoaded() ? ENABLED.get() : ENABLED.getDefault();
    }

    public static int toolDurabilityCost() {
        return SPEC.isLoaded() ? TOOL_DURABILITY_COST.get() : TOOL_DURABILITY_COST.getDefault();
    }
}
