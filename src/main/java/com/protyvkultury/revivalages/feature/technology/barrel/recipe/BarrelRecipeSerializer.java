package com.protyvkultury.revivalages.feature.technology.barrel.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public final class BarrelRecipeSerializer implements RecipeSerializer<BarrelRecipe> {

    private static final Codec<List<Ingredient>> INGREDIENTS = Ingredient.CODEC_NONEMPTY.listOf().validate(values ->
            !values.isEmpty() && values.size() <= 4
                    ? com.mojang.serialization.DataResult.success(values)
                    : com.mojang.serialization.DataResult.error(() -> "items must contain one to four ingredients"));
    private static final MapCodec<BarrelRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            INGREDIENTS.fieldOf("items").forGetter(BarrelRecipe::itemIngredients),
            FluidStack.CODEC.fieldOf("input_fluid").forGetter(BarrelRecipe::inputFluid),
            FluidStack.CODEC.fieldOf("result_fluid").forGetter(BarrelRecipe::resultFluid),
            Codec.INT.validate(value -> value > 0
                            ? com.mojang.serialization.DataResult.success(value)
                            : com.mojang.serialization.DataResult.error(() -> "processing_time must be positive"))
                    .fieldOf("processing_time").forGetter(BarrelRecipe::processingTime)
    ).apply(instance, BarrelRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, BarrelRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(java.util.ArrayList::new, Ingredient.CONTENTS_STREAM_CODEC),
            BarrelRecipe::itemIngredients,
            FluidStack.STREAM_CODEC,
            BarrelRecipe::inputFluid,
            FluidStack.STREAM_CODEC,
            BarrelRecipe::resultFluid,
            ByteBufCodecs.VAR_INT,
            BarrelRecipe::processingTime,
            BarrelRecipe::new
    );

    @Override
    public MapCodec<BarrelRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BarrelRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
