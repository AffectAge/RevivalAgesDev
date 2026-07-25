package com.protyvkultury.revivalages.feature.technology.knapping.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class KnappingRecipeSerializer implements RecipeSerializer<KnappingRecipe> {

    private static final MapCodec<RawRecipe> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("knapping_type").forGetter(RawRecipe::knappingType),
            KnappingPattern.CODEC.fieldOf("pattern").forGetter(RawRecipe::pattern),
            Codec.BOOL.optionalFieldOf("default_on").forGetter(RawRecipe::defaultOn),
            Ingredient.CODEC_NONEMPTY.optionalFieldOf("ingredient").forGetter(RawRecipe::ingredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(RawRecipe::result)
    ).apply(instance, RawRecipe::new));
    private static final MapCodec<KnappingRecipe> CODEC = RAW_CODEC.flatXmap(
            KnappingRecipeSerializer::decodeRecipe,
            recipe -> DataResult.success(new RawRecipe(
                    recipe.knappingType(),
                    recipe.pattern(),
                    recipe.pattern().width() == 5 && recipe.pattern().height() == 5
                            ? Optional.empty()
                            : Optional.of(recipe.pattern().defaultOn()),
                    recipe.ingredient(),
                    recipe.result()
            ))
    );
    private static final StreamCodec<RegistryFriendlyByteBuf, KnappingRecipe> STREAM_CODEC =
            StreamCodec.of(KnappingRecipeSerializer::encode, KnappingRecipeSerializer::decode);

    @Override
    public MapCodec<KnappingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, KnappingRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static void encode(RegistryFriendlyByteBuf buffer, KnappingRecipe recipe) {
        ResourceLocation.STREAM_CODEC.encode(buffer, recipe.knappingType());
        ByteBufCodecs.VAR_INT.encode(buffer, recipe.pattern().width());
        ByteBufCodecs.VAR_INT.encode(buffer, recipe.pattern().height());
        ByteBufCodecs.VAR_INT.encode(buffer, recipe.pattern().cells());
        buffer.writeBoolean(recipe.pattern().defaultOn());
        buffer.writeBoolean(recipe.ingredient().isPresent());
        recipe.ingredient().ifPresent(value -> Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, value));
        ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
    }

    private static KnappingRecipe decode(RegistryFriendlyByteBuf buffer) {
        ResourceLocation type = ResourceLocation.STREAM_CODEC.decode(buffer);
        int width = ByteBufCodecs.VAR_INT.decode(buffer);
        int height = ByteBufCodecs.VAR_INT.decode(buffer);
        int cells = ByteBufCodecs.VAR_INT.decode(buffer);
        boolean defaultOn = buffer.readBoolean();
        Optional<Ingredient> ingredient = buffer.readBoolean()
                ? Optional.of(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer))
                : Optional.empty();
        ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
        return new KnappingRecipe(type, new KnappingPattern(width, height, cells, defaultOn), ingredient, result);
    }

    private static DataResult<KnappingRecipe> decodeRecipe(RawRecipe raw) {
        boolean fullSize = raw.pattern().width() == 5 && raw.pattern().height() == 5;
        if (!fullSize && raw.defaultOn().isEmpty()) {
            return DataResult.error(() -> "default_on is required for a knapping pattern smaller than 5x5");
        }
        return DataResult.success(new KnappingRecipe(
                raw.knappingType(),
                raw.pattern().withDefaultOn(raw.defaultOn().orElse(false)),
                raw.ingredient(),
                raw.result()
        ));
    }

    private record RawRecipe(
            ResourceLocation knappingType,
            KnappingPattern pattern,
            Optional<Boolean> defaultOn,
            Optional<Ingredient> ingredient,
            ItemStack result
    ) {
    }
}
