package com.protyvkultury.revivalages.feature.technology.dryingrack.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Loader-neutral seasonal boundary used by optional season-mod adapters.
 */
@FunctionalInterface
public interface SeasonProvider {

    SeasonProvider NONE = (level, pos) -> SeasonType.NONE;

    SeasonType seasonAt(Level level, BlockPos pos);
}
