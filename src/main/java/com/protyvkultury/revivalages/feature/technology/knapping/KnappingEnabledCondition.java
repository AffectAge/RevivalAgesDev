package com.protyvkultury.revivalages.feature.technology.knapping;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;

public record KnappingEnabledCondition(boolean enabled) implements ICondition {

    public static final MapCodec<KnappingEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(Codec.BOOL.fieldOf("enabled").forGetter(KnappingEnabledCondition::enabled))
                    .apply(instance, KnappingEnabledCondition::new));

    @Override
    public boolean test(IContext context) {
        return KnappingConfig.enabled() == enabled;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
