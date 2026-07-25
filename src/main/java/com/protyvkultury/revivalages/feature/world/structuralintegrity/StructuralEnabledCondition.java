package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.conditions.ICondition;

public record StructuralEnabledCondition(Scope scope, boolean enabled) implements ICondition {

    public static final MapCodec<StructuralEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Scope.CODEC.fieldOf("scope").forGetter(StructuralEnabledCondition::scope),
                    Codec.BOOL.fieldOf("enabled").forGetter(StructuralEnabledCondition::enabled)
            ).apply(instance, StructuralEnabledCondition::new));

    @Override
    public boolean test(IContext context) {
        boolean actual = switch (scope) {
            case SUPPORT_BEAMS -> StructuralIntegrityConfig.supportBeamsEnabled();
            case COLLAPSES -> StructuralIntegrityConfig.collapsesEnabled();
            case LANDSLIDES -> StructuralIntegrityConfig.landslidesEnabled();
        };
        return actual == enabled;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    public enum Scope implements StringRepresentable {
        SUPPORT_BEAMS("support_beams"),
        COLLAPSES("collapses"),
        LANDSLIDES("landslides");

        public static final Codec<Scope> CODEC = StringRepresentable.fromEnum(Scope::values);

        private final String serializedName;

        Scope(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
