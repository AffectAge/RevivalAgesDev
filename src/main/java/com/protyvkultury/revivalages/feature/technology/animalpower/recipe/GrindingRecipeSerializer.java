package com.protyvkultury.revivalages.feature.technology.animalpower.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class GrindingRecipeSerializer implements RecipeSerializer<GrindingRecipe> {

    private static final Codec<Integer> POSITIVE_INT = Codec.INT.validate(value -> value > 0
            ? DataResult.success(value)
            : DataResult.error(() -> "value must be positive"));
    private static final Codec<Double> CHANCE = Codec.DOUBLE.validate(value -> value >= 0.0D && value <= 1.0D
            ? DataResult.success(value)
            : DataResult.error(() -> "secondary_chance must be between zero and one"));
    private static final Codec<List<GrindingMachine>> MACHINES = GrindingMachine.CODEC.listOf().validate(values ->
            !values.isEmpty() && values.stream().distinct().count() == values.size()
                    ? DataResult.success(values)
                    : DataResult.error(() -> "machines must contain unique values and cannot be empty"));
    private static final MapCodec<Payload> PAYLOAD_CODEC = RecordCodecBuilder.<Payload>mapCodec(
            instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(Payload::ingredient),
            POSITIVE_INT.optionalFieldOf("input_count", 1).forGetter(Payload::inputCount),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(Payload::result),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("secondary_result", ItemStack.EMPTY)
                    .forGetter(Payload::secondaryResult),
            CHANCE.optionalFieldOf("secondary_chance", 0.0D)
                    .forGetter(Payload::secondaryChance),
            POSITIVE_INT.fieldOf("work_points").forGetter(Payload::workPoints),
            MACHINES.optionalFieldOf("machines", List.of(GrindingMachine.HAND, GrindingMachine.ANIMAL))
                    .forGetter(Payload::machines)
            ).apply(instance, Payload::new)
    );
    private static final MapCodec<GrindingRecipe> CODEC = PAYLOAD_CODEC.flatXmap(
            GrindingRecipeSerializer::create,
            recipe -> DataResult.success(new Payload(
                    recipe.ingredient(),
                    recipe.inputCount(),
                    recipe.result(),
                    recipe.secondaryResult(),
                    recipe.secondaryChance(),
                    recipe.workPoints(),
                    recipe.machines()
            ))
    );
    private static final StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> STREAM_CODEC =
            StreamCodec.ofMember(GrindingRecipeSerializer::encode, GrindingRecipeSerializer::decode);

    @Override
    public MapCodec<GrindingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static DataResult<GrindingRecipe> create(Payload payload) {
        if (payload.result().isEmpty()) {
            return DataResult.error(() -> "result cannot be empty");
        }
        if (payload.secondaryResult().isEmpty() && payload.secondaryChance() != 0.0D) {
            return DataResult.error(() -> "secondary_chance requires secondary_result");
        }
        return DataResult.success(new GrindingRecipe(
                payload.ingredient(),
                payload.inputCount(),
                payload.result(),
                payload.secondaryResult(),
                payload.secondaryChance(),
                payload.workPoints(),
                payload.machines()
        ));
    }

    private static void encode(GrindingRecipe recipe, RegistryFriendlyByteBuf buffer) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient());
        ByteBufCodecs.VAR_INT.encode(buffer, recipe.inputCount());
        ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.secondaryResult());
        buffer.writeDouble(recipe.secondaryChance());
        ByteBufCodecs.VAR_INT.encode(buffer, recipe.workPoints());
        ByteBufCodecs.VAR_INT.encode(buffer, recipe.machines().size());
        recipe.machines().forEach(machine -> ByteBufCodecs.STRING_UTF8.encode(buffer, machine.getSerializedName()));
    }

    private static GrindingRecipe decode(RegistryFriendlyByteBuf buffer) {
        Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        int inputCount = ByteBufCodecs.VAR_INT.decode(buffer);
        ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
        ItemStack secondary = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        double chance = buffer.readDouble();
        int points = ByteBufCodecs.VAR_INT.decode(buffer);
        int size = ByteBufCodecs.VAR_INT.decode(buffer);
        java.util.ArrayList<GrindingMachine> machines = new java.util.ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            String name = ByteBufCodecs.STRING_UTF8.decode(buffer);
            machines.add(java.util.Arrays.stream(GrindingMachine.values())
                    .filter(machine -> machine.getSerializedName().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown grinding machine: " + name)));
        }
        return new GrindingRecipe(ingredient, inputCount, result, secondary, chance, points, machines);
    }

    private record Payload(
            Ingredient ingredient,
            int inputCount,
            ItemStack result,
            ItemStack secondaryResult,
            double secondaryChance,
            int workPoints,
            List<GrindingMachine> machines
    ) {
    }
}
