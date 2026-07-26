package com.protyvkultury.revivalages.integration.kubejs;

import com.protyvkultury.revivalages.RevivalAges;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.CharacterComponent;
import dev.latvian.mods.kubejs.recipe.component.BlockStateComponent;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.ComponentValueMap;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.MapRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.util.IntBounds;
import dev.latvian.mods.kubejs.util.TinyMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;

/** Optional typed recipe schema that still delegates final validity to the gameplay codec. */
public final class ConstructionFrameKubeJsPlugin implements KubeJSPlugin {

    private static final RecipeKey<ItemStack> RESULT =
            ItemStackComponent.ITEM_STACK.key("result", ComponentRole.OUTPUT);
    private static final RecipeKey<Ingredient> TOOL =
            IngredientComponent.INGREDIENT.key("tool", ComponentRole.INPUT);
    private static final RecipeKey<List<String>> BOTTOM =
            StringComponent.STRING.instance().asList().key("bottom", ComponentRole.INPUT);
    private static final RecipeKey<List<String>> MIDDLE =
            StringComponent.STRING.instance().asList().key("middle", ComponentRole.INPUT);
    private static final RecipeKey<List<String>> TOP =
            StringComponent.STRING.instance().asList().key("top", ComponentRole.INPUT);
    private static final RecipeKey<TinyMap<String, List<String>>> PATTERN =
            new MapRecipeComponent<>(
                    StringComponent.STRING.instance(),
                    StringComponent.STRING.instance().asList(),
                    IntBounds.DEFAULT,
                    true
            ).key("pattern", ComponentRole.OTHER);
    private static final RecipeKey<TinyMap<Character, Ingredient>> KEY =
            new MapRecipeComponent<>(
                    CharacterComponent.CHARACTER.instance(),
                    IngredientComponent.INGREDIENT.instance(),
                    IntBounds.DEFAULT,
                    true
            ).key("key", ComponentRole.INPUT);

    private static final RecipeSchema SCHEMA = new RecipeSchema(RESULT, TOOL, PATTERN, KEY)
            .constructor(RESULT, TOOL, PATTERN, KEY)
            .constructor(new RecipeConstructor(RESULT, TOOL, BOTTOM, MIDDLE, TOP, KEY) {
                @Override
                public void setValues(
                        RecipeScriptContext context,
                        RecipeSchemaType schemaType,
                        ComponentValueMap values
                ) {
                    context.recipe().setValue(RESULT, values.getValue(context, RESULT));
                    context.recipe().setValue(TOOL, values.getValue(context, TOOL));
                    context.recipe().setValue(KEY, values.getValue(context, KEY));
                    context.recipe().setValue(PATTERN, TinyMap.ofMap(Map.of(
                            "bottom", values.getValue(context, BOTTOM),
                            "middle", values.getValue(context, MIDDLE),
                            "top", values.getValue(context, TOP)
                    )));
                }
            });
    private static final RecipeKey<String> KNAPPING_TYPE =
            StringComponent.ID.key("knapping_type", ComponentRole.OTHER);
    private static final RecipeKey<List<String>> KNAPPING_PATTERN =
            StringComponent.STRING.instance().asList().key("pattern", ComponentRole.OTHER);
    private static final RecipeKey<Boolean> DEFAULT_ON =
            BooleanComponent.BOOLEAN.key("default_on", ComponentRole.OTHER);
    private static final RecipeKey<Ingredient> REFINEMENT =
            IngredientComponent.OPTIONAL_INGREDIENT.key("ingredient", ComponentRole.INPUT);
    private static final RecipeSchema KNAPPING_SCHEMA =
            new RecipeSchema(KNAPPING_TYPE, KNAPPING_PATTERN, DEFAULT_ON, REFINEMENT, RESULT)
                    .constructor(RESULT, KNAPPING_TYPE, KNAPPING_PATTERN, DEFAULT_ON)
                    .constructor(RESULT, KNAPPING_TYPE, KNAPPING_PATTERN, DEFAULT_ON, REFINEMENT);
    private static final RecipeKey<List<String>> BLOCK_INGREDIENT =
            StringComponent.STRING.instance().asList().key("ingredient", ComponentRole.INPUT);
    private static final RecipeKey<BlockState> BLOCK_RESULT =
            BlockStateComponent.BLOCK.key("result", ComponentRole.OUTPUT);
    private static final RecipeSchema BLOCK_TRANSFORMATION_SCHEMA =
            new RecipeSchema(BLOCK_INGREDIENT, BLOCK_RESULT)
                    .constructor(BLOCK_RESULT, BLOCK_INGREDIENT);

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(RevivalAges.id("frame_assembly"), SCHEMA);
        registry.register(RevivalAges.id("knapping"), KNAPPING_SCHEMA);
        registry.register(RevivalAges.id("collapse"), BLOCK_TRANSFORMATION_SCHEMA);
        registry.register(RevivalAges.id("landslide"), BLOCK_TRANSFORMATION_SCHEMA);
    }

    @Override
    public void registerBindings(BindingRegistry registry) {
        registry.add("RevivalAgesItemSize", ItemSizeScriptBindings.INSTANCE);
        registry.add("RevivalAgesCarriedWeight", CarriedWeightScriptBindings.INSTANCE);
    }
}
