package com.protyvkultury.revivalages.integration.emi;

import com.protyvkultury.revivalages.feature.technology.constructionframe.view.FrameAssemblyRecipeView;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;

final class FrameAssemblyEmiRecipe extends BasicEmiRecipe {

    FrameAssemblyEmiRecipe(EmiRecipeCategory category, FrameAssemblyRecipeView view) {
        super(category, view.id(), 118, 98);
        inputs = view.ingredients().stream().map(EmiIngredient::of).toList();
        catalysts = List.of(EmiIngredient.of(view.tool()));
        outputs = List.of(EmiStack.of(view.result()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.add(new FrameAssemblyEmiWidget(0, 0, 94, 97, inputs));
        widgets.addTexture(dev.emi.emi.api.render.EmiTexture.EMPTY_ARROW, 59, 41);
        widgets.addSlot(catalysts.getFirst(), 61, 60).catalyst(true);
        widgets.addSlot(outputs.getFirst(), 91, 36).large(true).recipeContext(this);
    }
}
