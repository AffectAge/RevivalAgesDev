package com.protyvkultury.revivalages.feature.technology.soakingpot.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.protyvkultury.revivalages.core.process.ProcessRule;
import java.util.ArrayList;
import java.util.List;
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
            ProcessRule.CODEC.listOf().fieldOf("process_rules")
                    .forGetter(SoakingPotRecipe::processRules),
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
            ByteBufCodecs.collection(ArrayList::new, ProcessRule.STREAM_CODEC),
            SoakingPotRecipe::processRules,
            ByteBufCodecs.VAR_INT,
            SoakingPotRecipe::processingTime,
            (ingredient, inputFluid, result, processRules, processingTime) -> new SoakingPotRecipe(
                    ingredient, inputFluid, result, processRules, processingTime)
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
