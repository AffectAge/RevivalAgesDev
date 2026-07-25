package com.protyvkultury.revivalages.integration.jei;

import com.protyvkultury.revivalages.feature.technology.knapping.client.KnappingScreen;
import com.protyvkultury.revivalages.feature.technology.knapping.view.KnappingRecipeView;
import java.util.Arrays;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

final class KnappingJeiCategory extends AbstractRecipeCategory<KnappingRecipeView> {

    KnappingJeiCategory(
            IGuiHelper guiHelper,
            RecipeType<KnappingRecipeView> type,
            ItemLike icon
    ) {
        super(
                type,
                Component.translatable("jei.revivalages.category.knapping"),
                guiHelper.createDrawableItemLike(icon),
                155,
                82
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, KnappingRecipeView recipe, IFocusGroup focuses) {
        int count = recipe.effectiveInput().count();
        var inputs = Arrays.stream(recipe.effectiveInput().getItems())
                .map(stack -> {
                    ItemStack copy = stack.copy();
                    copy.setCount(count);
                    return copy;
                })
                .toList();
        builder.addSlot(RecipeIngredientRole.INPUT, 0, 33)
                .setStandardSlotBackground()
                .setSlotName("input")
                .addItemStacks(inputs);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 137, 33)
                .setOutputSlotBackground()
                .addItemStack(recipe.result());
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            KnappingRecipeView recipe,
            IFocusGroup focuses
    ) {
        builder.addRecipeArrow().setPosition(106, 33);
    }

    @Override
    public void draw(
            KnappingRecipeView recipe,
            IRecipeSlotsView slots,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        ItemStack input = slots.findSlotByName("input")
                .flatMap(mezz.jei.api.gui.ingredient.IRecipeSlotView::getDisplayedItemStack)
                .orElseGet(recipe::viewerIcon);
        int offsetX = Math.floorDiv(5 - recipe.pattern().width(), 2);
        int offsetY = Math.floorDiv(5 - recipe.pattern().height(), 2);
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                int localX = x - offsetX;
                int localY = y - offsetY;
                boolean on = recipe.pattern().defaultOn();
                if (localX >= 0
                        && localX < recipe.pattern().width()
                        && localY >= 0
                        && localY < recipe.pattern().height()) {
                    on = recipe.pattern().on(localX, localY);
                }
                ResourceLocation texture = on
                        ? KnappingScreen.textureFor(input, recipe.type(), true)
                        : recipe.hasOffTexture()
                                ? KnappingScreen.textureFor(input, recipe.type(), false)
                                : null;
                if (texture != null) {
                    graphics.blit(texture, 21 + x * 16, 1 + y * 16, 0, 0, 16, 16, 16, 16);
                }
            }
        }
    }

    @Override
    public ResourceLocation getRegistryName(KnappingRecipeView recipe) {
        return recipe.id();
    }
}
