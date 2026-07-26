package com.protyvkultury.revivalages.api.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Reloadable preservation modifier. Values below one extend shelf life.
 */
public record FoodTrait(double decayMultiplier, String translationKey) {

    public static final Codec<FoodTrait> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.doubleRange(Double.MIN_NORMAL, 1_000.0D)
                    .fieldOf("decay_multiplier")
                    .forGetter(FoodTrait::decayMultiplier),
            Codec.STRING.fieldOf("translation_key").forGetter(FoodTrait::translationKey)
    ).apply(instance, FoodTrait::new));

    public FoodTrait {
        if (translationKey.isBlank()) {
            throw new IllegalArgumentException("Food trait translation key cannot be blank");
        }
    }
}
