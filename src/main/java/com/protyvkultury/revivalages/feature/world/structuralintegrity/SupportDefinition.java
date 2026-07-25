package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

public record SupportDefinition(
        SupportIngredient ingredient,
        int supportUp,
        int supportDown,
        int supportHorizontal
) {

    public static final Codec<SupportDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SupportIngredient.CODEC.optionalFieldOf("ingredient", SupportIngredient.ANY)
                    .forGetter(SupportDefinition::ingredient),
            Codec.intRange(0, 32).fieldOf("support_up").forGetter(SupportDefinition::supportUp),
            Codec.intRange(0, 32).fieldOf("support_down").forGetter(SupportDefinition::supportDown),
            Codec.intRange(0, 32).fieldOf("support_horizontal").forGetter(SupportDefinition::supportHorizontal)
    ).apply(instance, SupportDefinition::new));

    public boolean matches(net.minecraft.world.level.block.state.BlockState state) {
        return ingredient.test(state);
    }

    public boolean supports(BlockPos source, BlockPos target) {
        BlockPos difference = target.subtract(source);
        return new SupportRange(supportUp, supportDown, supportHorizontal).contains(
                difference.getX(),
                difference.getY(),
                difference.getZ()
        );
    }
}
