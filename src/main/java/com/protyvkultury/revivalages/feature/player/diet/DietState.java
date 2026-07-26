package com.protyvkultury.revivalages.feature.player.diet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import com.protyvkultury.revivalages.api.diet.DietMath;

public record DietState(Map<ResourceLocation, Double> values, int lastFoodLevel) {

    private static final Codec<Map<ResourceLocation, Double>> VALUES_CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.doubleRange(0.0D, 100.0D));
    public static final Codec<DietState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VALUES_CODEC.optionalFieldOf("values", Map.of()).forGetter(DietState::values),
            Codec.intRange(-1, 20).optionalFieldOf("last_food_level", -1).forGetter(DietState::lastFoodLevel)
    ).apply(instance, DietState::new));
    public static final DietState EMPTY = new DietState(Map.of(), -1);

    public DietState {
        values = Map.copyOf(new LinkedHashMap<>(values));
    }

    public DietState withFoodLevel(int foodLevel) {
        return new DietState(values, Math.clamp(foodLevel, 0, 20));
    }

    public DietState withValue(ResourceLocation group, double value) {
        Map<ResourceLocation, Double> changed = new LinkedHashMap<>(values);
        changed.put(group, Math.clamp(value, 0.0D, 100.0D));
        return new DietState(changed, lastFoodLevel);
    }

    public DietState penalized(double penalty, double minimum) {
        Map<ResourceLocation, Double> changed = new LinkedHashMap<>();
        values.forEach((id, value) -> changed.put(id, DietMath.deathPenalty(value, penalty, minimum)));
        return new DietState(changed, -1);
    }
}
