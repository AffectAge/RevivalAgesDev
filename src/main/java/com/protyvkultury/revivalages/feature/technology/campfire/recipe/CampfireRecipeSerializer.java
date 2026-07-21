package com.protyvkultury.revivalages.feature.technology.campfire.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class CampfireRecipeSerializer implements RecipeSerializer<CampfireRecipe> {

    private static final MapCodec<CampfireRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(CampfireRecipe::ingredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CampfireRecipe::result),
            Codec.INT.validate(value -> value > 0
                            ? com.mojang.serialization.DataResult.success(value)
                            : com.mojang.serialization.DataResult.error(() -> "cooking_time must be positive"))
                    .fieldOf("cooking_time")
                    .forGetter(CampfireRecipe::cookingTime)
    ).apply(instance, CampfireRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, CampfireRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            CampfireRecipe::ingredient,
            ItemStack.STREAM_CODEC,
            CampfireRecipe::result,
            ByteBufCodecs.VAR_INT,
            CampfireRecipe::cookingTime,
            CampfireRecipe::new
    );

    @Override
    public MapCodec<CampfireRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CampfireRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
