package com.protyvkultury.revivalages.feature.technology.pitburn.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class PitBurnRecipeSerializer implements RecipeSerializer<PitBurnRecipe> {

    private static final MapCodec<PitBurnRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(PitBurnRecipe::ingredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(PitBurnRecipe::result),
            positive("stages").optionalFieldOf("stages", 10).forGetter(PitBurnRecipe::stages),
            positive("burn_time").fieldOf("burn_time").forGetter(PitBurnRecipe::burnTime),
            Codec.FLOAT.validate(value -> value >= 0.0F && value <= 1.0F
                            ? DataResult.success(value)
                            : DataResult.error(() -> "failure_chance must be in [0,1]"))
                    .optionalFieldOf("failure_chance", 0.0F).forGetter(PitBurnRecipe::failureChance),
            ItemStack.STRICT_CODEC.listOf().optionalFieldOf("failure_results", List.of())
                    .forGetter(PitBurnRecipe::failureResults)
    ).apply(instance, PitBurnRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, PitBurnRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, PitBurnRecipe::ingredient,
            ItemStack.STREAM_CODEC, PitBurnRecipe::result,
            ByteBufCodecs.VAR_INT, PitBurnRecipe::stages,
            ByteBufCodecs.VAR_INT, PitBurnRecipe::burnTime,
            ByteBufCodecs.FLOAT, PitBurnRecipe::failureChance,
            ByteBufCodecs.collection(ArrayList::new, ItemStack.STREAM_CODEC), PitBurnRecipe::failureResults,
            PitBurnRecipe::new
    );

    private static Codec<Integer> positive(String name) {
        return Codec.INT.validate(value -> value > 0
                ? DataResult.success(value)
                : DataResult.error(() -> name + " must be positive"));
    }

    @Override
    public MapCodec<PitBurnRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, PitBurnRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
