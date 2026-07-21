package com.protyvkultury.revivalages.integration.emi;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeView;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;

final class PrimitiveEmiRecipe implements EmiRecipe {
    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final PrimitiveRecipeView view;
    private final Layout layout;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    PrimitiveEmiRecipe(EmiRecipeCategory category, Layout layout, PrimitiveRecipeView view) {
        this.category = category;
        this.layout = layout;
        this.view = view;
        this.id =
                RevivalAges.id(
            "/emi/" + layout.texture + "/" + view.id().getNamespace() + "/" + view.id().getPath());
        this.inputs = new ArrayList<EmiIngredient>();
        view.itemInputs()
                .forEach(ingredient -> this.inputs.add(EmiIngredient.of((Ingredient) ingredient)));
        if (!view.fluidInput().isEmpty()) {
            this.inputs.add(
                    (EmiIngredient)
                            EmiStack.of(
                                    (Fluid) view.fluidInput().getFluid(), (long) view.fluidInput().getAmount()));
        }
        this.outputs = new ArrayList<EmiStack>();
        view.itemOutputs().forEach(stack -> this.outputs.add(EmiStack.of((ItemStack) stack)));
        if (!view.fluidOutput().isEmpty()) {
            this.outputs.add(
                    EmiStack.of(
                            (Fluid) view.fluidOutput().getFluid(), (long) view.fluidOutput().getAmount()));
        }
    }

    public EmiRecipeCategory getCategory() {
        return this.category;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public List<EmiIngredient> getInputs() {
        return this.inputs;
    }

    public List<EmiStack> getOutputs() {
        return this.outputs;
    }

    public int getDisplayWidth() {
        return this.layout.width;
    }

    public int getDisplayHeight() {
        return this.layout.backgroundHeight + 13;
    }

    public void addWidgets(WidgetHolder widgets) {
        ResourceLocation texture = RevivalAges.id("textures/gui/" + this.layout.texture + ".png");
        widgets.addTexture(texture, 0, 0, this.layout.width, this.layout.backgroundHeight, 0, 0);
        widgets.addAnimatedTexture(
                texture,
                this.layout.arrowX,
                this.layout.arrowY,
                24,
                17,
                this.layout.arrowU,
                this.layout.arrowV,
                Math.max(1000, this.view.processingTime() * 50),
                true,
                false,
                false);
        switch (this.layout.ordinal()) {
            case 0:
                {
                    this.addItemInputs(widgets, new int[][] {{0, 0}});
                    this.addItemOutputs(widgets, new int[][] {{60, 10}});
                    break;
                }
            case 1:
                {
                    this.addItemInputs(widgets, new int[][] {{0, 17}});
                    this.addItemOutputs(widgets, new int[][] {{60, 18}});
                    break;
                }
            case 2:
                {
                    this.addItemInputs(widgets, new int[][] {{0, 22}});
                    this.addItemOutputs(widgets, new int[][] {{60, 18}, {83, 22}});
                    break;
                }
            case 3:
                {
                    this.addItemInputs(widgets, new int[][] {{0, 0}, {19, 0}, {0, 19}, {19, 19}});
                    widgets.addTank(
                            this.inputs.get(this.view.itemInputs().size()),
                            1,
                            39,
                            35,
                            11,
                            this.view.fluidInput().getAmount());
                    widgets
                            .addTank(
                                    (EmiIngredient) this.outputs.getFirst(),
                                    72,
                                    1,
                                    24,
                                    49,
                                    this.view.fluidOutput().getAmount())
                            .recipeContext((EmiRecipe) this);
                    break;
                }
            case 4:
                {
                    this.addItemInputs(widgets, new int[][] {{0, 0}});
                    widgets.addTank(
                            this.inputs.get(this.view.itemInputs().size()),
                            1,
                            20,
                            16,
                            16,
                            this.view.fluidInput().getAmount());
                    this.addItemOutputs(widgets, new int[][] {{60, 19}});
                    break;
                }
            case 5:
                {
                    this.addItemInputs(widgets, new int[][] {{0, 3}});
                    this.addItemOutputs(widgets, new int[][] {{60, 4}, {83, 4}});
                }
        }
        Component detail = this.view.detail();
        if (detail.getString().isEmpty() && this.view.processingTime() > 0) {
            detail =
                    Component.translatable(
                            (String) "gui.revivalages.recipe.time",
                            (Object[])
                                    new Object[] {
                                        String.format(Locale.ROOT, "%.1f", (double) this.view.processingTime() / 20.0)
                                    });
        }
        if (!detail.getString().isEmpty()) {
            widgets
                    .addText(detail, this.layout.width / 2, this.layout.backgroundHeight + 2, -1, true)
                    .horizontalAlign(TextWidget.Alignment.CENTER);
        }
    }

    private void addItemInputs(WidgetHolder widgets, int[][] positions) {
        for (int index = 0;
                index < this.view.itemInputs().size() && index < positions.length;
                ++index) {
            widgets
                    .addSlot(this.inputs.get(index), positions[index][0], positions[index][1])
                    .drawBack(false);
        }
    }

    private void addItemOutputs(WidgetHolder widgets, int[][] positions) {
        for (int index = 0;
                index < this.view.itemOutputs().size() && index < positions.length;
                ++index) {
            widgets
                    .addSlot(
                            (EmiIngredient) this.outputs.get(index), positions[index][0], positions[index][1])
                    .drawBack(false)
                    .recipeContext((EmiRecipe) this);
        }
    }

    public RecipeHolder<?> getBackingRecipe() {
        return this.view.backingRecipe();
    }

    static enum Layout {
        CAMPFIRE("campfire", 82, 33, 82, 14, 24, 10),
        CHOPPING("chopping", 82, 40, 82, 0, 24, 18),
        PIT_KILN("pit_kiln", 101, 54, 101, 14, 24, 18),
        BARREL("barrel", 97, 51, 101, 0, 42, 19),
        SOAKING_POT("soaking_pot", 82, 56, 82, 0, 24, 19),
        TANNING_RACK("tanning_rack", 101, 26, 82, 0, 24, 4);

        final String texture;
        final int width;
        final int backgroundHeight;
        final int arrowU;
        final int arrowV;
        final int arrowX;
        final int arrowY;

        private Layout(
                String texture,
                int width,
                int backgroundHeight,
                int arrowU,
                int arrowV,
                int arrowX,
                int arrowY) {
            this.texture = texture;
            this.width = width;
            this.backgroundHeight = backgroundHeight;
            this.arrowU = arrowU;
            this.arrowV = arrowV;
            this.arrowX = arrowX;
            this.arrowY = arrowY;
        }
    }
}
