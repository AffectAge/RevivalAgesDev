package com.protyvkultury.revivalages.api.weight;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Context propagated through nested weight calculations.
 */
public record WeightContext(
        @Nullable Level level,
        @Nullable Entity entity,
        int depth,
        int maximumDepth
) {

    public WeightContext {
        depth = Math.max(0, depth);
        maximumDepth = Math.max(0, maximumDepth);
    }

    public WeightContext nested() {
        return new WeightContext(level, entity, depth + 1, maximumDepth);
    }

    public boolean recursionLimitExceeded() {
        return depth > maximumDepth;
    }
}
