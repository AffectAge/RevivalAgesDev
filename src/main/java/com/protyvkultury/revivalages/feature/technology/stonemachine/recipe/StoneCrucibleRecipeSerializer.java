package com.protyvkultury.revivalages.feature.technology.stonemachine.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public final class StoneCrucibleRecipeSerializer implements RecipeSerializer<StoneCrucibleRecipe> {

    private static final MapCodec<StoneCrucibleRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(StoneCrucibleRecipe::ingredient),
            FluidStack.CODEC.fieldOf("result").forGetter(StoneCrucibleRecipe::result),
            Codec.INT.validate(value -> value > 0
                            ? com.mojang.serialization.DataResult.success(value)
                            : com.mojang.serialization.DataResult.error(() -> "processing_time must be positive"))
                    .fieldOf("processing_time").forGetter(StoneCrucibleRecipe::processingTime)
    ).apply(instance, StoneCrucibleRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, StoneCrucibleRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC,
                    StoneCrucibleRecipe::ingredient,
                    FluidStack.STREAM_CODEC,
                    StoneCrucibleRecipe::result,
                    ByteBufCodecs.VAR_INT,
                    StoneCrucibleRecipe::processingTime,
                    StoneCrucibleRecipe::new
            );

    @Override
    public MapCodec<StoneCrucibleRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, StoneCrucibleRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
