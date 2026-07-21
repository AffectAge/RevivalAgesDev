package com.protyvkultury.revivalages.integration.eclipticseasons;

import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.SeasonProvider;
import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.SeasonType;
import com.teamtea.eclipticseasons.api.EclipticSeasonsApi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class EclipticSeasonsSeasonProvider implements SeasonProvider {

    @Override
    public SeasonType seasonAt(Level level, BlockPos pos) {
        return switch (EclipticSeasonsApi.getInstance().getSeasonSignal(level, pos)) {
            case SPRING -> SeasonType.SPRING;
            case SUMMER -> SeasonType.SUMMER;
            case AUTUMN -> SeasonType.AUTUMN;
            case WINTER -> SeasonType.WINTER;
            case NONE -> SeasonType.NONE;
        };
    }
}
