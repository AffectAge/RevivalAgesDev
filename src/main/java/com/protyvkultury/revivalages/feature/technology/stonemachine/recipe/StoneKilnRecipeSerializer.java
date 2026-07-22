package com.protyvkultury.revivalages.feature.technology.stonemachine.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class StoneKilnRecipeSerializer implements RecipeSerializer<StoneKilnRecipe> {

    private static final Codec<Integer> POSITIVE_INT = Codec.INT.validate(value -> value > 0
            ? com.mojang.serialization.DataResult.success(value)
            : com.mojang.serialization.DataResult.error(() -> "processing_time must be positive"));
    private static final Codec<Float> CHANCE = Codec.FLOAT.validate(value -> value >= 0.0F && value <= 1.0F
            ? com.mojang.serialization.DataResult.success(value)
            : com.mojang.serialization.DataResult.error(() -> "failure_chance must be between zero and one"));
    private static final MapCodec<StoneKilnRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(StoneKilnRecipe::ingredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(StoneKilnRecipe::result),
            POSITIVE_INT.fieldOf("processing_time").forGetter(StoneKilnRecipe::processingTime),
            CHANCE.optionalFieldOf("failure_chance", 0.0F).forGetter(StoneKilnRecipe::failureChance),
            ItemStack.STRICT_CODEC.listOf().optionalFieldOf("failure_results", List.of())
                    .forGetter(StoneKilnRecipe::failureResults)
    ).apply(instance, StoneKilnRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, StoneKilnRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            StoneKilnRecipe::ingredient,
            ItemStack.STREAM_CODEC,
            StoneKilnRecipe::result,
            ByteBufCodecs.VAR_INT,
            StoneKilnRecipe::processingTime,
            ByteBufCodecs.FLOAT,
            StoneKilnRecipe::failureChance,
            ItemStack.LIST_STREAM_CODEC,
            StoneKilnRecipe::failureResults,
            StoneKilnRecipe::new
    );

    @Override
    public MapCodec<StoneKilnRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, StoneKilnRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
