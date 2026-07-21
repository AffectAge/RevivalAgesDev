package com.protyvkultury.revivalages.feature.technology.dryingrack.environment;

import java.util.Objects;

/**
 * Holds the season provider selected once by the mod composition root.
 */
public final class DryingRackSeasonService {

    private static SeasonProvider provider = SeasonProvider.NONE;
    private static boolean installed;

    private DryingRackSeasonService() {
    }

    public static synchronized void install(SeasonProvider selectedProvider) {
        if (installed) {
            throw new IllegalStateException("The drying-rack season provider was already installed");
        }
        provider = Objects.requireNonNull(selectedProvider);
        installed = true;
    }

    public static SeasonProvider provider() {
        return provider;
    }
}
