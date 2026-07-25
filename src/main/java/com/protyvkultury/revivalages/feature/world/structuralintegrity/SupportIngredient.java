package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public record SupportIngredient(Map<String, String> properties) {

    public static final SupportIngredient ANY = new SupportIngredient(Map.of());
    public static final Codec<SupportIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.STRING)
                    .optionalFieldOf("properties", Map.of())
                    .forGetter(SupportIngredient::properties)
    ).apply(instance, SupportIngredient::new));

    public SupportIngredient {
        properties = Map.copyOf(properties);
    }

    public boolean test(BlockState state) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            Property<?> property = state.getBlock().getStateDefinition().getProperty(entry.getKey());
            if (property == null || !matches(state, property, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static <T extends Comparable<T>> boolean matches(BlockState state, Property<T> property, String value) {
        return property.getValue(value)
                .map(expected -> state.getValue(property).equals(expected))
                .orElse(false);
    }
}
