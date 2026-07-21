package com.protyvkultury.revivalages.feature.technology.dryingrack.config;

import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.SeasonType;
import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.SeasonalBonuses;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class DryingRackConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue AUTOMATION_ENABLED;
    public static final ModConfigSpec.BooleanValue LADDER_ENABLED;
    public static final ModConfigSpec.DoubleValue LADDER_CLIMB_SPEED;
    public static final ModConfigSpec.DoubleValue CRUDE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue NORMAL_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue DIRECT_RAIN_SPEED;
    public static final ModConfigSpec.DoubleValue INDIRECT_RAIN_SPEED;
    public static final ModConfigSpec.DoubleValue NETHER_SPEED;
    public static final ModConfigSpec.DoubleValue DEFAULT_SPEED;
    public static final ModConfigSpec.DoubleValue HOT_BONUS;
    public static final ModConfigSpec.DoubleValue DRY_BONUS;
    public static final ModConfigSpec.DoubleValue COLD_PENALTY;
    public static final ModConfigSpec.DoubleValue WET_PENALTY;
    public static final ModConfigSpec.DoubleValue FIRE_BONUS;
    public static final ModConfigSpec.IntValue FIRE_RADIUS;
    public static final ModConfigSpec.DoubleValue DAYLIGHT_BONUS;
    public static final ModConfigSpec.BooleanValue SEASONS_ENABLED;
    public static final ModConfigSpec.DoubleValue SPRING_BONUS;
    public static final ModConfigSpec.DoubleValue SUMMER_BONUS;
    public static final ModConfigSpec.DoubleValue AUTUMN_BONUS;
    public static final ModConfigSpec.DoubleValue WINTER_BONUS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("dryingRack");
        AUTOMATION_ENABLED = builder
                .comment("Allows item-handler automation. Disabled by default to preserve manual early-game play.")
                .define("automationEnabled", false);
        LADDER_ENABLED = builder.comment("Allows vertically stacked normal drying racks to act as ladders.")
                .define("ladderEnabled", true);
        LADDER_CLIMB_SPEED = builder.comment("Vertical climb speed for stacked normal drying racks.")
                .defineInRange("ladderClimbSpeed", 0.1D, 0.0D, 1.0D);
        CRUDE_MULTIPLIER = builder.comment("Final speed multiplier for the crude drying rack.")
                .defineInRange("crudeMultiplier", 1.0D, 0.0D, 100.0D);
        NORMAL_MULTIPLIER = builder.comment("Final speed multiplier for the normal drying rack.")
                .defineInRange("normalMultiplier", 1.35D, 0.0D, 100.0D);

        builder.push("environment");
        DIRECT_RAIN_SPEED = speed(builder, "directRainSpeed", -1.0D);
        INDIRECT_RAIN_SPEED = speed(builder, "indirectRainSpeed", 0.25D);
        NETHER_SPEED = speed(builder, "netherSpeed", 2.0D);
        DEFAULT_SPEED = speed(builder, "defaultSpeed", 1.0D);
        HOT_BONUS = speed(builder, "hotBonus", 0.2D);
        DRY_BONUS = speed(builder, "dryBonus", 0.2D);
        COLD_PENALTY = speed(builder, "coldPenalty", -0.2D);
        WET_PENALTY = speed(builder, "wetPenalty", -0.2D);
        FIRE_BONUS = speed(builder, "fireBonus", 0.2D);
        FIRE_RADIUS = builder.defineInRange("fireRadius", 2, 0, 8);
        DAYLIGHT_BONUS = speed(builder, "daylightBonus", 0.2D);
        builder.pop();

        builder.push("seasons");
        SEASONS_ENABLED = builder
                .comment("Applies configured bonuses from a supported season provider.")
                .define("enabled", true);
        SPRING_BONUS = seasonBonus(builder, "springBonus", 0.1D);
        SUMMER_BONUS = seasonBonus(builder, "summerBonus", 0.3D);
        AUTUMN_BONUS = seasonBonus(builder, "autumnBonus", -0.1D);
        WINTER_BONUS = seasonBonus(builder, "winterBonus", -0.3D);
        builder.pop(2);
        SPEC = builder.build();
    }

    private DryingRackConfig() {
    }

    public static double seasonBonus(SeasonType season) {
        return new SeasonalBonuses(
                SEASONS_ENABLED.get(),
                SPRING_BONUS.get(),
                SUMMER_BONUS.get(),
                AUTUMN_BONUS.get(),
                WINTER_BONUS.get()
        ).valueFor(season);
    }

    private static ModConfigSpec.DoubleValue speed(ModConfigSpec.Builder builder, String name, double defaultValue) {
        return builder.defineInRange(name, defaultValue, -100.0D, 100.0D);
    }

    private static ModConfigSpec.DoubleValue seasonBonus(
            ModConfigSpec.Builder builder,
            String name,
            double defaultValue
    ) {
        return builder.comment("Additive seasonal speed modifier. Zero disables this season's effect.")
                .defineInRange(name, defaultValue, -100.0D, 100.0D);
    }
}
