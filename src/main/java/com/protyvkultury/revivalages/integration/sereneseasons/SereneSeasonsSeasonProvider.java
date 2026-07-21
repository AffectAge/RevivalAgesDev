package com.protyvkultury.revivalages.integration.sereneseasons;

import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.SeasonProvider;
import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.SeasonType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import sereneseasons.api.season.SeasonHelper;

public final class SereneSeasonsSeasonProvider implements SeasonProvider {

    @Override
    public SeasonType seasonAt(Level level, BlockPos pos) {
        return switch (SeasonHelper.getSeasonState(level).getSeason()) {
            case SPRING -> SeasonType.SPRING;
            case SUMMER -> SeasonType.SUMMER;
            case AUTUMN -> SeasonType.AUTUMN;
            case WINTER -> SeasonType.WINTER;
        };
    }
}
