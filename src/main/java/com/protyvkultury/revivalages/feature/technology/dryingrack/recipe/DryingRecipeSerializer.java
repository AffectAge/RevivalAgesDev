package com.protyvkultury.revivalages.feature.technology.dryingrack.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public final class DryingRecipeSerializer implements RecipeSerializer<DryingRecipe> {

    private final MapCodec<DryingRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, DryingRecipe> streamCodec;

    public DryingRecipeSerializer(Supplier<RecipeType<DryingRecipe>> type) {
        codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(DryingRecipe::ingredient),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.getResultItem(null)),
                Codec.INT.validate(value -> value > 0
                                ? com.mojang.serialization.DataResult.success(value)
                                : com.mojang.serialization.DataResult.error(() -> "drying_time must be positive"))
                        .fieldOf("drying_time")
                        .forGetter(DryingRecipe::dryingTime)
        ).apply(instance, (ingredient, result, dryingTime) ->
                new DryingRecipe(ingredient, result, dryingTime, type.get())));
        streamCodec = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                DryingRecipe::ingredient,
                ItemStack.STREAM_CODEC,
                recipe -> recipe.getResultItem(null),
                ByteBufCodecs.VAR_INT,
                DryingRecipe::dryingTime,
                (ingredient, result, dryingTime) -> new DryingRecipe(ingredient, result, dryingTime, type.get())
        );
    }

    @Override
    public MapCodec<DryingRecipe> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, DryingRecipe> streamCodec() {
        return streamCodec;
    }
}
