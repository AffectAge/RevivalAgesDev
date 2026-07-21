package com.protyvkultury.revivalages.feature.technology.tanningrack.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class TanningRackRecipeSerializer implements RecipeSerializer<TanningRackRecipe> {

    private static final MapCodec<TanningRackRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(TanningRackRecipe::ingredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(TanningRackRecipe::result),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("rain_failure", ItemStack.EMPTY).forGetter(TanningRackRecipe::rainFailure),
            Codec.INT.validate(value -> value > 0
                            ? com.mojang.serialization.DataResult.success(value)
                            : com.mojang.serialization.DataResult.error(() -> "processing_time must be positive"))
                    .fieldOf("processing_time").forGetter(TanningRackRecipe::processingTime)
    ).apply(instance, TanningRackRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, TanningRackRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            TanningRackRecipe::ingredient,
            ItemStack.STREAM_CODEC,
            TanningRackRecipe::result,
            ItemStack.OPTIONAL_STREAM_CODEC,
            TanningRackRecipe::rainFailure,
            ByteBufCodecs.VAR_INT,
            TanningRackRecipe::processingTime,
            TanningRackRecipe::new
    );

    @Override
    public MapCodec<TanningRackRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, TanningRackRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
