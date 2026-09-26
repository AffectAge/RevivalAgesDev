package com.protyvkultury.revivalages.feature.player.diet.client;

import com.protyvkultury.revivalages.config.RevivalAgesConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class DietClientConfig {

    public static final ModConfigSpec.BooleanValue SHOW_TOOLTIPS;
    public static final ModConfigSpec.BooleanValue SHOW_INVENTORY_BUTTON;

    static {
        ModConfigSpec.Builder builder = RevivalAgesConfig.builder();
        builder.push("diet");
        SHOW_TOOLTIPS = builder.define("showTooltips", true);
        SHOW_INVENTORY_BUTTON = builder.define("showInventoryButton", true);
        builder.pop();
    }

    private DietClientConfig() {
    }

    public static void bootstrap() {
    }
}
