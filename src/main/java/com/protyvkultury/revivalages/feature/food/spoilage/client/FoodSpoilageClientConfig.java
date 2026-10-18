package com.protyvkultury.revivalages.feature.food.spoilage.client;

import com.protyvkultury.revivalages.config.RevivalAgesConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class FoodSpoilageClientConfig {

    public static final ModConfigSpec.EnumValue<TooltipMode> TOOLTIP_MODE;

    static {
        ModConfigSpec.Builder builder = RevivalAgesConfig.builder();
        builder.push("foodSpoilage");
        TOOLTIP_MODE = builder.defineEnum("tooltipMode", TooltipMode.TIME_LEFT);
        builder.pop();
    }

    private FoodSpoilageClientConfig() {
    }

    public static void bootstrap() {
    }

    public enum TooltipMode {
        TIME_LEFT,
        OFF
    }
}
