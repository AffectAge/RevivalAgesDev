package com.protyvkultury.revivalages.feature.technology.primitive.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-owned balance settings for the primitive technology feature family. */
public final class PrimitiveTechnologyConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue AUTOMATION_ENABLED;
    public static final ModConfigSpec.BooleanValue PROGRESS_PARTICLES;
    public static final ModConfigSpec.DoubleValue RAW_HIDE_DROP_CHANCE;
    public static final ModConfigSpec.IntValue RAW_HIDE_MAX_DROPS;

    public static final ModConfigSpec.IntValue CAMPFIRE_COOK_TICKS;
    public static final ModConfigSpec.IntValue CAMPFIRE_BURN_TICKS_PER_LOG;
    public static final ModConfigSpec.IntValue CAMPFIRE_BURNED_FOOD_TICKS;
    public static final ModConfigSpec.IntValue CAMPFIRE_FULL_SPEED_FUEL_LEVEL;
    public static final ModConfigSpec.BooleanValue CAMPFIRE_RAIN_EXTINGUISHES;
    public static final ModConfigSpec.IntValue CAMPFIRE_RAIN_EXTINGUISH_TICKS;
    public static final ModConfigSpec.DoubleValue CAMPFIRE_ASH_CHANCE;
    public static final ModConfigSpec.DoubleValue CAMPFIRE_PLAYER_BURN_CHANCE;
    public static final ModConfigSpec.DoubleValue CAMPFIRE_PLAYER_BURN_DAMAGE;
    public static final ModConfigSpec.DoubleValue CAMPFIRE_ENTITY_BURN_DAMAGE;
    public static final ModConfigSpec.DoubleValue CAMPFIRE_FLOOR_IGNITION_CHANCE;
    public static final ModConfigSpec.IntValue CAMPFIRE_MINIMUM_LIGHT;
    public static final ModConfigSpec.IntValue CAMPFIRE_MAXIMUM_LIGHT;

    public static final ModConfigSpec.IntValue CHOPPING_WOOD_CHOPS;
    public static final ModConfigSpec.IntValue CHOPPING_STONE_CHOPS;
    public static final ModConfigSpec.IntValue CHOPPING_IRON_CHOPS;
    public static final ModConfigSpec.IntValue CHOPPING_DIAMOND_CHOPS;
    public static final ModConfigSpec.IntValue CHOPPING_WOOD_OUTPUT;
    public static final ModConfigSpec.IntValue CHOPPING_STONE_OUTPUT;
    public static final ModConfigSpec.IntValue CHOPPING_IRON_OUTPUT;
    public static final ModConfigSpec.IntValue CHOPPING_DIAMOND_OUTPUT;
    public static final ModConfigSpec.IntValue CHOPPING_CHOPS_PER_DAMAGE;
    public static final ModConfigSpec.DoubleValue CHOPPING_WOOD_CHIPS_CHANCE;
    public static final ModConfigSpec.DoubleValue CHOPPING_EXHAUSTION_PER_CHOP;
    public static final ModConfigSpec.DoubleValue CHOPPING_EXHAUSTION_PER_CRAFT;
    public static final ModConfigSpec.DoubleValue CHOPPING_EXHAUSTION_PER_CHIP_SCOOP;
    public static final ModConfigSpec.IntValue CHOPPING_MINIMUM_HUNGER;
    public static final ModConfigSpec.BooleanValue CHOPPING_USES_DURABILITY;

    public static final ModConfigSpec.IntValue PIT_KILN_MAX_STACK_SIZE;
    public static final ModConfigSpec.DoubleValue PIT_KILN_DURATION_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue PIT_KILN_VARIABLE_SPEED;
    public static final ModConfigSpec.BooleanValue PIT_KILN_RAIN_EXTINGUISHES;
    public static final ModConfigSpec.IntValue PIT_KILN_RAIN_EXTINGUISH_TICKS;

    public static final ModConfigSpec.IntValue BARREL_CAPACITY;
    public static final ModConfigSpec.DoubleValue BARREL_DURATION_MULTIPLIER;
    public static final ModConfigSpec.IntValue BARREL_RAIN_FILL_INTERVAL;
    public static final ModConfigSpec.IntValue BARREL_RAIN_CONVERSION_INTERVAL;
    public static final ModConfigSpec.IntValue HOT_FLUID_TEMPERATURE;
    public static final ModConfigSpec.BooleanValue WOODEN_CONTAINERS_HOLD_HOT_FLUIDS;

    public static final ModConfigSpec.IntValue SOAKING_POT_CAPACITY;
    public static final ModConfigSpec.IntValue SOAKING_POT_MAX_STACK_SIZE;
    public static final ModConfigSpec.DoubleValue SOAKING_POT_DURATION_MULTIPLIER;

    public static final ModConfigSpec.DoubleValue TANNING_RACK_DURATION_MULTIPLIER;
    public static final ModConfigSpec.IntValue TANNING_RACK_RAIN_RUIN_TICKS;

    public static final ModConfigSpec.IntValue STONE_MACHINE_INPUT_LIMIT;
    public static final ModConfigSpec.IntValue STONE_MACHINE_FUEL_LIMIT;
    public static final ModConfigSpec.DoubleValue STONE_MACHINE_FUEL_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue STONE_MACHINE_AIRFLOW_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue STONE_MACHINE_AIRFLOW_DRAG;
    public static final ModConfigSpec.BooleanValue STONE_MACHINE_KEEP_HEAT;
    public static final ModConfigSpec.DoubleValue STONE_SAWMILL_BLADE_DAMAGE;
    public static final ModConfigSpec.BooleanValue STONE_SAWMILL_DAMAGE_BLADES;
    public static final ModConfigSpec.DoubleValue STONE_SAWMILL_WOOD_CHIP_CHANCE;
    public static final ModConfigSpec.BooleanValue STONE_SAWMILL_IDLE_SOUND_ENABLED;
    public static final ModConfigSpec.DoubleValue STONE_SAWMILL_IDLE_SOUND_VOLUME;
    public static final ModConfigSpec.BooleanValue STONE_SAWMILL_COMPLETE_SOUND_ENABLED;
    public static final ModConfigSpec.DoubleValue STONE_SAWMILL_COMPLETE_SOUND_VOLUME;
    public static final ModConfigSpec.IntValue STONE_OVEN_COOK_TICKS;
    public static final ModConfigSpec.DoubleValue STONE_OVEN_DRYING_DURATION_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue STONE_KILN_PIT_DURATION_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue STONE_KILN_PIT_FAILURE_MULTIPLIER;
    public static final ModConfigSpec.IntValue STONE_CRUCIBLE_CAPACITY;
    public static final ModConfigSpec.IntValue ANVIL_HITS_PER_DAMAGE_STAGE;
    public static final ModConfigSpec.DoubleValue ANVIL_EXHAUSTION_PER_HIT;
    public static final ModConfigSpec.DoubleValue ANVIL_EXHAUSTION_PER_CRAFT;
    public static final ModConfigSpec.IntValue ANVIL_MINIMUM_HUNGER;
    public static final ModConfigSpec.BooleanValue ANVIL_USE_TOOL_DURABILITY;

    public static final ModConfigSpec.BooleanValue CAMPFIRE_EFFECTS_ENABLED;
    public static final ModConfigSpec.IntValue CAMPFIRE_EFFECT_START_TIME;
    public static final ModConfigSpec.IntValue CAMPFIRE_EFFECT_STOP_TIME;
    public static final ModConfigSpec.IntValue CAMPFIRE_EFFECT_RANGE;
    public static final ModConfigSpec.DoubleValue COMFORT_FOOD_BONUS;
    public static final ModConfigSpec.IntValue RESTING_REGEN_INTERVAL;
    public static final ModConfigSpec.IntValue RESTING_LEVEL_INTERVAL;
    public static final ModConfigSpec.IntValue WELL_FED_DURATION;
    public static final ModConfigSpec.DoubleValue WELL_FED_EXHAUSTION_MODIFIER;
    public static final ModConfigSpec.IntValue WELL_RESTED_DURATION;
    public static final ModConfigSpec.IntValue WELL_RESTED_ABSORPTION_HALF_HEARTS;
    public static final ModConfigSpec.IntValue FOCUSED_DURATION;
    public static final ModConfigSpec.DoubleValue FOCUSED_XP_BONUS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("primitiveTechnology");
        AUTOMATION_ENABLED = builder.comment("Exposes item and fluid capabilities on primitive devices.")
                .define("automationEnabled", true);
        PROGRESS_PARTICLES = builder.comment("Shows recipe progress and failure particles.")
                .define("progressParticles", true);
        RAW_HIDE_DROP_CHANCE = chance(builder, "rawHideDropChance", 0.5D);
        RAW_HIDE_MAX_DROPS = builder.defineInRange("rawHideMaxDrops", 2, 1, 16);

        builder.push("campfire");
        CAMPFIRE_COOK_TICKS = positive(builder, "cookTicks", 90 * 20);
        CAMPFIRE_BURN_TICKS_PER_LOG = positive(builder, "burnTicksPerLog", 2 * 60 * 20);
        CAMPFIRE_BURNED_FOOD_TICKS = positive(builder, "burnedFoodTicks", 30 * 20);
        CAMPFIRE_FULL_SPEED_FUEL_LEVEL = builder.defineInRange("fullSpeedFuelLevel", 4, 1, 8);
        CAMPFIRE_RAIN_EXTINGUISHES = builder.define("rainExtinguishes", true);
        CAMPFIRE_RAIN_EXTINGUISH_TICKS = positive(builder, "rainExtinguishTicks", 10 * 20);
        CAMPFIRE_ASH_CHANCE = chance(builder, "ashChance", 0.25D);
        CAMPFIRE_PLAYER_BURN_CHANCE = chance(builder, "playerBurnChance", 0.5D);
        CAMPFIRE_PLAYER_BURN_DAMAGE = nonNegative(builder, "playerBurnDamage", 1.0D);
        CAMPFIRE_ENTITY_BURN_DAMAGE = nonNegative(builder, "entityBurnDamage", 1.0D);
        CAMPFIRE_FLOOR_IGNITION_CHANCE = chance(builder, "floorIgnitionChance", 0.05D);
        CAMPFIRE_MINIMUM_LIGHT = builder.defineInRange("minimumLight", 3, 0, 15);
        CAMPFIRE_MAXIMUM_LIGHT = builder.defineInRange("maximumLight", 11, 0, 15);
        builder.pop();

        builder.push("choppingBlock");
        CHOPPING_WOOD_CHOPS = positive(builder, "woodTierChops", 6);
        CHOPPING_STONE_CHOPS = positive(builder, "stoneTierChops", 4);
        CHOPPING_IRON_CHOPS = positive(builder, "ironTierChops", 2);
        CHOPPING_DIAMOND_CHOPS = positive(builder, "diamondTierChops", 2);
        CHOPPING_WOOD_OUTPUT = nonNegativeInt(builder, "woodTierOutput", 1);
        CHOPPING_STONE_OUTPUT = nonNegativeInt(builder, "stoneTierOutput", 2);
        CHOPPING_IRON_OUTPUT = nonNegativeInt(builder, "ironTierOutput", 3);
        CHOPPING_DIAMOND_OUTPUT = nonNegativeInt(builder, "diamondTierOutput", 4);
        CHOPPING_CHOPS_PER_DAMAGE = positive(builder, "chopsPerDamageStage", 16);
        CHOPPING_WOOD_CHIPS_CHANCE = chance(builder, "woodChipsChance", 0.05D);
        CHOPPING_EXHAUSTION_PER_CHOP = nonNegative(builder, "exhaustionPerChop", 1.5D);
        CHOPPING_EXHAUSTION_PER_CRAFT = nonNegative(builder, "exhaustionPerCraft", 1.5D);
        CHOPPING_EXHAUSTION_PER_CHIP_SCOOP = nonNegative(builder, "exhaustionPerChipScoop", 0.5D);
        CHOPPING_MINIMUM_HUNGER = builder.defineInRange("minimumHunger", 3, 0, 20);
        CHOPPING_USES_DURABILITY = builder.define("usesDurability", true);
        builder.pop();

        builder.push("pitKiln");
        PIT_KILN_MAX_STACK_SIZE = builder.defineInRange("maxStackSize", 8, 1, 64);
        PIT_KILN_DURATION_MULTIPLIER = nonNegative(builder, "durationMultiplier", 1.0D);
        PIT_KILN_VARIABLE_SPEED = builder.defineInRange("variableSpeedModifier", 0.5D, 0.01D, 1.0D);
        PIT_KILN_RAIN_EXTINGUISHES = builder.define("rainExtinguishes", true);
        PIT_KILN_RAIN_EXTINGUISH_TICKS = positive(builder, "rainExtinguishTicks", 10 * 20);
        builder.pop();

        builder.push("barrel");
        BARREL_CAPACITY = positive(builder, "capacity", 1000);
        BARREL_DURATION_MULTIPLIER = nonNegative(builder, "durationMultiplier", 1.0D);
        BARREL_RAIN_FILL_INTERVAL = nonNegativeInt(builder, "rainFillInterval", 20);
        BARREL_RAIN_CONVERSION_INTERVAL = nonNegativeInt(builder, "rainConversionInterval", 2 * 60 * 20);
        HOT_FLUID_TEMPERATURE = nonNegativeInt(builder, "hotFluidTemperature", 450);
        WOODEN_CONTAINERS_HOLD_HOT_FLUIDS = builder.define("holdsHotFluids", false);
        builder.pop();

        builder.push("soakingPot");
        SOAKING_POT_CAPACITY = positive(builder, "capacity", 4000);
        SOAKING_POT_MAX_STACK_SIZE = builder.defineInRange("maxStackSize", 8, 1, 64);
        SOAKING_POT_DURATION_MULTIPLIER = nonNegative(builder, "durationMultiplier", 1.0D);
        builder.pop();

        builder.push("tanningRack");
        TANNING_RACK_DURATION_MULTIPLIER = nonNegative(builder, "durationMultiplier", 1.0D);
        TANNING_RACK_RAIN_RUIN_TICKS = builder.defineInRange("rainRuinTicks", 2 * 60 * 20, -1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("stoneMachines");
        STONE_MACHINE_INPUT_LIMIT = builder.defineInRange("inputLimit", 8, 1, 64);
        STONE_MACHINE_FUEL_LIMIT = builder.defineInRange("fuelLimit", 16, 1, 64);
        STONE_MACHINE_FUEL_MULTIPLIER = nonNegative(builder, "fuelBurnTimeMultiplier", 1.0D);
        STONE_MACHINE_AIRFLOW_MULTIPLIER = nonNegative(builder, "airflowMultiplier", 1.0D);
        STONE_MACHINE_AIRFLOW_DRAG = builder.defineInRange("airflowDrag", 0.02D, 0.0D, 1.0D);
        STONE_MACHINE_KEEP_HEAT = builder.comment(
                        "When true, a lit machine stays active briefly after input or fuel becomes unavailable.")
                .define("keepHeat", false);

        builder.push("sawmill");
        STONE_SAWMILL_BLADE_DAMAGE = nonNegative(builder, "activeBladeEntityDamage", 3.0D);
        STONE_SAWMILL_DAMAGE_BLADES = builder.define("damageBlades", true);
        STONE_SAWMILL_WOOD_CHIP_CHANCE = chance(builder, "woodChipChance", 0.25D);
        STONE_SAWMILL_IDLE_SOUND_ENABLED = builder.define("idleSoundEnabled", true);
        STONE_SAWMILL_IDLE_SOUND_VOLUME = nonNegative(builder, "idleSoundVolume", 0.5D);
        STONE_SAWMILL_COMPLETE_SOUND_ENABLED = builder.define("recipeCompleteSoundEnabled", true);
        STONE_SAWMILL_COMPLETE_SOUND_VOLUME = nonNegative(builder, "recipeCompleteSoundVolume", 0.75D);
        builder.pop();

        builder.push("oven");
        STONE_OVEN_COOK_TICKS = positive(builder, "foodCookTicks", 2 * 60 * 20);
        STONE_OVEN_DRYING_DURATION_MULTIPLIER = nonNegative(builder, "dryingDurationMultiplier", 0.25D);
        builder.pop();

        builder.push("kiln");
        STONE_KILN_PIT_DURATION_MULTIPLIER = nonNegative(builder, "pitKilnDurationMultiplier", 0.5D);
        STONE_KILN_PIT_FAILURE_MULTIPLIER = nonNegative(builder, "pitKilnFailureMultiplier", 0.25D);
        builder.pop();

        builder.push("crucible");
        STONE_CRUCIBLE_CAPACITY = positive(builder, "capacity", 4000);
        builder.pop(2);

        builder.push("anvil");
        ANVIL_HITS_PER_DAMAGE_STAGE = positive(builder, "hitsPerDamageStage", 64);
        ANVIL_EXHAUSTION_PER_HIT = nonNegative(builder, "exhaustionPerHit", 0.5D);
        ANVIL_EXHAUSTION_PER_CRAFT = nonNegative(builder, "exhaustionPerCraft", 0.0D);
        ANVIL_MINIMUM_HUNGER = builder.defineInRange("minimumHunger", 3, 0, 20);
        ANVIL_USE_TOOL_DURABILITY = builder.define("useToolDurability", true);
        builder.pop();

        builder.push("campfireEffects");
        CAMPFIRE_EFFECTS_ENABLED = builder.define("enabled", true);
        CAMPFIRE_EFFECT_START_TIME = builder.defineInRange("startTime", 12000, 0, 24000);
        CAMPFIRE_EFFECT_STOP_TIME = builder.defineInRange("stopTime", 23000, 0, 24000);
        CAMPFIRE_EFFECT_RANGE = builder.defineInRange("range", 5, 1, 32);
        COMFORT_FOOD_BONUS = nonNegative(builder, "comfortFoodBonus", 0.5D);
        RESTING_REGEN_INTERVAL = positive(builder, "restingRegenInterval", 100);
        RESTING_LEVEL_INTERVAL = positive(builder, "restingLevelInterval", 200);
        WELL_FED_DURATION = nonNegativeInt(builder, "wellFedDuration", 5 * 60 * 20);
        WELL_FED_EXHAUSTION_MODIFIER = builder.defineInRange("wellFedExhaustionModifier", 0.5D, 0.0D, 1.0D);
        WELL_RESTED_DURATION = nonNegativeInt(builder, "wellRestedDuration", 5 * 60 * 20);
        WELL_RESTED_ABSORPTION_HALF_HEARTS = nonNegativeInt(builder, "wellRestedAbsorptionHalfHearts", 4);
        FOCUSED_DURATION = nonNegativeInt(builder, "focusedDuration", 5 * 60 * 20);
        FOCUSED_XP_BONUS = nonNegative(builder, "focusedXpBonus", 1.0D);
        builder.pop(2);
        SPEC = builder.build();
    }

    private PrimitiveTechnologyConfig() {
    }

    private static ModConfigSpec.IntValue positive(ModConfigSpec.Builder builder, String key, int value) {
        return builder.defineInRange(key, value, 1, Integer.MAX_VALUE);
    }

    private static ModConfigSpec.IntValue nonNegativeInt(ModConfigSpec.Builder builder, String key, int value) {
        return builder.defineInRange(key, value, 0, Integer.MAX_VALUE);
    }

    private static ModConfigSpec.DoubleValue nonNegative(ModConfigSpec.Builder builder, String key, double value) {
        return builder.defineInRange(key, value, 0.0D, 1000.0D);
    }

    private static ModConfigSpec.DoubleValue chance(ModConfigSpec.Builder builder, String key, double value) {
        return builder.defineInRange(key, value, 0.0D, 1.0D);
    }
}
