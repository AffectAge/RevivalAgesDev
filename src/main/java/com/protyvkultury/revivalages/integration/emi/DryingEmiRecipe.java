package com.protyvkultury.revivalages.integration.emi;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.dryingrack.view.DryingRecipeView;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeLayout;
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
    private static final PrimitiveRecipeLayout LAYOUT = PrimitiveRecipeLayout.DRYING;
    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final DryingRecipeView view;
    private final EmiIngredient input;
    private final EmiStack output;

    DryingEmiRecipe(EmiRecipeCategory category, String categoryPath, DryingRecipeView view) {
        this.category = category;
        this.id =
                RevivalAges.id(
            "/emi/" + categoryPath + "/" + view.id().getNamespace() + "/" + view.id().getPath());
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
        return LAYOUT.backgroundWidth();
    }

    public int getDisplayHeight() {
        return LAYOUT.backgroundHeight() + 12;
    }

    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEXTURE, 0, 0, LAYOUT.backgroundWidth(), LAYOUT.backgroundHeight(), 0, 0);
        widgets.addAnimatedTexture(
                TEXTURE,
                LAYOUT.progressArrow().x(),
                LAYOUT.progressArrow().y(),
                24,
                17,
                LAYOUT.arrowSourceX(),
                LAYOUT.arrowSourceY(),
                this.view.processingTime() * 50,
                true,
                false,
                false);
        PrimitiveRecipeLayout.Position inputPosition = LAYOUT.itemInputs().getFirst();
        PrimitiveRecipeLayout.Position outputPosition = LAYOUT.itemOutputs().getFirst();
        widgets.addSlot(this.input, inputPosition.x(), inputPosition.y()).drawBack(false);
        widgets
                .addSlot(this.output, outputPosition.x(), outputPosition.y())
                .drawBack(false)
                .recipeContext(this);
        widgets
                .addText(
                        Component.translatable(
                                "gui.revivalages.drying_rack.time",
                                String.format(
                                        Locale.ROOT,
                                        "%.1f",
                                        (double) this.view.processingTime() / 20.0
                                )
                        ),
                        LAYOUT.backgroundWidth() / 2,
                        LAYOUT.backgroundHeight() + 2,
                        -1,
                        true)
                .horizontalAlign(TextWidget.Alignment.CENTER);
    }

    public RecipeHolder<?> getBackingRecipe() {
        return this.view.holder();
    }
}
