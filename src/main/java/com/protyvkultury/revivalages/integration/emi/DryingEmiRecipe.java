package com.protyvkultury.revivalages.integration.emi;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.dryingrack.view.DryingRecipeView;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

final class DryingEmiRecipe implements EmiRecipe {
    private static final ResourceLocation TEXTURE = RevivalAges.id("textures/gui/drying_rack.png");
    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final DryingRecipeView view;
    private final EmiIngredient input;
    private final EmiStack output;

    DryingEmiRecipe(EmiRecipeCategory category, String categoryPath, DryingRecipeView view) {
        this.category = category;
        this.id =
                RevivalAges.id(
            "emi/" + categoryPath + "/" + view.id().getNamespace() + "/" + view.id().getPath());
        this.view = view;
        this.input = EmiIngredient.of(view.displayIngredient());
        this.output = EmiStack.of(
                view.recipe().getResultItem(Minecraft.getInstance().level.registryAccess())
        );
    }

    public EmiRecipeCategory getCategory() {
        return this.category;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public List<EmiIngredient> getInputs() {
        return List.of(this.input);
    }

    public List<EmiStack> getOutputs() {
        return List.of(this.output);
    }

    public int getDisplayWidth() {
        return 82;
    }

    public int getDisplayHeight() {
        return 38;
    }

    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEXTURE, 0, 0, 82, 26, 0, 0);
        widgets.addAnimatedTexture(
                TEXTURE, 24, 4, 24, 17, 82, 0, this.view.recipe().dryingTime() * 50, true, false, false);
        widgets.addSlot(this.input, 0, 3).drawBack(false);
        widgets
                .addSlot(this.output, 60, 4)
                .drawBack(false)
                .recipeContext(this);
        widgets
                .addText(
                        Component.translatable(
                                "gui.revivalages.drying_rack.time",
                                String.format(
                                        Locale.ROOT,
                                        "%.1f",
                                        (double) this.view.recipe().dryingTime() / 20.0
                                )
                        ),
                        41,
                        28,
                        -1,
                        true)
                .horizontalAlign(TextWidget.Alignment.CENTER);
    }

    public RecipeHolder<?> getBackingRecipe() {
        return this.view.holder();
    }
}
