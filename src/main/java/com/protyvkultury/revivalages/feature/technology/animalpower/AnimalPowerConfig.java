package com.protyvkultury.revivalages.feature.technology.animalpower;

import com.protyvkultury.revivalages.feature.content.ContentKey;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-owned settings for the hand grindstone. */
public final class AnimalPowerConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue HAND_GRINDSTONE_ENABLED;
    public static final ModConfigSpec.IntValue HAND_GRINDSTONE_ROTATION_TICKS;
    public static final ModConfigSpec.IntValue HAND_GRINDSTONE_POINTS_PER_ROTATION;
    public static final ModConfigSpec.DoubleValue HAND_GRINDSTONE_EXHAUSTION;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("animalPower");
        builder.push("handGrindstone");
        HAND_GRINDSTONE_ENABLED = restartToggle(builder, "enabled", "Enables the Hand Grindstone.");
        HAND_GRINDSTONE_ROTATION_TICKS = builder.defineInRange("rotationTicks", 18, 1, 1200);
        HAND_GRINDSTONE_POINTS_PER_ROTATION = builder.defineInRange("pointsPerRotation", 2, 1, 1000);
        HAND_GRINDSTONE_EXHAUSTION = builder.defineInRange("exhaustionPerRotation", 0.1D, 0.0D, 40.0D);
        builder.pop(2);
        SPEC = builder.build();
    }

    private AnimalPowerConfig() {
    }

    public static boolean contentEnabled(ContentKey key) {
        if (key != ContentKey.HAND_GRINDSTONE) {
            throw new IllegalArgumentException("Not a hand-grinding content key: " + key);
        }
        ModConfigSpec.BooleanValue value = HAND_GRINDSTONE_ENABLED;
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    private static ModConfigSpec.BooleanValue restartToggle(
            ModConfigSpec.Builder builder,
            String name,
            String comment
    ) {
        return builder.comment(comment, "Changing this value requires a server restart.")
                .define(name, true);
    }
}
