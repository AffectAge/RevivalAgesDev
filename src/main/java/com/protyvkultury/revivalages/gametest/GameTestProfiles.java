package com.protyvkultury.revivalages.gametest;

import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import net.minecraft.gametest.framework.GameTestHelper;

final class GameTestProfiles {

    private GameTestProfiles() {
    }

    static boolean requireEnabledContent(GameTestHelper helper) {
        if (!ContentAvailability.isForcedAllDisabled()) {
            return true;
        }
        helper.succeed();
        return false;
    }

    static boolean requireDisabledContent(GameTestHelper helper) {
        if (ContentAvailability.isForcedAllDisabled()) {
            return true;
        }
        helper.succeed();
        return false;
    }
}
