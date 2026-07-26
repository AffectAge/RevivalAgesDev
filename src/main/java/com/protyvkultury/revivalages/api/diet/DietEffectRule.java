package com.protyvkultury.revivalages.api.diet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

/**
 * Reloadable threshold rule evaluated against active diet groups.
 */
public record DietEffectRule(
        Holder<MobEffect> effect,
        int amplifier,
        double minimum,
        double maximum,
        DietDetector detector,
        int cumulativeMultiplier,
        List<ResourceLocation> groups
) {

    public static final Codec<DietEffectRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MobEffect.CODEC.fieldOf("effect").forGetter(DietEffectRule::effect),
            Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(DietEffectRule::amplifier),
            Codec.doubleRange(0.0D, 100.0D).fieldOf("minimum").forGetter(DietEffectRule::minimum),
            Codec.doubleRange(0.0D, 100.0D).fieldOf("maximum").forGetter(DietEffectRule::maximum),
            DietDetector.CODEC.fieldOf("detector").forGetter(DietEffectRule::detector),
            Codec.intRange(1, 255)
                    .optionalFieldOf("cumulative_multiplier", 1)
                    .forGetter(DietEffectRule::cumulativeMultiplier),
            ResourceLocation.CODEC.listOf().optionalFieldOf("groups", List.of()).forGetter(DietEffectRule::groups)
    ).apply(instance, DietEffectRule::new));

    public DietEffectRule {
        if (minimum > maximum) {
            throw new IllegalArgumentException("Diet effect minimum cannot exceed maximum");
        }
        groups = List.copyOf(groups);
    }
}
