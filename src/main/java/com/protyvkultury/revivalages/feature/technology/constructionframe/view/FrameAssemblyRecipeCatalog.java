package com.protyvkultury.revivalages.feature.technology.constructionframe.view;

import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameConfig;
import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameFeature;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeManager;

/** The only RecipeManager-to-viewer query path for frame assembly. */
public final class FrameAssemblyRecipeCatalog {

    private FrameAssemblyRecipeCatalog() {
    }

    public static List<FrameAssemblyRecipeView> recipes(RecipeManager manager) {
        if (!ConstructionFrameConfig.enabled()) {
            return List.of();
        }
        return manager.getAllRecipesFor(ConstructionFrameFeature.RECIPE_TYPE.get()).stream()
                .map(holder -> new FrameAssemblyRecipeView(
                        holder.id(),
                        holder.value().getIngredients(),
                        holder.value().tool(),
                        holder.value().result(),
                        holder
                ))
                .toList();
    }
}
