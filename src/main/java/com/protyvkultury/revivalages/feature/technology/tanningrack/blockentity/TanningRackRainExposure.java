package com.protyvkultury.revivalages.feature.technology.tanningrack.blockentity;

final class TanningRackRainExposure {

    private TanningRackRainExposure() {
    }

    static int next(int current, int limit, boolean raining, boolean hasRainFailure) {
        if (limit < 0 || !raining || !hasRainFailure || current >= limit) {
            return current;
        }
        return current + 1;
    }
}
