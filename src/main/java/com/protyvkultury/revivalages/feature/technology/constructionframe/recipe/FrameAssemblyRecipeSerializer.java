package com.protyvkultury.revivalages.feature.technology.constructionframe.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Strict codec and network serializer for frame assembly recipes. */
public final class FrameAssemblyRecipeSerializer implements RecipeSerializer<FrameAssemblyRecipe> {

    private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap(value -> {
        if (value.length() != 1 || value.charAt(0) == ' ') {
            return DataResult.error(() -> "Recipe symbols must be one non-space character");
        }
        return DataResult.success(value.charAt(0));
    }, String::valueOf);

    private static final Codec<List<String>> LAYER_CODEC = Codec.STRING.listOf().comapFlatMap(rows -> {
        if (rows.size() != 3 || rows.stream().anyMatch(row -> row.length() != 3)) {
            return DataResult.error(() -> "Every frame assembly layer must contain exactly three 3-character rows");
        }
        return DataResult.success(List.copyOf(rows));
    }, rows -> rows);

    private static final MapCodec<PatternData> PATTERN_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LAYER_CODEC.fieldOf("bottom").forGetter(PatternData::bottom),
            LAYER_CODEC.fieldOf("middle").forGetter(PatternData::middle),
            LAYER_CODEC.fieldOf("top").forGetter(PatternData::top)
    ).apply(instance, PatternData::new));

    private static final MapCodec<RawRecipe> RAW_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.strictUnboundedMap(SYMBOL_CODEC, Ingredient.CODEC_NONEMPTY)
                    .fieldOf("key").forGetter(RawRecipe::key),
            PATTERN_CODEC.fieldOf("pattern").forGetter(RawRecipe::pattern),
            Ingredient.CODEC_NONEMPTY.fieldOf("tool").forGetter(RawRecipe::tool),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(RawRecipe::result),
            FrameGridPosition.CODEC.optionalFieldOf("wood_variant_source")
                    .forGetter(RawRecipe::woodVariantSource)
    ).apply(instance, RawRecipe::new));

    private static final MapCodec<FrameAssemblyRecipe> CODEC = RAW_CODEC.flatXmap(
            FrameAssemblyRecipeSerializer::decode,
            recipe -> DataResult.success(new RawRecipe(
                    keyForEncoding(recipe),
                    patternForEncoding(recipe),
                    recipe.tool(),
                    recipe.result(),
                    recipe.woodVariantSource()
            ))
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, FrameAssemblyRecipe> STREAM_CODEC =
            StreamCodec.of(FrameAssemblyRecipeSerializer::encode, FrameAssemblyRecipeSerializer::decode);

    @Override
    public MapCodec<FrameAssemblyRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FrameAssemblyRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static DataResult<FrameAssemblyRecipe> decode(RawRecipe raw) {
        Set<Character> unused = new HashSet<>(raw.key.keySet());
        List<Ingredient> ingredients = new ArrayList<>(27);
        for (List<String> layer : List.of(raw.pattern.bottom, raw.pattern.middle, raw.pattern.top)) {
            for (String row : layer) {
                for (int index = 0; index < 3; index++) {
                    char symbol = row.charAt(index);
                    if (symbol == ' ') {
                        ingredients.add(Ingredient.EMPTY);
                    } else {
                        Ingredient ingredient = raw.key.get(symbol);
                        if (ingredient == null) {
                            return DataResult.error(() ->
                                    "Frame assembly pattern references undefined symbol '" + symbol + "'");
                        }
                        unused.remove(symbol);
                        ingredients.add(ingredient);
                    }
                }
            }
        }
        if (!unused.isEmpty()) {
            return DataResult.error(() -> "Frame assembly key contains unused symbols: " + unused);
        }
        ItemStack result = raw.result;
        if (result.getCount() != 1 || !(result.getItem() instanceof BlockItem)) {
            return DataResult.error(() -> "Frame assembly result must be exactly one BlockItem");
        }
        return DataResult.success(new FrameAssemblyRecipe(
                ingredients,
                raw.tool,
                result,
                raw.woodVariantSource
        ));
    }

    private static void encode(RegistryFriendlyByteBuf buffer, FrameAssemblyRecipe recipe) {
        for (Ingredient ingredient : recipe.getIngredients()) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
        }
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.tool());
        ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
        buffer.writeBoolean(recipe.woodVariantSource().isPresent());
        recipe.woodVariantSource().ifPresent(position -> FrameGridPosition.STREAM_CODEC.encode(buffer, position));
    }

    private static FrameAssemblyRecipe decode(RegistryFriendlyByteBuf buffer) {
        NonNullList<Ingredient> ingredients = NonNullList.withSize(27, Ingredient.EMPTY);
        ingredients.replaceAll(ignored -> Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
        Ingredient tool = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
        Optional<FrameGridPosition> source = buffer.readBoolean()
                ? Optional.of(FrameGridPosition.STREAM_CODEC.decode(buffer))
                : Optional.empty();
        return new FrameAssemblyRecipe(ingredients, tool, result, source);
    }

    /*
     * Network-decoded recipes do not retain symbolic source data. The codec calls these only while encoding
     * data-generated instances, which this feature does not do; explicit failure prevents a lossy JSON write.
     */
    private static Map<Character, Ingredient> keyForEncoding(FrameAssemblyRecipe recipe) {
        Map<Character, Ingredient> key = new LinkedHashMap<>();
        char symbol = 'A';
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (!ingredient.isEmpty() && !key.containsValue(ingredient)) {
                key.put(symbol++, ingredient);
            }
        }
        return key;
    }

    private static PatternData patternForEncoding(FrameAssemblyRecipe recipe) {
        Map<Ingredient, Character> symbols = new LinkedHashMap<>();
        char next = 'A';
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (!ingredient.isEmpty() && !symbols.containsKey(ingredient)) {
                symbols.put(ingredient, next++);
            }
        }
        List<String> rows = new ArrayList<>(9);
        for (int row = 0; row < 9; row++) {
            StringBuilder value = new StringBuilder(3);
            for (int column = 0; column < 3; column++) {
                Ingredient ingredient = recipe.getIngredients().get(row * 3 + column);
                value.append(ingredient.isEmpty() ? ' ' : symbols.get(ingredient));
            }
            rows.add(value.toString());
        }
        return new PatternData(rows.subList(0, 3), rows.subList(3, 6), rows.subList(6, 9));
    }

    private record PatternData(List<String> bottom, List<String> middle, List<String> top) {
    }

    private record RawRecipe(
            Map<Character, Ingredient> key,
            PatternData pattern,
            Ingredient tool,
            ItemStack result,
            Optional<FrameGridPosition> woodVariantSource
    ) {
    }
}
