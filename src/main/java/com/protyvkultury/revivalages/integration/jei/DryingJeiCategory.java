package com.protyvkultury.revivalages.integration.jei;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.dryingrack.view.DryingRecipeView;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeLayout;
import java.util.Locale;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

final class DryingJeiCategory implements IRecipeCategory<DryingRecipeView> {
    private static final PrimitiveRecipeLayout LAYOUT = PrimitiveRecipeLayout.DRYING;
    private static final int HEIGHT = LAYOUT.backgroundHeight() + 16;
    private static final ResourceLocation TEXTURE = RevivalAges.id("textures/gui/drying_rack.png");
    private final RecipeType<DryingRecipeView> recipeType;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable arrow;

    DryingJeiCategory(
            IGuiHelper guiHelper,
            RecipeType<DryingRecipeView> recipeType,
            String titleKey,
            Item iconItem) {
        this.recipeType = recipeType;
        this.title = Component.translatable((String) titleKey);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack((ItemLike) iconItem));
        this.background = guiHelper.createDrawable(
                TEXTURE, 0, 0, LAYOUT.backgroundWidth(), LAYOUT.backgroundHeight());
        this.arrow =
                guiHelper.createAnimatedDrawable(
                        guiHelper.createDrawable(TEXTURE, LAYOUT.arrowSourceX(), LAYOUT.arrowSourceY(), 24, 17),
                        200,
                        IDrawableAnimated.StartDirection.LEFT,
                        false);
    }

    public RecipeType<DryingRecipeView> getRecipeType() {
        return this.recipeType;
    }

    public Component getTitle() {
        return this.title;
    }

    public int getWidth() {
        return LAYOUT.backgroundWidth();
    }

    public int getHeight() {
        return 42;
    }

    public IDrawable getIcon() {
        return this.icon;
    }

    public void setRecipe(IRecipeLayoutBuilder builder, DryingRecipeView view, IFocusGroup focuses) {
        PrimitiveRecipeLayout.Position input = LAYOUT.itemInputs().getFirst();
        PrimitiveRecipeLayout.Position output = LAYOUT.itemOutputs().getFirst();
        builder.addInputSlot(input.x(), input.y()).addIngredients(view.displayIngredient());
        builder
                .addOutputSlot(output.x(), output.y())
                .addItemStack(
                        view.recipe()
                                .getResultItem(
                                        (HolderLookup.Provider) Minecraft.getInstance().level.registryAccess()));
    }

    public void draw(
            DryingRecipeView view,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        this.background.draw(graphics, 0, 0);
        this.arrow.draw(graphics, LAYOUT.progressArrow().x(), LAYOUT.progressArrow().y());
        MutableComponent duration =
                Component.translatable(
                        (String) "gui.revivalages.drying_rack.time",
                        (Object[])
                                new Object[] {
                                    String.format(Locale.ROOT, "%.1f", (double) view.processingTime() / 20.0)
                                });
        int x = (this.getWidth() - Minecraft.getInstance().font.width((FormattedText) duration)) / 2;
        graphics.drawString(
                Minecraft.getInstance().font,
                (Component) duration,
                x,
                LAYOUT.backgroundHeight() + 2,
                -8355712,
                false);
    }

    public ResourceLocation getRegistryName(DryingRecipeView view) {
        return view.id();
    }
}
