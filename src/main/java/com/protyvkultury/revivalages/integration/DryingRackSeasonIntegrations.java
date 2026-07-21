package com.protyvkultury.revivalages.integration;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.SeasonProvider;
import com.protyvkultury.revivalages.integration.eclipticseasons.EclipticSeasonsSeasonProvider;
import com.protyvkultury.revivalages.integration.sereneseasons.SereneSeasonsSeasonProvider;
import net.neoforged.fml.ModList;

public final class DryingRackSeasonIntegrations {

    private DryingRackSeasonIntegrations() {
    }

    public static SeasonProvider createProvider() {
        if (ModList.get().isLoaded("eclipticseasons")) {
            RevivalAges.LOGGER.info("Drying Rack seasonal integration: Ecliptic Seasons");
            return new EclipticSeasonsSeasonProvider();
        }
        if (ModList.get().isLoaded("sereneseasons")) {
            RevivalAges.LOGGER.info("Drying Rack seasonal integration: Serene Seasons");
            return new SereneSeasonsSeasonProvider();
        }
        return SeasonProvider.NONE;
    }
}
