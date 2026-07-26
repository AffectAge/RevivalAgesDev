package com.protyvkultury.revivalages.feature.inventory.carriedweight.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CarriedWeightClientConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_TOOLTIPS;
    public static final ModConfigSpec.BooleanValue SHOW_HUD;
    public static final ModConfigSpec.EnumValue<HudStyle> HUD_STYLE;
    public static final ModConfigSpec.EnumValue<HudPosition> HUD_POSITION;
    public static final ModConfigSpec.DoubleValue CUSTOM_X;
    public static final ModConfigSpec.DoubleValue CUSTOM_Y;
    public static final ModConfigSpec.IntValue SPRITE_SIZE;
    public static final ModConfigSpec.IntValue BAR_WIDTH;
    public static final ModConfigSpec.IntValue BAR_HEIGHT;
    public static final ModConfigSpec.EnumValue<HudTextMode> TEXT_MODE;
    public static final ModConfigSpec.EnumValue<HudTextPosition> TEXT_POSITION;
    public static final ModConfigSpec.IntValue TEXT_X_OFFSET;
    public static final ModConfigSpec.IntValue TEXT_Y_OFFSET;
    public static final ModConfigSpec.IntValue TEXT_COLOR;
    public static final ModConfigSpec.BooleanValue TEXT_SHADOW;
    public static final ModConfigSpec.BooleanValue KEEP_TEXT_ON_SCREEN;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("carriedWeight");
        SHOW_TOOLTIPS = builder.define("showTooltips", true);
        SHOW_HUD = builder.define("showHud", true);
        HUD_STYLE = builder.defineEnum("hud.style", HudStyle.SPRITE);
        HUD_POSITION = builder.defineEnum("hud.position", HudPosition.BOTTOM_RIGHT);
        CUSTOM_X = builder.defineInRange("hud.customX", 0.5D, 0.0D, 1.0D);
        CUSTOM_Y = builder.defineInRange("hud.customY", 0.5D, 0.0D, 1.0D);
        SPRITE_SIZE = builder.defineInRange("hud.spriteSize", 16, 8, 128);
        BAR_WIDTH = builder.defineInRange("hud.barWidth", 82, 16, 512);
        BAR_HEIGHT = builder.defineInRange("hud.barHeight", 10, 2, 128);
        TEXT_MODE = builder.defineEnum("hud.textMode", HudTextMode.CURRENT_MAX);
        TEXT_POSITION = builder.defineEnum("hud.textPosition", HudTextPosition.BELOW);
        TEXT_X_OFFSET = builder.defineInRange("hud.textXOffset", 0, -2_048, 2_048);
        TEXT_Y_OFFSET = builder.defineInRange("hud.textYOffset", 0, -2_048, 2_048);
        TEXT_COLOR = builder.defineInRange("hud.textColor", 0xFFFFFF, 0, 0xFFFFFF);
        TEXT_SHADOW = builder.define("hud.textShadow", true);
        KEEP_TEXT_ON_SCREEN = builder.define("hud.keepTextOnScreen", true);
        builder.pop();
        SPEC = builder.build();
    }

    private CarriedWeightClientConfig() {
    }

    public enum HudStyle {
        SPRITE,
        BAR
    }

    public enum HudPosition {
        TOP_LEFT,
        TOP_RIGHT,
        CENTER_LEFT,
        CENTER_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        HOTBAR_LEFT,
        HOTBAR_RIGHT,
        CENTER_HOTBAR,
        CUSTOM
    }

    public enum HudTextMode {
        NONE,
        CURRENT,
        MAX,
        PERCENT,
        REMAINING,
        CURRENT_MAX,
        CURRENT_MAX_PERCENT
    }

    public enum HudTextPosition {
        ABOVE,
        BELOW,
        LEFT,
        RIGHT,
        INSIDE,
        CUSTOM
    }
}
