package com.protyvkultury.revivalages.feature.technology.primitive.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class PrimitiveTechnologyClientConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_DURABILITY_TOOLTIPS;
    public static final ModConfigSpec.BooleanValue SHOW_INTERACTION_PREVIEWS;
    public static final ModConfigSpec.BooleanValue SHOW_PHYSICAL_ITEM_COUNTS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("primitiveTechnology");
        SHOW_DURABILITY_TOOLTIPS = builder
                .comment("Shows exact remaining uses on primitive tools and saw blades.")
                .define("showDurabilityTooltips", true);
        SHOW_INTERACTION_PREVIEWS = builder
                .comment("Shows translucent insertion and extraction previews on physical machine slots.")
                .define("showInteractionPreviews", true);
        SHOW_PHYSICAL_ITEM_COUNTS = builder
                .comment("Shows counts next to item stacks rendered in physical machine slots.")
                .define("showPhysicalItemCounts", true);
        builder.pop();
        SPEC = builder.build();
    }

    private PrimitiveTechnologyClientConfig() {
    }
}
