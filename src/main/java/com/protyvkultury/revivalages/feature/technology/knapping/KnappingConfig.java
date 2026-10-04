package com.protyvkultury.revivalages.feature.technology.knapping;

import com.protyvkultury.revivalages.config.RevivalAgesConfig;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class KnappingConfig {

    public static final ModConfigSpec.BooleanValue SCREEN_PARTICLES;

    static {
        ModConfigSpec.Builder builder = RevivalAgesConfig.builder();
        SCREEN_PARTICLES = builder.define("knapping.screenParticles", true);
    }

    private KnappingConfig() {
    }

    public static boolean enabled() {
        return ContentAvailability.isEnabled(ContentKey.KNAPPING);
    }

    public static boolean screenParticles() {
        return RevivalAgesConfig.isLoaded() ? SCREEN_PARTICLES.get() : SCREEN_PARTICLES.getDefault();
    }

    public static void bootstrap() {
    }
}
