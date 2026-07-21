package com.protyvkultury.revivalages.feature.technology.choppingblock.recipe;

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

public final class ChoppingRecipeSerializer implements RecipeSerializer<ChoppingRecipe> {

    private static final Codec<List<Integer>> POSITIVE_LIST = Codec.INT.listOf().validate(values ->
            values.stream().allMatch(value -> value > 0)
                    ? com.mojang.serialization.DataResult.success(values)
                    : com.mojang.serialization.DataResult.error(() -> "values must be positive"));
    private static final Codec<List<Integer>> NON_NEGATIVE_LIST = Codec.INT.listOf().validate(values ->
            values.stream().allMatch(value -> value >= 0)
                    ? com.mojang.serialization.DataResult.success(values)
                    : com.mojang.serialization.DataResult.error(() -> "values must be non-negative"));

    private static final MapCodec<ChoppingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ChoppingRecipe::ingredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ChoppingRecipe::result),
            POSITIVE_LIST.optionalFieldOf("chops", List.of()).forGetter(ChoppingRecipe::chops),
            NON_NEGATIVE_LIST.optionalFieldOf("quantities", List.of()).forGetter(ChoppingRecipe::quantities)
    ).apply(instance, ChoppingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, ChoppingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            ChoppingRecipe::ingredient,
            ItemStack.STREAM_CODEC,
            ChoppingRecipe::result,
            ByteBufCodecs.collection(java.util.ArrayList::new, ByteBufCodecs.VAR_INT),
            ChoppingRecipe::chops,
            ByteBufCodecs.collection(java.util.ArrayList::new, ByteBufCodecs.VAR_INT),
            ChoppingRecipe::quantities,
            ChoppingRecipe::new
    );

    @Override
    public MapCodec<ChoppingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ChoppingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
