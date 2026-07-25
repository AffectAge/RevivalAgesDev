package com.protyvkultury.revivalages.feature.world.structuralintegrity.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public final class SupportBeamRecipeSerializer implements RecipeSerializer<SupportBeamRecipe> {

    private static final MapCodec<SupportBeamRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(SupportBeamRecipe::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                    .forGetter(SupportBeamRecipe::category),
            ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(SupportBeamRecipe::result),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(SupportBeamRecipe::showNotification)
    ).apply(instance, SupportBeamRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, SupportBeamRecipe> STREAM_CODEC =
            StreamCodec.of(SupportBeamRecipeSerializer::encode, SupportBeamRecipeSerializer::decode);

    @Override
    public MapCodec<SupportBeamRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, SupportBeamRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static void encode(RegistryFriendlyByteBuf buffer, SupportBeamRecipe recipe) {
        buffer.writeUtf(recipe.getGroup());
        buffer.writeEnum(recipe.category());
        ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
        ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
        buffer.writeBoolean(recipe.showNotification());
    }

    private static SupportBeamRecipe decode(RegistryFriendlyByteBuf buffer) {
        return new SupportBeamRecipe(
                buffer.readUtf(),
                buffer.readEnum(CraftingBookCategory.class),
                ShapedRecipePattern.STREAM_CODEC.decode(buffer),
                ItemStack.STREAM_CODEC.decode(buffer),
                buffer.readBoolean()
        );
    }
}
