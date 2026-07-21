package com.protyvkultury.revivalages.feature.technology.pitkiln.recipe;

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

public final class PitKilnRecipeSerializer implements RecipeSerializer<PitKilnRecipe> {

    private static final MapCodec<PitKilnRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(PitKilnRecipe::ingredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(PitKilnRecipe::result),
            Codec.INT.validate(value -> value > 0
                            ? com.mojang.serialization.DataResult.success(value)
                            : com.mojang.serialization.DataResult.error(() -> "burn_time must be positive"))
                    .fieldOf("burn_time").forGetter(PitKilnRecipe::burnTime),
            Codec.FLOAT.validate(value -> value >= 0.0F && value <= 1.0F
                            ? com.mojang.serialization.DataResult.success(value)
                            : com.mojang.serialization.DataResult.error(() -> "failure_chance must be in [0,1]"))
                    .optionalFieldOf("failure_chance", 0.0F).forGetter(PitKilnRecipe::failureChance),
            ItemStack.STRICT_CODEC.listOf().optionalFieldOf("failure_results", List.of())
                    .forGetter(PitKilnRecipe::failureResults)
    ).apply(instance, PitKilnRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, PitKilnRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            PitKilnRecipe::ingredient,
            ItemStack.STREAM_CODEC,
            PitKilnRecipe::result,
            ByteBufCodecs.VAR_INT,
            PitKilnRecipe::burnTime,
            ByteBufCodecs.FLOAT,
            PitKilnRecipe::failureChance,
            ByteBufCodecs.collection(java.util.ArrayList::new, ItemStack.STREAM_CODEC),
            PitKilnRecipe::failureResults,
            PitKilnRecipe::new
    );

    @Override
    public MapCodec<PitKilnRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, PitKilnRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
