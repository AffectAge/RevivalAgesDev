package com.protyvkultury.revivalages.feature.player.diet.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class DietClientConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_TOOLTIPS;
    public static final ModConfigSpec.BooleanValue SHOW_INVENTORY_BUTTON;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("diet");
        SHOW_TOOLTIPS = builder.define("showTooltips", true);
        SHOW_INVENTORY_BUTTON = builder.define("showInventoryButton", true);
        builder.pop();
        SPEC = builder.build();
    }

    private DietClientConfig() {
    }
}
