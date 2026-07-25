package com.protyvkultury.revivalages.feature.world.structuralintegrity.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockTransformationRecipeSerializer implements RecipeSerializer<BlockTransformationRecipe> {

    private final BlockTransformationRecipe.Kind kind;
    private final MapCodec<BlockTransformationRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, BlockTransformationRecipe> streamCodec;

    public BlockTransformationRecipeSerializer(BlockTransformationRecipe.Kind kind) {
        this.kind = kind;
        this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockIngredient.CODEC.fieldOf("ingredient").forGetter(BlockTransformationRecipe::ingredient),
                BlockState.CODEC.fieldOf("result").forGetter(BlockTransformationRecipe::result)
        ).apply(instance, (ingredient, result) -> new BlockTransformationRecipe(kind, ingredient, result)));
        this.streamCodec = StreamCodec.ofMember(this::encode, this::decode);
    }

    @Override
    public MapCodec<BlockTransformationRecipe> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BlockTransformationRecipe> streamCodec() {
        return streamCodec;
    }

    private void encode(BlockTransformationRecipe recipe, RegistryFriendlyByteBuf buffer) {
        ByteBufCodecs.VAR_INT.encode(buffer, recipe.ingredient().entries().size());
        recipe.ingredient().entries().forEach(entry -> ByteBufCodecs.STRING_UTF8.encode(buffer, entry));
        ByteBufCodecs.fromCodecWithRegistries(BlockState.CODEC).encode(buffer, recipe.result());
    }

    private BlockTransformationRecipe decode(RegistryFriendlyByteBuf buffer) {
        int size = ByteBufCodecs.VAR_INT.decode(buffer);
        if (size <= 0 || size > 4_096) {
            throw new IllegalArgumentException("Invalid block ingredient entry count: " + size);
        }
        java.util.ArrayList<String> entries = new java.util.ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            entries.add(ByteBufCodecs.STRING_UTF8.decode(buffer));
        }
        BlockState result = ByteBufCodecs.fromCodecWithRegistries(BlockState.CODEC).decode(buffer);
        return new BlockTransformationRecipe(kind, new BlockIngredient(entries), result);
    }
}
