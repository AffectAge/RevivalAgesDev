package com.protyvkultury.revivalages.feature.technology.dryingrack.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class DryingRackClientConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_PROGRESS_PARTICLES;
    public static final ModConfigSpec.BooleanValue SHOW_ITEM_PREVIEW;
    public static final ModConfigSpec.BooleanValue SHOW_ITEM_COUNTS;
    public static final ModConfigSpec.BooleanValue SHOW_INTERACTION_BOUNDS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("dryingRack");
        SHOW_PROGRESS_PARTICLES = builder
                .comment("Shows subtle happy-villager particles while a Drying Rack is making progress.")
                .define("showProgressParticles", true);
        SHOW_ITEM_PREVIEW = builder
                .comment("Shows a translucent preview of the item targeted for insertion or removal.")
                .define("showItemPreview", true);
        SHOW_ITEM_COUNTS = builder
                .comment("Shows stack counts next to physical Drying Rack contents.")
                .define("showItemCounts", true);
        SHOW_INTERACTION_BOUNDS = builder
                .comment("Shows a green outline around the selected Drying Rack interaction area.")
                .define("showInteractionBounds", true);
        builder.pop();
        SPEC = builder.build();
    }

    private DryingRackClientConfig() {
    }
}
