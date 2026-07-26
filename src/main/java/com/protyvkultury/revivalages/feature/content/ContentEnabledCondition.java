package com.protyvkultury.revivalages.feature.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

/** Reload-safe data condition backed by the effective server content state. */
public record ContentEnabledCondition(ResourceLocation content, boolean enabled) implements ICondition {

    public static final MapCodec<ContentEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("content").forGetter(ContentEnabledCondition::content),
                    Codec.BOOL.optionalFieldOf("enabled", true).forGetter(ContentEnabledCondition::enabled)
            ).apply(instance, ContentEnabledCondition::new));

    @Override
    public boolean test(IContext context) {
        return ContentAvailability.isEnabled(content)
                .map(actual -> actual == enabled)
                .orElse(false);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
