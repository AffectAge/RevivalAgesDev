package com.protyvkultury.revivalages.feature.technology.knapping;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class KnappingConfig {

    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue SCREEN_PARTICLES;

    static {
        ModConfigSpec.Builder server = new ModConfigSpec.Builder();
        ENABLED = server
                .comment(
                        "Enables Knapping acquisition, recipe presentation, and interaction.",
                        "Changing this value requires a server restart so recipes can be rebuilt safely."
                )
                .define("knapping.enabled", true);
        SERVER_SPEC = server.build();

        ModConfigSpec.Builder client = new ModConfigSpec.Builder();
        SCREEN_PARTICLES = client.define("knapping.screenParticles", true);
        CLIENT_SPEC = client.build();
    }

    private KnappingConfig() {
    }

    public static boolean enabled() {
        return SERVER_SPEC.isLoaded() ? ENABLED.get() : ENABLED.getDefault();
    }

    public static boolean screenParticles() {
        return CLIENT_SPEC.isLoaded() ? SCREEN_PARTICLES.get() : SCREEN_PARTICLES.getDefault();
    }
}
