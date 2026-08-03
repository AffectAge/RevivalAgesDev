package com.protyvkultury.revivalages.integration.emi;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.core.process.ProcessRuleType;
import com.protyvkultury.revivalages.core.process.ProcessRuleView;
import com.protyvkultury.revivalages.core.process.ProcessRulePresentation;
import com.protyvkultury.revivalages.core.process.ProcessRuleLayout;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveFluidSlotGeometry;
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
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

final class PrimitiveEmiRecipe implements EmiRecipe {
    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final PrimitiveRecipeView view;
    private final Layout layout;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    PrimitiveEmiRecipe(EmiRecipeCategory category, Layout layout, PrimitiveRecipeView view) {
        this(category, layout, layout.texture, view);
    }

    PrimitiveEmiRecipe(EmiRecipeCategory category, Layout layout, String presentationId, PrimitiveRecipeView view) {
        this.category = category;
        this.layout = layout;
        this.view = view;
        this.id =
                RevivalAges.id(
            "/emi/" + presentationId + "/" + view.id().getNamespace() + "/" + view.id().getPath());
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
        return this.layout.backgroundHeight + toolRequirementHeight() + ruleLayout().height() + 26;
    }

    public void addWidgets(WidgetHolder widgets) {
        ResourceLocation texture = RevivalAges.id("textures/gui/" + this.layout.texture + ".png");
        widgets.addTexture(texture, 0, 0, this.layout.width, this.layout.backgroundHeight, 0, 0);
        if (!this.layout.usesEmbeddedArrow) {
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
        }
        if (this.layout.hasFlame) {
            widgets.addAnimatedTexture(
                    texture,
                    this.layout.flameX,
                    this.layout.flameY,
                    14,
                    14,
                    this.layout.flameU,
                    this.layout.flameV,
                    3000,
                    false,
                    true,
                    false);
        }
        switch (this.layout) {
            case CAMPFIRE, STONE_OVEN:
                {
                    this.addItemInputs(widgets, new int[][] {{0, 0}});
                    this.addItemOutputs(widgets, new int[][] {{60, 10}});
                    break;
                }
            case CHOPPING, ANVIL:
                {
                    this.addItemInputs(widgets, new int[][] {{0, 17}});
                    this.addItemOutputs(widgets, new int[][] {{60, 18}, {83, 18}});
                    break;
                }
            case GRINDING:
                this.addItemInputs(widgets, new int[][] {{34, 27}});
                this.addItemOutputs(widgets, new int[][] {{90, 27}, {90, 50}});
                break;
            case PRESSING:
                this.addItemInputs(widgets, new int[][] {{34, 32}});
                this.addItemOutputs(widgets, new int[][] {{90, 32}});
                this.addFluidTank(widgets, this.view.fluidOutput(), PrimitiveFluidSlotGeometry.PRESSING_OUTPUT);
                break;
            case PIT_KILN, PIT_BURN:
                {
                    this.addItemInputs(widgets, new int[][] {{0, 22}});
                    this.addChanceOutputs(widgets, 60, 18, 83, 22);
                    break;
                }
            case BARREL:
                {
                    this.addItemInputs(widgets, new int[][] {{0, 0}, {19, 0}, {0, 19}, {19, 19}});
                    this.addFluidTank(widgets, this.view.fluidInput(), PrimitiveFluidSlotGeometry.BARREL_INPUT);
                    this.addFluidTank(widgets, this.view.fluidOutput(), PrimitiveFluidSlotGeometry.BARREL_OUTPUT);
                    break;
                }
            case SOAKING_POT:
                {
                    this.addItemInputs(widgets, new int[][] {{0, 0}});
                    this.addFluidTank(widgets, this.view.fluidInput(), PrimitiveFluidSlotGeometry.SOAKING_POT_INPUT);
                    this.addItemOutputs(widgets, new int[][] {{60, 19}});
                    break;
                }
            case TANNING_RACK:
                {
                    this.addItemInputs(widgets, new int[][] {{1, 9}});
                    this.addItemOutputs(widgets, new int[][] {{53, 9}, {77, 9}});
                    break;
                }
            case STONE_SAWMILL:
                this.addItemInputs(widgets, new int[][] {{0, 0}, {0, 19}});
                this.addChanceOutputs(widgets, 60, 16, 83, 20);
                break;
            case STONE_KILN:
                this.addItemInputs(widgets, new int[][] {{0, 0}});
                this.addChanceOutputs(widgets, 60, 10, 83, 14);
                break;
            case STONE_CRUCIBLE:
                this.addItemInputs(widgets, new int[][] {{0, 0}});
                this.addFluidTank(
                        widgets, this.view.fluidOutput(), PrimitiveFluidSlotGeometry.STONE_CRUCIBLE_OUTPUT);
                break;
        }
        addToolRequirementWidgets(widgets);
        addConditionWidgets(widgets, texture);
        int textY = conditionY() + ruleLayout().height() + 2;
        if (this.view.processingTime() > 0) {
            widgets
                    .addText(this.processingTimeText(), this.layout.width / 2, textY, -8355712, false)
                    .horizontalAlign(TextWidget.Alignment.CENTER);
            textY += 10;
        }
        Component detail = this.view.detail();
        if (!detail.getString().isEmpty()) {
            widgets
                    .addText(detail, this.layout.width / 2, textY, -8355712, false)
                    .horizontalAlign(TextWidget.Alignment.CENTER);
        }
    }

    private Component processingTimeText() {
        return Component.translatable(
                "gui.revivalages.recipe.time",
                String.format(
                        Locale.ROOT,
                        "%.1f",
                        (double) this.view.processingTime() / 20.0));
    }

    private void addConditionWidgets(WidgetHolder widgets, ResourceLocation texture) {
        int y = conditionY();
        ProcessRuleLayout ruleLayout = ruleLayout();
        for (int index = 0; index < this.view.processRules().size(); index++) {
            ProcessRuleView condition = this.view.processRules().get(index);
            int iconX = ruleLayout.x(index);
            ProcessRulePresentation presentation = condition.presentation();
            widgets.addTexture(
                            ProcessRulePresentation.ATLAS,
                            iconX,
                            ruleLayout.y(y, index),
                            ProcessRulePresentation.ICON_SIZE,
                            ProcessRulePresentation.ICON_SIZE,
                            presentation.u(),
                            presentation.v(),
                            ProcessRulePresentation.ICON_SIZE,
                            ProcessRulePresentation.ICON_SIZE,
                            ProcessRulePresentation.ATLAS_WIDTH,
                            ProcessRulePresentation.ATLAS_HEIGHT)
                    .tooltip((mouseX, mouseY) -> ruleTooltip(condition));
        }
    }

    private int conditionY() {
        return this.layout.backgroundHeight + 2 + toolRequirementHeight();
    }

    private int toolRequirementHeight() {
        return this.view.toolRequirements().isEmpty() ? 0 : 20;
    }

    private void addToolRequirementWidgets(WidgetHolder widgets) {
        int x = Math.max(0, (this.layout.width - this.view.toolRequirements().size() * 18 + 2) / 2);
        int y = this.layout.backgroundHeight + 2;
        for (int index = 0; index < this.view.toolRequirements().size(); index++) {
            var requirement = this.view.toolRequirements().get(index);
            var slot = widgets.addSlot(EmiIngredient.of(requirement.ingredient()), x + index * 18, y)
                    .catalyst(true);
            requirement.tooltip().forEach(slot::appendTooltip);
        }
    }

    private ProcessRuleLayout ruleLayout() {
        return ProcessRuleLayout.of(this.layout.width, this.view.processRules().size());
    }

    private static List<ClientTooltipComponent> ruleTooltip(ProcessRuleView rule) {
        return ProcessRulePresentation.viewerTooltip(rule).stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();
    }

    private void addItemInputs(WidgetHolder widgets, int[][] positions) {
        for (int index = 0;
                index < this.view.itemInputs().size() && index < positions.length;
                ++index) {
            widgets
                    .addSlot(
                            EmiIngredient.of(this.view.itemInputs().get(index)),
                            positions[index][0],
                            positions[index][1])
                    .drawBack(false);
        }
    }

    private void addItemOutputs(WidgetHolder widgets, int[][] positions) {
        for (int index = 0;
                index < this.view.itemOutputs().size() && index < positions.length;
                ++index) {
            widgets
                    .addSlot(
                            EmiStack.of(this.view.itemOutputs().get(index)),
                            positions[index][0],
                            positions[index][1])
                    .drawBack(false)
                    .recipeContext((EmiRecipe) this);
        }
    }

    private void addChanceOutputs(WidgetHolder widgets, int resultX, int resultY, int chanceX, int chanceY) {
        if (this.view.itemOutputs().isEmpty()) {
            return;
        }
        widgets.addSlot(EmiStack.of(this.view.itemOutputs().getFirst()), resultX, resultY).drawBack(false).recipeContext(this);
        if (this.view.itemOutputs().size() > 1) {
            List<EmiIngredient> chanceOutputs = this.view.itemOutputs().subList(1, this.view.itemOutputs().size()).stream()
                    .map(EmiStack::of)
                    .map(output -> (EmiIngredient) output)
                    .toList();
            widgets.addSlot(EmiIngredient.of(chanceOutputs), chanceX, chanceY).drawBack(false).recipeContext(this);
        }
    }

    private void addFluidTank(
            WidgetHolder widgets, FluidStack fluid, PrimitiveFluidSlotGeometry geometry) {
        if (fluid.isEmpty()) {
            return;
        }
        PrimitiveFluidSlotGeometry.EmiTankBounds bounds = geometry.emiTankBounds();
        widgets.addTank(
                        EmiStack.of(fluid.getFluid(), fluid.getAmount()),
                        bounds.x(),
                        bounds.y(),
                        bounds.width(),
                        bounds.height(),
                        fluid.getAmount())
                .recipeContext(this);
    }

    public RecipeHolder<?> getBackingRecipe() {
        return this.view.backingRecipe();
    }

    static enum Layout {
        CAMPFIRE("campfire", 82, 33, 82, 14, 24, 10, true, 82, 0, 1, 19),
        CHOPPING("chopping", 82, 40, 82, 0, 24, 18, false, 0, 0, 0, 0),
        PIT_KILN("pit_kiln", 101, 54, 101, 14, 24, 18, true, 101, 0, 1, 7),
        PIT_BURN("pit_kiln", 101, 54, 101, 14, 24, 18, true, 101, 0, 1, 6),
        BARREL("barrel", 97, 51, 101, 0, 42, 19, false, 0, 0, 0, 0),
        SOAKING_POT("soaking_pot", 82, 56, 82, 0, 24, 19, false, 0, 0, 0, 0),
        TANNING_RACK("tanning_rack", 95, 45, 119, 0, 24, 10, false, 0, 0, 0, 0),
        STONE_SAWMILL("stone_sawmill", 101, 38, 101, 0, 24, 16, false, 0, 0, 0, 0),
        STONE_OVEN("stone_oven", 82, 33, 82, 14, 24, 10, true, 82, 0, 1, 19),
        STONE_KILN("stone_kiln", 101, 46, 101, 14, 24, 10, true, 101, 0, 1, 19),
        STONE_CRUCIBLE("stone_crucible", 82, 33, 82, 14, 24, 10, true, 82, 0, 1, 19),
        ANVIL("anvil", 82, 40, 82, 0, 24, 18, false, 0, 0, 0, 0),
        GRINDING("animal_power_grinding", 146, 85, 0, 0, 0, 0, false, 0, 0, 0, 0, true),
        PRESSING("animal_power_pressing", 146, 74, 0, 0, 0, 0, false, 0, 0, 0, 0, true);

        final String texture;
        final int width;
        final int backgroundHeight;
        final int arrowU;
        final int arrowV;
        final int arrowX;
        final int arrowY;
        final boolean hasFlame;
        final int flameU;
        final int flameV;
        final int flameX;
        final int flameY;
        final boolean usesEmbeddedArrow;

        private Layout(
                String texture,
                int width,
                int backgroundHeight,
                int arrowU,
                int arrowV,
                int arrowX,
                int arrowY,
                boolean hasFlame,
                int flameU,
                int flameV,
                int flameX,
                int flameY) {
            this(
                    texture,
                    width,
                    backgroundHeight,
                    arrowU,
                    arrowV,
                    arrowX,
                    arrowY,
                    hasFlame,
                    flameU,
                    flameV,
                    flameX,
                    flameY,
                    false
            );
        }

        private Layout(
                String texture,
                int width,
                int backgroundHeight,
                int arrowU,
                int arrowV,
                int arrowX,
                int arrowY,
                boolean hasFlame,
                int flameU,
                int flameV,
                int flameX,
                int flameY,
                boolean usesEmbeddedArrow) {
            this.texture = texture;
            this.width = width;
            this.backgroundHeight = backgroundHeight;
            this.arrowU = arrowU;
            this.arrowV = arrowV;
            this.arrowX = arrowX;
            this.arrowY = arrowY;
            this.hasFlame = hasFlame;
            this.flameU = flameU;
            this.flameV = flameV;
            this.flameX = flameX;
            this.flameY = flameY;
            this.usesEmbeddedArrow = usesEmbeddedArrow;
        }
    }
}
