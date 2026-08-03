package com.protyvkultury.revivalages.feature.technology.constructionframe.view;

import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameConfig;
import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameFeature;
import com.protyvkultury.revivalages.core.process.ToolRequirementView;
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
                .filter(holder -> ContentAvailability.isResultEnabled(holder.value().result()))
                .map(holder -> new FrameAssemblyRecipeView(
                        holder.id(),
                        holder.value().getIngredients(),
                        new ToolRequirementView(holder.value().tool(), List.of(
                                net.minecraft.network.chat.Component.translatable("gui.revivalages.tool_requirement.frame"),
                                net.minecraft.network.chat.Component.translatable(
                                        "gui.revivalages.tool_requirement.frame_durability",
                                        ConstructionFrameConfig.toolDurabilityCost()))),
                        holder.value().result(),
                        holder
                ))
                .toList();
    }
}
