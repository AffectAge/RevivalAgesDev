package com.protyvkultury.revivalages.feature.technology.animalpower.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public final class PressingRecipeSerializer implements RecipeSerializer<PressingRecipe> {

    private static final Codec<Integer> POSITIVE_INT = Codec.INT.validate(value -> value > 0
            ? DataResult.success(value)
            : DataResult.error(() -> "input_count must be positive"));
    private static final MapCodec<Payload> PAYLOAD_CODEC = RecordCodecBuilder.<Payload>mapCodec(
            instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(Payload::ingredient),
            POSITIVE_INT.optionalFieldOf("input_count", 1).forGetter(Payload::inputCount),
            ItemStack.STRICT_CODEC.optionalFieldOf("result")
                    .forGetter(Payload::itemResult),
            FluidStack.CODEC.optionalFieldOf("fluid_result")
                    .forGetter(Payload::fluidResult)
            ).apply(instance, Payload::new)
    );
    private static final MapCodec<PressingRecipe> CODEC = PAYLOAD_CODEC.flatXmap(
            PressingRecipeSerializer::create,
            recipe -> DataResult.success(new Payload(
                    recipe.ingredient(),
                    recipe.inputCount(),
                    optional(recipe.itemResult()),
                    optional(recipe.fluidResult())
            ))
    );
    private static final StreamCodec<RegistryFriendlyByteBuf, PressingRecipe> STREAM_CODEC =
            StreamCodec.ofMember(PressingRecipeSerializer::encode, PressingRecipeSerializer::decode);

    @Override
    public MapCodec<PressingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, PressingRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static DataResult<PressingRecipe> create(Payload payload) {
        if (payload.itemResult().isPresent() == payload.fluidResult().isPresent()) {
            return DataResult.error(() -> "Pressing recipes require exactly one result field");
        }
        return DataResult.success(new PressingRecipe(
                payload.ingredient(),
                payload.inputCount(),
                payload.itemResult().orElse(ItemStack.EMPTY),
                payload.fluidResult().orElse(FluidStack.EMPTY)
        ));
    }

    private static <T> Optional<T> optional(T value) {
        if (value instanceof ItemStack stack && stack.isEmpty()) {
            return Optional.empty();
        }
        if (value instanceof FluidStack stack && stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    private static void encode(PressingRecipe recipe, RegistryFriendlyByteBuf buffer) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient());
        ByteBufCodecs.VAR_INT.encode(buffer, recipe.inputCount());
        boolean item = !recipe.itemResult().isEmpty();
        buffer.writeBoolean(item);
        if (item) {
            ItemStack.STREAM_CODEC.encode(buffer, recipe.itemResult());
        } else {
            FluidStack.STREAM_CODEC.encode(buffer, recipe.fluidResult());
        }
    }

    private static PressingRecipe decode(RegistryFriendlyByteBuf buffer) {
        Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        int inputCount = ByteBufCodecs.VAR_INT.decode(buffer);
        if (buffer.readBoolean()) {
            return new PressingRecipe(
                    ingredient, inputCount, ItemStack.STREAM_CODEC.decode(buffer), FluidStack.EMPTY);
        }
        return new PressingRecipe(
                ingredient, inputCount, ItemStack.EMPTY, FluidStack.STREAM_CODEC.decode(buffer));
    }

    private record Payload(
            Ingredient ingredient,
            int inputCount,
            Optional<ItemStack> itemResult,
            Optional<FluidStack> fluidResult
    ) {
    }
}
