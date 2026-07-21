package com.protyvkultury.revivalages.integration.jei;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeView;
import java.util.Locale;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;

final class PrimitiveJeiCategory implements IRecipeCategory<PrimitiveRecipeView> {
    private final RecipeType<PrimitiveRecipeView> type;
    private final Layout layout;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable arrow;

    PrimitiveJeiCategory(
            IGuiHelper helper,
            RecipeType<PrimitiveRecipeView> type,
            Layout layout,
            String titleKey,
            Item iconItem) {
        this.type = type;
        this.layout = layout;
        this.title = Component.translatable((String) titleKey);
        this.icon = helper.createDrawableItemStack(new ItemStack((ItemLike) iconItem));
        ResourceLocation texture = RevivalAges.id("textures/gui/" + layout.texture + ".png");
        this.background = helper.createDrawable(texture, 0, 0, layout.width, layout.backgroundHeight);
        this.arrow =
                helper.createAnimatedDrawable(
                        helper.createDrawable(
                                texture, layout.arrowU, layout.arrowV, layout.arrowWidth, layout.arrowHeight),
                        200,
                        IDrawableAnimated.StartDirection.LEFT,
                        false);
    }

    public RecipeType<PrimitiveRecipeView> getRecipeType() {
        return this.type;
    }

    public Component getTitle() {
        return this.title;
    }

    public IDrawable getIcon() {
        return this.icon;
    }

    public int getWidth() {
        return this.layout.width;
    }

    public int getHeight() {
        return this.layout.backgroundHeight + 13;
    }

    public void setRecipe(
            IRecipeLayoutBuilder builder, PrimitiveRecipeView recipe, IFocusGroup focuses) {
        switch (this.layout.ordinal()) {
            case 0:
                {
                    PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{0, 0}});
                    PrimitiveJeiCategory.addItemOutputs(builder, recipe, new int[][] {{60, 10}});
                    break;
                }
            case 1:
                {
                    PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{0, 17}});
                    PrimitiveJeiCategory.addItemOutputs(builder, recipe, new int[][] {{60, 18}});
                    break;
                }
            case 2:
                {
                    PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{0, 22}});
                    PrimitiveJeiCategory.addItemOutputs(builder, recipe, new int[][] {{60, 18}, {83, 22}});
                    break;
                }
            case 3:
                {
                    PrimitiveJeiCategory.addItemInputs(
                            builder, recipe, new int[][] {{0, 0}, {19, 0}, {0, 19}, {19, 19}});
                    PrimitiveJeiCategory.addFluid(
                            builder, RecipeIngredientRole.INPUT, recipe.fluidInput(), 1, 39, 35, 11);
                    PrimitiveJeiCategory.addFluid(
                            builder, RecipeIngredientRole.OUTPUT, recipe.fluidOutput(), 72, 1, 24, 49);
                    break;
                }
            case 4:
                {
                    PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{0, 0}});
                    PrimitiveJeiCategory.addFluid(
                            builder, RecipeIngredientRole.INPUT, recipe.fluidInput(), 1, 20, 16, 16);
                    PrimitiveJeiCategory.addItemOutputs(builder, recipe, new int[][] {{60, 19}});
                    break;
                }
            case 5:
                {
                    PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{0, 3}});
                    PrimitiveJeiCategory.addItemOutputs(builder, recipe, new int[][] {{60, 4}, {83, 4}});
                }
        }
    }

    private static void addItemInputs(
            IRecipeLayoutBuilder builder, PrimitiveRecipeView recipe, int[][] positions) {
        for (int index = 0; index < recipe.itemInputs().size() && index < positions.length; ++index) {
            builder
                    .addInputSlot(positions[index][0], positions[index][1])
                    .addIngredients(recipe.itemInputs().get(index));
        }
    }

    private static void addItemOutputs(
            IRecipeLayoutBuilder builder, PrimitiveRecipeView recipe, int[][] positions) {
        for (int index = 0; index < recipe.itemOutputs().size() && index < positions.length; ++index) {
            builder
                    .addOutputSlot(positions[index][0], positions[index][1])
                    .addItemStack(recipe.itemOutputs().get(index));
        }
    }

    private static void addFluid(
            IRecipeLayoutBuilder builder,
            RecipeIngredientRole role,
            FluidStack fluid,
            int x,
            int y,
            int width,
            int height) {
        if (fluid.isEmpty()) {
            return;
        }
        builder
                .addSlot(role, x, y)
                .setFluidRenderer((long) fluid.getAmount(), false, width, height)
                .addFluidStack(fluid.getFluid(), (long) fluid.getAmount());
    }

    public void draw(
            PrimitiveRecipeView recipe,
            IRecipeSlotsView slots,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        this.background.draw(graphics, 0, 0);
        this.arrow.draw(graphics, this.layout.arrowX, this.layout.arrowY);
        Component detail = recipe.detail();
        if (detail.getString().isEmpty() && recipe.processingTime() > 0) {
            detail =
                    Component.translatable(
                            (String) "gui.revivalages.recipe.time",
                            (Object[])
                                    new Object[] {
                                        String.format(Locale.ROOT, "%.1f", (double) recipe.processingTime() / 20.0)
                                    });
        }
        if (!detail.getString().isEmpty()) {
            int x = (this.getWidth() - Minecraft.getInstance().font.width((FormattedText) detail)) / 2;
            graphics.drawString(
                    Minecraft.getInstance().font,
                    detail,
                    Math.max(0, x),
                    this.layout.backgroundHeight + 2,
                    -8355712,
                    false);
        }
    }

    public ResourceLocation getRegistryName(PrimitiveRecipeView recipe) {
        return recipe.id();
    }

    static enum Layout {
        CAMPFIRE("campfire", 82, 33, 82, 14, 24, 17, 24, 10),
        CHOPPING("chopping", 82, 40, 82, 0, 24, 17, 24, 18),
        PIT_KILN("pit_kiln", 101, 54, 101, 14, 24, 17, 24, 18),
        BARREL("barrel", 97, 51, 101, 0, 24, 17, 42, 19),
        SOAKING_POT("soaking_pot", 82, 56, 82, 0, 24, 17, 24, 19),
        TANNING_RACK("tanning_rack", 101, 26, 82, 0, 24, 17, 24, 4);

        final String texture;
        final int width;
        final int backgroundHeight;
        final int arrowU;
        final int arrowV;
        final int arrowWidth;
        final int arrowHeight;
        final int arrowX;
        final int arrowY;

        private Layout(
                String texture,
                int width,
                int backgroundHeight,
                int arrowU,
                int arrowV,
                int arrowWidth,
                int arrowHeight,
                int arrowX,
                int arrowY) {
            this.texture = texture;
            this.width = width;
            this.backgroundHeight = backgroundHeight;
            this.arrowU = arrowU;
            this.arrowV = arrowV;
            this.arrowWidth = arrowWidth;
            this.arrowHeight = arrowHeight;
            this.arrowX = arrowX;
            this.arrowY = arrowY;
        }
    }
}
