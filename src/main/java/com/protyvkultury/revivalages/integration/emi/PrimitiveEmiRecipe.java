package com.protyvkultury.revivalages.integration.emi;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.core.process.ProcessRuleType;
import com.protyvkultury.revivalages.core.process.ProcessRuleView;
import com.protyvkultury.revivalages.core.process.ProcessRulePresentation;
import com.protyvkultury.revivalages.core.process.ProcessRuleLayout;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveFluidSlotGeometry;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeLayout;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeView;
import com.protyvkultury.revivalages.feature.technology.stonemachine.view.StoneMachineRecipeLayout;
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
            case CAMPFIRE:
                {
                    this.addItemInputs(widgets, this.layout.flow.itemInputs());
                    this.addItemOutputs(widgets, this.layout.flow.itemOutputs());
                    break;
                }
            case STONE_OVEN:
                addStoneMachineFlow(widgets, StoneMachineRecipeLayout.OVEN);
                break;
            case CHOPPING, ANVIL:
                {
                    this.addItemInputs(widgets, this.layout.flow.itemInputs());
                    this.addItemOutputs(widgets, this.layout.flow.itemOutputs());
                    break;
                }
            case GRINDING:
                this.addItemInputs(widgets, this.layout.flow.itemInputs());
                this.addItemOutputs(widgets, this.layout.flow.itemOutputs());
                break;
            case PRESSING:
                this.addItemInputs(widgets, this.layout.flow.itemInputs());
                this.addItemOutputs(widgets, this.layout.flow.itemOutputs());
                this.addFluidTank(widgets, this.view.fluidOutput(), PrimitiveFluidSlotGeometry.PRESSING_OUTPUT);
                break;
            case PIT_KILN, PIT_BURN:
                {
                    this.addItemInputs(widgets, this.layout.flow.itemInputs());
                    this.addChanceOutputs(widgets, this.layout.flow.itemOutputs());
                    break;
                }
            case BARREL:
                {
                    this.addItemInputs(widgets, this.layout.flow.itemInputs());
                    this.addFluidTank(widgets, this.view.fluidInput(), PrimitiveFluidSlotGeometry.BARREL_INPUT);
                    this.addFluidTank(widgets, this.view.fluidOutput(), PrimitiveFluidSlotGeometry.BARREL_OUTPUT);
                    break;
                }
            case SOAKING_POT:
                {
                    this.addItemInputs(widgets, this.layout.flow.itemInputs());
                    this.addFluidTank(widgets, this.view.fluidInput(), PrimitiveFluidSlotGeometry.SOAKING_POT_INPUT);
                    this.addItemOutputs(widgets, this.layout.flow.itemOutputs());
                    break;
                }
            case TANNING_RACK:
                {
                    this.addItemInputs(widgets, this.layout.flow.itemInputs());
                    this.addChanceOutputs(widgets, this.layout.flow.itemOutputs());
                    break;
                }
            case STONE_SAWMILL:
                this.addItemInputs(widgets, this.layout.flow.itemInputs());
                this.addChanceOutputs(widgets, this.layout.flow.itemOutputs());
                break;
            case STONE_KILN:
                addStoneMachineFlow(widgets, StoneMachineRecipeLayout.KILN);
                break;
            case STONE_CRUCIBLE:
                this.addItemInputs(
                        widgets,
                        new int[][] {{StoneMachineRecipeLayout.CRUCIBLE.input().x(),
                                StoneMachineRecipeLayout.CRUCIBLE.input().y()}}
                );
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

    private void addItemInputs(WidgetHolder widgets, List<PrimitiveRecipeLayout.Position> positions) {
        for (int index = 0; index < this.view.itemInputs().size() && index < positions.size(); ++index) {
            PrimitiveRecipeLayout.Position position = positions.get(index);
            widgets
                    .addSlot(EmiIngredient.of(this.view.itemInputs().get(index)), position.x(), position.y())
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

    private void addItemOutputs(WidgetHolder widgets, List<PrimitiveRecipeLayout.Position> positions) {
        for (int index = 0; index < this.view.itemOutputs().size() && index < positions.size(); ++index) {
            PrimitiveRecipeLayout.Position position = positions.get(index);
            widgets
                    .addSlot(EmiStack.of(this.view.itemOutputs().get(index)), position.x(), position.y())
                    .drawBack(false)
                    .recipeContext(this);
        }
    }

    private void addChanceOutputs(WidgetHolder widgets, int resultX, int resultY, int chanceX, int chanceY) {
        if (this.view.itemOutputs().isEmpty()) {
            return;
        }
        widgets
                .addSlot(EmiStack.of(this.view.itemOutputs().getFirst()), resultX, resultY)
                .drawBack(false)
                .recipeContext(this);
        if (this.view.itemOutputs().size() > 1) {
            List<EmiIngredient> chanceOutputs = this.view.itemOutputs()
                    .subList(1, this.view.itemOutputs().size())
                    .stream()
                    .map(EmiStack::of)
                    .map(output -> (EmiIngredient) output)
                    .toList();
            widgets.addSlot(EmiIngredient.of(chanceOutputs), chanceX, chanceY).drawBack(false).recipeContext(this);
        }
    }

    private void addChanceOutputs(WidgetHolder widgets, List<PrimitiveRecipeLayout.Position> positions) {
        if (positions.size() < 2) {
            throw new IllegalArgumentException("Chance-result layouts need two output positions");
        }
        PrimitiveRecipeLayout.Position result = positions.getFirst();
        PrimitiveRecipeLayout.Position chance = positions.get(1);
        this.addChanceOutputs(widgets, result.x(), result.y(), chance.x(), chance.y());
    }

    private void addStoneMachineFlow(WidgetHolder widgets, StoneMachineRecipeLayout layout) {
        this.addItemInputs(
                widgets,
                new int[][] {{layout.input().x(), layout.input().y()}}
        );
        if (layout.hasSecondaryOutput()) {
            StoneMachineRecipeLayout.Position secondaryOutput = layout.secondaryOutput();
            this.addChanceOutputs(
                    widgets,
                    layout.output().x(),
                    layout.output().y(),
                    secondaryOutput.x(),
                    secondaryOutput.y()
            );
            return;
        }
        this.addItemOutputs(
                widgets,
                new int[][] {{layout.output().x(), layout.output().y()}}
        );
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
        CAMPFIRE(PrimitiveRecipeLayout.CAMPFIRE),
        CHOPPING(PrimitiveRecipeLayout.CHOPPING),
        PIT_KILN(PrimitiveRecipeLayout.PIT_KILN),
        PIT_BURN(PrimitiveRecipeLayout.PIT_BURN),
        BARREL(PrimitiveRecipeLayout.BARREL),
        SOAKING_POT(PrimitiveRecipeLayout.SOAKING_POT),
        TANNING_RACK(PrimitiveRecipeLayout.TANNING_RACK),
        STONE_SAWMILL(PrimitiveRecipeLayout.STONE_SAWMILL),
        STONE_OVEN(
                "stone_oven",
                StoneMachineRecipeLayout.OVEN.backgroundWidth(),
                StoneMachineRecipeLayout.OVEN.backgroundHeight(),
                StoneMachineRecipeLayout.OVEN.backgroundWidth(),
                14,
                StoneMachineRecipeLayout.OVEN.progressArrow().x(),
                StoneMachineRecipeLayout.OVEN.progressArrow().y(),
                true,
                StoneMachineRecipeLayout.OVEN.backgroundWidth(),
                0,
                StoneMachineRecipeLayout.OVEN.flame().x(),
                StoneMachineRecipeLayout.OVEN.flame().y()
        ),
        STONE_KILN(
                "stone_kiln",
                StoneMachineRecipeLayout.KILN.backgroundWidth(),
                StoneMachineRecipeLayout.KILN.backgroundHeight(),
                StoneMachineRecipeLayout.KILN.backgroundWidth(),
                14,
                StoneMachineRecipeLayout.KILN.progressArrow().x(),
                StoneMachineRecipeLayout.KILN.progressArrow().y(),
                true,
                StoneMachineRecipeLayout.KILN.backgroundWidth(),
                0,
                StoneMachineRecipeLayout.KILN.flame().x(),
                StoneMachineRecipeLayout.KILN.flame().y()
        ),
        STONE_CRUCIBLE(
                "stone_crucible",
                StoneMachineRecipeLayout.CRUCIBLE.backgroundWidth(),
                StoneMachineRecipeLayout.CRUCIBLE.backgroundHeight(),
                StoneMachineRecipeLayout.CRUCIBLE.backgroundWidth(),
                14,
                StoneMachineRecipeLayout.CRUCIBLE.progressArrow().x(),
                StoneMachineRecipeLayout.CRUCIBLE.progressArrow().y(),
                true,
                StoneMachineRecipeLayout.CRUCIBLE.backgroundWidth(),
                0,
                StoneMachineRecipeLayout.CRUCIBLE.flame().x(),
                StoneMachineRecipeLayout.CRUCIBLE.flame().y()
        ),
        ANVIL(PrimitiveRecipeLayout.ANVIL),
        GRINDING(PrimitiveRecipeLayout.GRINDING),
        PRESSING(PrimitiveRecipeLayout.PRESSING);

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
        final PrimitiveRecipeLayout flow;

        private Layout(PrimitiveRecipeLayout flow) {
            this.texture = flow.texture();
            this.width = flow.backgroundWidth();
            this.backgroundHeight = flow.backgroundHeight();
            this.arrowU = flow.arrowSourceX();
            this.arrowV = flow.arrowSourceY();
            this.arrowX = flow.progressArrow().x();
            this.arrowY = flow.progressArrow().y();
            this.hasFlame = flow.hasFlame();
            this.flameU = flow.flameSourceX();
            this.flameV = flow.flameSourceY();
            this.flameX = flow.hasFlame() ? flow.flame().x() : 0;
            this.flameY = flow.hasFlame() ? flow.flame().y() : 0;
            this.usesEmbeddedArrow = false;
            this.flow = flow;
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
            this.flow = null;
        }
    }
}
