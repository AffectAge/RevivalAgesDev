package com.protyvkultury.revivalages.feature.food.spoilage.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class FoodSpoilageClientConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<TooltipMode> TOOLTIP_MODE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("foodSpoilage");
        TOOLTIP_MODE = builder.defineEnum("tooltipMode", TooltipMode.TIME_LEFT);
        builder.pop();
        SPEC = builder.build();
    }

    private FoodSpoilageClientConfig() {
    }

    public enum TooltipMode {
        TIME_LEFT,
        OFF
    }
}
