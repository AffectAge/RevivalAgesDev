package com.protyvkultury.revivalages.feature.technology.animalpower;

import com.protyvkultury.revivalages.feature.content.ContentKey;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-owned balance and automation settings for animal-powered devices. */
public final class AnimalPowerConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue HAND_GRINDSTONE_ENABLED;
    public static final ModConfigSpec.BooleanValue HORSE_GRINDSTONE_ENABLED;
    public static final ModConfigSpec.BooleanValue HORSE_CHOPPING_BLOCK_ENABLED;
    public static final ModConfigSpec.BooleanValue HORSE_PRESS_ENABLED;
    public static final ModConfigSpec.BooleanValue AUTOMATION_ENABLED;
    public static final ModConfigSpec.IntValue WORK_AREA_CHECK_INTERVAL;
    public static final ModConfigSpec.IntValue WORKER_RETRY_INTERVAL;
    public static final ModConfigSpec.IntValue NAVIGATION_REFRESH_INTERVAL;
    public static final ModConfigSpec.DoubleValue WORKER_SPEED;
    public static final ModConfigSpec.DoubleValue WAYPOINT_REACH_DISTANCE;
    public static final ModConfigSpec.IntValue HAND_GRINDSTONE_ROTATION_TICKS;
    public static final ModConfigSpec.IntValue HAND_GRINDSTONE_POINTS_PER_ROTATION;
    public static final ModConfigSpec.DoubleValue HAND_GRINDSTONE_EXHAUSTION;
    public static final ModConfigSpec.IntValue CHOPPING_TIER;
    public static final ModConfigSpec.IntValue CHOPPING_POINTS_PER_CYCLE;
    public static final ModConfigSpec.IntValue PRESS_POINTS;
    public static final ModConfigSpec.IntValue PRESS_TANK_CAPACITY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("animalPower");
        ENABLED = restartToggle(builder, "enabled", "Enables the complete Animal Power feature family.");
        HORSE_GRINDSTONE_ENABLED = restartToggle(
                builder,
                "horseGrindstoneEnabled",
                "Enables the Horse Grindstone."
        );
        AUTOMATION_ENABLED = builder.comment("Exposes item and fluid capabilities on animal-powered devices.")
                .define("automationEnabled", true);
        WORK_AREA_CHECK_INTERVAL = builder.defineInRange("workAreaCheckInterval", 20, 1, 1200);
        WORKER_RETRY_INTERVAL = builder.defineInRange("workerRetryInterval", 40, 1, 1200);
        NAVIGATION_REFRESH_INTERVAL = builder.defineInRange("navigationRefreshInterval", 20, 1, 1200);
        WORKER_SPEED = builder.defineInRange("workerSpeed", 0.65D, 0.05D, 2.0D);
        WAYPOINT_REACH_DISTANCE = builder.defineInRange("waypointReachDistance", 1.25D, 0.25D, 3.0D);

        builder.push("handGrindstone");
        HAND_GRINDSTONE_ENABLED = restartToggle(builder, "enabled", "Enables the Hand Grindstone.");
        HAND_GRINDSTONE_ROTATION_TICKS = builder.defineInRange("rotationTicks", 18, 1, 1200);
        HAND_GRINDSTONE_POINTS_PER_ROTATION = builder.defineInRange("pointsPerRotation", 2, 1, 1000);
        HAND_GRINDSTONE_EXHAUSTION = builder.defineInRange("exhaustionPerRotation", 0.1D, 0.0D, 40.0D);
        builder.pop();

        builder.push("choppingBlock");
        HORSE_CHOPPING_BLOCK_ENABLED = restartToggle(
                builder,
                "enabled",
                "Enables the Horse Chopping Block."
        );
        CHOPPING_TIER = builder.defineInRange("recipeTier", 2, 0, 3);
        CHOPPING_POINTS_PER_CYCLE = builder.defineInRange("pointsPerChoppingCycle", 8, 1, 1000);
        builder.pop();

        builder.push("press");
        HORSE_PRESS_ENABLED = restartToggle(builder, "enabled", "Enables the Horse Press.");
        PRESS_POINTS = builder.defineInRange("pointsPerRecipe", 16, 1, 100000);
        PRESS_TANK_CAPACITY = builder.defineInRange("tankCapacity", 3000, 1, 1_000_000);
        builder.pop(2);
        SPEC = builder.build();
    }

    private AnimalPowerConfig() {
    }

    public static boolean contentEnabled(ContentKey key) {
        ModConfigSpec.BooleanValue value = switch (key) {
            case ANIMAL_POWER -> ENABLED;
            case HAND_GRINDSTONE -> HAND_GRINDSTONE_ENABLED;
            case HORSE_GRINDSTONE -> HORSE_GRINDSTONE_ENABLED;
            case HORSE_CHOPPING_BLOCK -> HORSE_CHOPPING_BLOCK_ENABLED;
            case HORSE_PRESS -> HORSE_PRESS_ENABLED;
            default -> throw new IllegalArgumentException("Not an Animal Power key: " + key);
        };
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
