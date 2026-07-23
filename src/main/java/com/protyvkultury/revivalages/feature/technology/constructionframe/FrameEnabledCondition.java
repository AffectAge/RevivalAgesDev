package com.protyvkultury.revivalages.feature.technology.constructionframe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;

/** Data-load condition selecting either frame assembly or its two-dimensional fallback. */
public record FrameEnabledCondition(boolean enabled) implements ICondition {

    public static final MapCodec<FrameEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(Codec.BOOL.fieldOf("enabled").forGetter(FrameEnabledCondition::enabled))
                    .apply(instance, FrameEnabledCondition::new));

    @Override
    public boolean test(IContext context) {
        return ConstructionFrameConfig.enabled() == enabled;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
