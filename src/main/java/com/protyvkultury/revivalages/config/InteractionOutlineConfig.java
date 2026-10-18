package com.protyvkultury.revivalages.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Shared client color for physical interaction outlines. */
public final class InteractionOutlineConfig {

    public static final ModConfigSpec.ConfigValue<String> COLOR;

    private static String cachedColor = "";
    private static int cachedRgb = 0x007FBD;

    static {
        ModConfigSpec.Builder builder = RevivalAgesConfig.builder();
        builder.push("client");
        COLOR = builder
                .comment("RGB color for interaction outlines, as six hexadecimal digits with an optional # prefix.")
                .translation("config.revivalages.interaction_outline_color")
                .define("interactionOutlineColor", "007FBD",
                        value -> value instanceof String color && color.matches("#?[0-9a-fA-F]{6}"));
        builder.pop();
    }

    private InteractionOutlineConfig() {
    }

    public static void bootstrap() {
    }

    public static int rgb() {
        String configured = COLOR.get();
        if (!configured.equals(cachedColor)) {
            cachedRgb = Integer.parseInt(configured.charAt(0) == '#' ? configured.substring(1) : configured, 16);
            cachedColor = configured;
        }
        return cachedRgb;
    }
}
