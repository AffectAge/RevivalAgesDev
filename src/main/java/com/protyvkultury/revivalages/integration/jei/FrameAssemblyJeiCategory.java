package com.protyvkultury.revivalages.integration.jei;

import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameFeature;
import com.protyvkultury.revivalages.feature.technology.constructionframe.view.FrameAssemblyRecipeView;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

final class FrameAssemblyJeiCategory extends AbstractRecipeCategory<FrameAssemblyRecipeView> {

    private final IJeiHelpers helpers;

    FrameAssemblyJeiCategory(IJeiHelpers helpers, RecipeType<FrameAssemblyRecipeView> type) {
        super(
                type,
                Component.translatable("jei.revivalages.category.frame_assembly"),
                helpers.getGuiHelper().createDrawableItemLike(ConstructionFrameFeature.CONSTRUCTION_FRAME_ITEM.get()),
                116,
                97
        );
        this.helpers = helpers;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            FrameAssemblyRecipeView recipe,
            IFocusGroup focuses
    ) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 62, 61)
                .setStandardSlotBackground()
                .addIngredients(recipe.toolRequirement().ingredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 40)
                .setOutputSlotBackground()
                .addItemStack(recipe.result());
        recipe.ingredients().forEach(ingredient ->
                builder.addSlot(RecipeIngredientRole.INPUT).addIngredients(ingredient));
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            FrameAssemblyRecipeView recipe,
            IFocusGroup focuses
    ) {
        FrameAssemblyJeiWidget widget = new FrameAssemblyJeiWidget(
                0,
                0,
                builder.getRecipeSlots().getSlots(RecipeIngredientRole.INPUT),
                helpers
        );
        builder.addRecipeArrow().setPosition(59, 41);
        builder.addSlottedWidget(widget, builder.getRecipeSlots().getSlots(RecipeIngredientRole.INPUT));
        builder.addGuiEventListener(widget);
    }

    @Override
    public void getTooltip(
            mezz.jei.api.gui.builder.ITooltipBuilder tooltip,
            FrameAssemblyRecipeView recipe,
            mezz.jei.api.gui.ingredient.IRecipeSlotsView slots,
            double mouseX,
            double mouseY) {
        if (mouseX >= 62 && mouseX < 78 && mouseY >= 61 && mouseY < 77) {
            recipe.toolRequirement().tooltip().forEach(tooltip::add);
        }
    }

    @Override
    public net.minecraft.resources.ResourceLocation getRegistryName(FrameAssemblyRecipeView recipe) {
        return recipe.id();
    }
}
