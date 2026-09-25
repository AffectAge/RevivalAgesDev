package com.protyvkultury.revivalages.feature.technology.animalpower;

import com.protyvkultury.revivalages.config.RevivalAgesConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-owned settings for the hand grindstone. */
public final class AnimalPowerConfig {

    public static final ModConfigSpec.IntValue HAND_GRINDSTONE_ROTATION_TICKS;
    public static final ModConfigSpec.IntValue HAND_GRINDSTONE_POINTS_PER_ROTATION;
    public static final ModConfigSpec.DoubleValue HAND_GRINDSTONE_EXHAUSTION;

    static {
        ModConfigSpec.Builder builder = RevivalAgesConfig.builder();
        builder.push("animalPower");
        builder.push("handGrindstone");
        HAND_GRINDSTONE_ROTATION_TICKS = builder.defineInRange("rotationTicks", 18, 1, 1200);
        HAND_GRINDSTONE_POINTS_PER_ROTATION = builder.defineInRange("pointsPerRotation", 2, 1, 1000);
        HAND_GRINDSTONE_EXHAUSTION = builder.defineInRange("exhaustionPerRotation", 0.1D, 0.0D, 40.0D);
        builder.pop(2);
    }

    private AnimalPowerConfig() {
    }

    public static void bootstrap() {
    }
}
