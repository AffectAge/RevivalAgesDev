package com.protyvkultury.revivalages.api.diet;

import com.mojang.serialization.Codec;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

/**
 * Per-item multipliers for one or more diet groups.
 */
public record DietContribution(Map<ResourceLocation, Double> groups) {

    private static final Codec<Map<ResourceLocation, Double>> GROUPS_CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.doubleRange(0.0D, 1_000.0D));
    public static final Codec<DietContribution> CODEC =
            GROUPS_CODEC.xmap(DietContribution::new, DietContribution::groups);

    public DietContribution {
        groups = Map.copyOf(new LinkedHashMap<>(groups));
        if (groups.isEmpty()) {
            throw new IllegalArgumentException("Diet contribution must contain at least one group");
        }
    }
}
