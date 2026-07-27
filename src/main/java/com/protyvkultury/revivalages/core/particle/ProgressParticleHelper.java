package com.protyvkultury.revivalages.core.particle;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;

/** Shared reference-style visual feedback for active physical processing. */
public final class ProgressParticleHelper {

    public static final int INTERVAL = 40;

    private ProgressParticleHelper() {
    }

    public static void spawn(
            Level level,
            double centerX,
            double centerY,
            double centerZ,
            double rangeX,
            double rangeY,
            double rangeZ
    ) {
        level.addParticle(
                ParticleTypes.HAPPY_VILLAGER,
                centerX + symmetric(level, rangeX),
                centerY + symmetric(level, rangeY),
                centerZ + symmetric(level, rangeZ),
                level.random.nextGaussian() * 0.02D,
                level.random.nextGaussian() * 0.02D,
                level.random.nextGaussian() * 0.02D
        );
    }

    private static double symmetric(Level level, double range) {
        return (level.random.nextDouble() * 2.0D - 1.0D) * range;
    }
}
