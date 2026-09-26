package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import com.protyvkultury.revivalages.config.RevivalAgesConfig;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-owned availability, balance, and work-budget settings for structural simulation. */
public final class StructuralIntegrityConfig {

    public static final ModConfigSpec.IntValue VERTICAL_AUTO_STACK;
    public static final ModConfigSpec.IntValue HORIZONTAL_MAX_SPAN;
    public static final ModConfigSpec.IntValue SAW_DAMAGE;
    public static final ModConfigSpec.IntValue LANDSLIDE_DELAY;
    public static final ModConfigSpec.DoubleValue COLLAPSE_TRIGGER_CHANCE;
    public static final ModConfigSpec.DoubleValue COLLAPSE_FAKE_TRIGGER_CHANCE;
    public static final ModConfigSpec.DoubleValue COLLAPSE_PROPAGATE_CHANCE;
    public static final ModConfigSpec.DoubleValue EXPLOSION_PROPAGATE_CHANCE;
    public static final ModConfigSpec.IntValue COLLAPSE_MIN_RADIUS;
    public static final ModConfigSpec.IntValue COLLAPSE_RADIUS_VARIANCE;
    public static final ModConfigSpec.IntValue PROPAGATION_INTERVAL;
    public static final ModConfigSpec.DoubleValue COLLAPSE_DAMAGE_PER_BLOCK;
    public static final ModConfigSpec.IntValue COLLAPSE_MAX_DAMAGE;
    public static final ModConfigSpec.DoubleValue LANDSLIDE_DAMAGE_PER_BLOCK;
    public static final ModConfigSpec.IntValue LANDSLIDE_MAX_DAMAGE;
    public static final ModConfigSpec.IntValue MAX_QUEUED_UPDATES;
    public static final ModConfigSpec.IntValue UPDATE_BUDGET_PER_TICK;
    public static final ModConfigSpec.DoubleValue CAMERA_SHAKE_RADIUS;
    public static final ModConfigSpec.DoubleValue WARNING_SHAKE_STRENGTH;
    public static final ModConfigSpec.IntValue WARNING_SHAKE_DURATION;
    public static final ModConfigSpec.DoubleValue COLLAPSE_SHAKE_STRENGTH;
    public static final ModConfigSpec.IntValue COLLAPSE_SHAKE_DURATION;
    public static final ModConfigSpec.DoubleValue PROPAGATION_SHAKE_STRENGTH;
    public static final ModConfigSpec.IntValue PROPAGATION_SHAKE_DURATION;
    public static final ModConfigSpec.BooleanValue CAMERA_SHAKE_ENABLED;
    public static final ModConfigSpec.DoubleValue CAMERA_SHAKE_INTENSITY;

    static {
        ModConfigSpec.Builder builder = RevivalAgesConfig.builder();
        builder.push("structuralIntegrity");

        builder.push("supportBeams");
        VERTICAL_AUTO_STACK = builder.defineInRange("verticalAutoStack", 3, 1, 16);
        HORIZONTAL_MAX_SPAN = builder.defineInRange("horizontalMaxSpan", 5, 1, 32);
        SAW_DAMAGE = builder.defineInRange("sawDamage", 1, 0, 1024);
        builder.pop();

        builder.push("collapses");
        COLLAPSE_TRIGGER_CHANCE = builder.defineInRange("triggerChance", 0.1D, 0D, 1D);
        COLLAPSE_FAKE_TRIGGER_CHANCE = builder.defineInRange("fakeTriggerChance", 0.35D, 0D, 1D);
        COLLAPSE_PROPAGATE_CHANCE = builder.defineInRange("propagateChance", 0.55D, 0D, 1D);
        EXPLOSION_PROPAGATE_CHANCE = builder.defineInRange("explosionPropagateChance", 0.3D, 0D, 1D);
        COLLAPSE_MIN_RADIUS = builder.defineInRange("minimumRadius", 3, 1, 32);
        COLLAPSE_RADIUS_VARIANCE = builder.defineInRange("radiusVariance", 16, 1, 32);
        PROPAGATION_INTERVAL = builder.defineInRange("propagationInterval", 10, 1, 200);
        COLLAPSE_DAMAGE_PER_BLOCK = builder.defineInRange("damagePerBlock", 2D, 0D, 100D);
        COLLAPSE_MAX_DAMAGE = builder.defineInRange("maximumDamage", 20, 0, 1000);
        builder.pop();

        builder.push("landslides");
        LANDSLIDE_DELAY = builder.defineInRange("delayTicks", 2, 0, 200);
        LANDSLIDE_DAMAGE_PER_BLOCK = builder.defineInRange("damagePerBlock", 0.8D, 0D, 100D);
        LANDSLIDE_MAX_DAMAGE = builder.defineInRange("maximumDamage", 10, 0, 1000);
        builder.pop();

        builder.push("workBudget");
        MAX_QUEUED_UPDATES = builder.defineInRange("maximumQueuedPositions", 65_536, 256, 1_048_576);
        UPDATE_BUDGET_PER_TICK = builder.defineInRange("positionsPerTick", 4_096, 16, 65_536);
        builder.pop();

        builder.push("cameraShake");
        CAMERA_SHAKE_RADIUS = builder.defineInRange("radius", 48.0D, 0.0D, 256.0D);
        WARNING_SHAKE_STRENGTH = builder.defineInRange("warningStrength", 0.35D, 0.0D, 10.0D);
        WARNING_SHAKE_DURATION = builder.defineInRange("warningDurationTicks", 18, 0, 200);
        COLLAPSE_SHAKE_STRENGTH = builder.defineInRange("collapseStrength", 1.25D, 0.0D, 10.0D);
        COLLAPSE_SHAKE_DURATION = builder.defineInRange("collapseDurationTicks", 36, 0, 200);
        PROPAGATION_SHAKE_STRENGTH = builder.defineInRange("propagationStrength", 0.65D, 0.0D, 10.0D);
        PROPAGATION_SHAKE_DURATION = builder.defineInRange("propagationDurationTicks", 14, 0, 200);
        builder.pop();

        builder.pop();

        builder.push("structuralIntegrity");
        builder.push("cameraShake");
        CAMERA_SHAKE_ENABLED = builder.define("enabled", true);
        CAMERA_SHAKE_INTENSITY = builder.defineInRange("intensity", 1.0D, 0.0D, 2.0D);
        builder.pop(2);
    }

    private StructuralIntegrityConfig() {
    }

    public static boolean supportBeamsEnabled() {
        return ContentAvailability.isEnabled(ContentKey.SUPPORT_BEAMS);
    }

    public static boolean collapsesEnabled() {
        return ContentAvailability.isEnabled(ContentKey.COLLAPSES);
    }

    public static boolean landslidesEnabled() {
        return ContentAvailability.isEnabled(ContentKey.LANDSLIDES);
    }

    public static boolean cameraShakeEnabled() {
        return RevivalAgesConfig.isLoaded()
                ? CAMERA_SHAKE_ENABLED.get()
                : CAMERA_SHAKE_ENABLED.getDefault();
    }

    public static double cameraShakeIntensity() {
        return RevivalAgesConfig.isLoaded()
                ? CAMERA_SHAKE_INTENSITY.get()
                : CAMERA_SHAKE_INTENSITY.getDefault();
    }

    public static void bootstrap() {
    }
}
