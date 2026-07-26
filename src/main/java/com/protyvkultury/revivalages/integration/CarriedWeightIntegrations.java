package com.protyvkultury.revivalages.integration;

import com.protyvkultury.revivalages.integration.curios.CuriosCarriedWeightIntegration;
import net.neoforged.fml.ModList;

public final class CarriedWeightIntegrations {

    private CarriedWeightIntegrations() {
    }

    public static void register() {
        if (ModList.get().isLoaded("curios")) {
            CuriosCarriedWeightIntegration.register();
        }
    }
}
