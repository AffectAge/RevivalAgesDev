package com.protyvkultury.revivalages.feature.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

/** Data condition for resources shared by more than one content consumer. */
public record AnyContentEnabledCondition(List<ResourceLocation> contents, boolean enabled) implements ICondition {

    public static final MapCodec<AnyContentEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.listOf().fieldOf("contents")
                            .forGetter(AnyContentEnabledCondition::contents),
                    Codec.BOOL.optionalFieldOf("enabled", true)
                            .forGetter(AnyContentEnabledCondition::enabled)
            ).apply(instance, AnyContentEnabledCondition::new));

    public AnyContentEnabledCondition {
        contents = List.copyOf(contents);
        if (contents.isEmpty()) {
            throw new IllegalArgumentException("contents must not be empty");
        }
    }

    @Override
    public boolean test(IContext context) {
        boolean anyEnabled = contents.stream()
                .map(ContentAvailability::isEnabled)
                .flatMap(java.util.Optional::stream)
                .anyMatch(Boolean::booleanValue);
        return anyEnabled == enabled;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
