package com.protyvkultury.revivalages.feature.technology.campfire.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class CampfireMobEffect extends MobEffect {

    public CampfireMobEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }
}
