package com.protyvkultury.revivalages.integration.kubejs;

import com.protyvkultury.revivalages.RevivalAges;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.CharacterComponent;
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
import dev.latvian.mods.kubejs.util.IntBounds;
import dev.latvian.mods.kubejs.util.TinyMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

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

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(RevivalAges.id("frame_assembly"), SCHEMA);
    }
}
