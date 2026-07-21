package com.protyvkultury.revivalages.feature.technology.soakingpot.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public final class SoakingPotRecipeSerializer implements RecipeSerializer<SoakingPotRecipe> {

    private static final MapCodec<SoakingPotRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(SoakingPotRecipe::ingredient),
            FluidStack.CODEC.fieldOf("input_fluid").forGetter(SoakingPotRecipe::inputFluid),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(SoakingPotRecipe::result),
            Codec.BOOL.optionalFieldOf("requires_campfire", false).forGetter(SoakingPotRecipe::requiresCampfire),
            Codec.INT.validate(value -> value > 0
                            ? com.mojang.serialization.DataResult.success(value)
                            : com.mojang.serialization.DataResult.error(() -> "processing_time must be positive"))
                    .fieldOf("processing_time").forGetter(SoakingPotRecipe::processingTime)
    ).apply(instance, SoakingPotRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, SoakingPotRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            SoakingPotRecipe::ingredient,
            FluidStack.STREAM_CODEC,
            SoakingPotRecipe::inputFluid,
            ItemStack.STREAM_CODEC,
            SoakingPotRecipe::result,
            ByteBufCodecs.BOOL,
            SoakingPotRecipe::requiresCampfire,
            ByteBufCodecs.VAR_INT,
            SoakingPotRecipe::processingTime,
            SoakingPotRecipe::new
    );

    @Override
    public MapCodec<SoakingPotRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, SoakingPotRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
