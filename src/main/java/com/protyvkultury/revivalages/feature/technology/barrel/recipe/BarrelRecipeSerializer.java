package com.protyvkultury.revivalages.feature.technology.barrel.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public final class BarrelRecipeSerializer implements RecipeSerializer<BarrelRecipe> {

    private static final Codec<List<CountedBarrelIngredient>> INGREDIENTS =
            CountedBarrelIngredient.CODEC.listOf().validate(values ->
            !values.isEmpty() && values.size() <= 4
                    ? com.mojang.serialization.DataResult.success(values)
                    : com.mojang.serialization.DataResult.error(() -> "items must contain one to four ingredients"));
    private static final MapCodec<BarrelRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            INGREDIENTS.fieldOf("items").forGetter(BarrelRecipe::countedIngredients),
            FluidStack.CODEC.optionalFieldOf("input_fluid", FluidStack.EMPTY).forGetter(BarrelRecipe::inputFluid),
            FluidStack.CODEC.optionalFieldOf("result_fluid", FluidStack.EMPTY).forGetter(BarrelRecipe::resultFluid),
            ItemStack.STRICT_CODEC.optionalFieldOf("result_item", ItemStack.EMPTY).forGetter(BarrelRecipe::resultItem),
            Codec.BOOL.optionalFieldOf("requires_seal", true).forGetter(BarrelRecipe::requiresSeal),
            Codec.INT.validate(value -> value > 0
                            ? com.mojang.serialization.DataResult.success(value)
                            : com.mojang.serialization.DataResult.error(() -> "processing_time must be positive"))
                    .fieldOf("processing_time").forGetter(BarrelRecipe::processingTime)
    ).apply(instance, BarrelRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, BarrelRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(java.util.ArrayList::new, CountedBarrelIngredient.STREAM_CODEC),
            BarrelRecipe::countedIngredients,
            FluidStack.OPTIONAL_STREAM_CODEC,
            BarrelRecipe::inputFluid,
            FluidStack.OPTIONAL_STREAM_CODEC,
            BarrelRecipe::resultFluid,
            ItemStack.OPTIONAL_STREAM_CODEC,
            BarrelRecipe::resultItem,
            ByteBufCodecs.BOOL,
            BarrelRecipe::requiresSeal,
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
