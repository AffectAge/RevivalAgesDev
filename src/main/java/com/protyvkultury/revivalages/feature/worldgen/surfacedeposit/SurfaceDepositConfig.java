package com.protyvkultury.revivalages.feature.worldgen.surfacedeposit;

import com.protyvkultury.revivalages.feature.content.ContentKey;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Restart-required availability settings for surface deposits. */
public final class SurfaceDepositConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue ROCKS_ENABLED;
    public static final ModConfigSpec.BooleanValue STICKS_ENABLED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("surfaceDeposits");
        ENABLED = toggle(builder, "enabled", "Enables the complete Surface Deposits feature family.");
        ROCKS_ENABLED = toggle(builder, "rocksEnabled", "Enables surface rocks and splitters.");
        STICKS_ENABLED = toggle(builder, "sticksEnabled", "Enables surface sticks.");
        builder.pop();
        SPEC = builder.build();
    }

    private SurfaceDepositConfig() {
    }

    public static boolean contentEnabled(ContentKey key) {
        ModConfigSpec.BooleanValue value = switch (key) {
            case SURFACE_DEPOSITS -> ENABLED;
            case SURFACE_ROCKS -> ROCKS_ENABLED;
            case SURFACE_STICKS -> STICKS_ENABLED;
            default -> throw new IllegalArgumentException("Not a surface deposit key: " + key);
        };
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    private static ModConfigSpec.BooleanValue toggle(
            ModConfigSpec.Builder builder,
            String name,
            String comment
    ) {
        return builder.comment(comment, "Changing this value requires a server restart.")
                .define(name, true);
    }
}
