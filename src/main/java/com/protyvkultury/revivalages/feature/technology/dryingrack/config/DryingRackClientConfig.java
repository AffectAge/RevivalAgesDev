package com.protyvkultury.revivalages.feature.technology.dryingrack.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class DryingRackClientConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_PROGRESS_PARTICLES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("dryingRack");
        SHOW_PROGRESS_PARTICLES = builder
                .comment("Shows subtle happy-villager particles while a Drying Rack is making progress.")
                .define("showProgressParticles", true);
        builder.pop();
        SPEC = builder.build();
    }

    private DryingRackClientConfig() {
    }
}
