package com.protyvkultury.revivalages.feature.technology.barrel.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

/** One barrel input slot and the number of items consumed from it. */
public record CountedBarrelIngredient(Ingredient ingredient, int count) {

    private static final Codec<CountedBarrelIngredient> COUNTED_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(CountedBarrelIngredient::ingredient),
                    Codec.intRange(1, 64).fieldOf("count").forGetter(CountedBarrelIngredient::count)
            ).apply(instance, CountedBarrelIngredient::new));
    public static final Codec<CountedBarrelIngredient> CODEC =
            Codec.either(Ingredient.CODEC_NONEMPTY, COUNTED_CODEC).xmap(
                    value -> value.map(ingredient -> new CountedBarrelIngredient(ingredient, 1), counted -> counted),
                    counted -> counted.count == 1 ? Either.left(counted.ingredient) : Either.right(counted)
            );
    public static final StreamCodec<RegistryFriendlyByteBuf, CountedBarrelIngredient> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, CountedBarrelIngredient::ingredient,
                    ByteBufCodecs.VAR_INT, CountedBarrelIngredient::count,
                    CountedBarrelIngredient::new
            );

    public CountedBarrelIngredient {
        if (ingredient == null || ingredient.isEmpty() || count < 1 || count > 64) {
            throw new IllegalArgumentException("Barrel ingredient must be nonempty and require 1-64 items");
        }
    }
}
