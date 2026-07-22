package com.protyvkultury.revivalages.feature.technology.anvil.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class AnvilRecipeSerializer implements RecipeSerializer<AnvilRecipe> {

    private static final MapCodec<AnvilRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(AnvilRecipe::ingredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(AnvilRecipe::result),
            Codec.INT.validate(value -> value > 0
                            ? com.mojang.serialization.DataResult.success(value)
                            : com.mojang.serialization.DataResult.error(() -> "hits must be positive"))
                    .fieldOf("hits").forGetter(AnvilRecipe::hits),
            StringRepresentableCodec.ANVIL_TOOL.fieldOf("tool").forGetter(AnvilRecipe::tool)
    ).apply(instance, AnvilRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, AnvilRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            AnvilRecipe::ingredient,
            ItemStack.STREAM_CODEC,
            AnvilRecipe::result,
            ByteBufCodecs.VAR_INT,
            AnvilRecipe::hits,
            ByteBufCodecs.idMapper(index -> AnvilTool.values()[index], AnvilTool::ordinal),
            AnvilRecipe::tool,
            AnvilRecipe::new
    );

    @Override
    public MapCodec<AnvilRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AnvilRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static final class StringRepresentableCodec {
        private static final Codec<AnvilTool> ANVIL_TOOL = net.minecraft.util.StringRepresentable.fromEnum(AnvilTool::values);

        private StringRepresentableCodec() {
        }
    }
}
