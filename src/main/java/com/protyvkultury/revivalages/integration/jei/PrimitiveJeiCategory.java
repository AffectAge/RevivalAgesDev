package com.protyvkultury.revivalages.integration.jei;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.core.process.ProcessRuleType;
import com.protyvkultury.revivalages.core.process.ProcessRuleView;
import com.protyvkultury.revivalages.core.process.ProcessRulePresentation;
import com.protyvkultury.revivalages.core.process.ProcessRuleLayout;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveFluidSlotGeometry;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeLayout;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeView;
import com.protyvkultury.revivalages.feature.technology.stonemachine.view.StoneMachineRecipeLayout;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
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
    private final IDrawable flame;

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
        this.arrow = layout.usesEmbeddedArrow
                ? null
                : helper.createAnimatedDrawable(
                        helper.createDrawable(
                                texture, layout.arrowU, layout.arrowV, layout.arrowWidth, layout.arrowHeight),
                        200,
                        IDrawableAnimated.StartDirection.LEFT,
                        false);
        this.flame = layout.hasFlame
                ? helper.createAnimatedDrawable(
                        helper.createDrawable(texture, layout.flameU, layout.flameV, 14, 14),
                        300,
                        IDrawableAnimated.StartDirection.TOP,
                        true)
                : null;
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
        return this.layout.backgroundHeight
                + 20
                + maximumRuleHeight()
                + 24;
    }

    public void setRecipe(
            IRecipeLayoutBuilder builder, PrimitiveRecipeView recipe, IFocusGroup focuses) {
        addToolRequirements(builder, recipe);
        switch (this.layout) {
            case CAMPFIRE:
                {
                    addItemInputs(builder, recipe, this.layout.flow.itemInputs());
                    addItemOutputs(builder, recipe, this.layout.flow.itemOutputs());
                    break;
                }
            case STONE_OVEN:
                addStoneMachineFlow(builder, recipe, StoneMachineRecipeLayout.OVEN);
                break;
            case CHOPPING, ANVIL:
                {
                    addItemInputs(builder, recipe, this.layout.flow.itemInputs());
                    addItemOutputs(builder, recipe, this.layout.flow.itemOutputs());
                    break;
                }
            case GRINDING:
                addItemInputs(builder, recipe, this.layout.flow.itemInputs());
                addItemOutputs(builder, recipe, this.layout.flow.itemOutputs());
                break;
            case PRESSING:
                addItemInputs(builder, recipe, this.layout.flow.itemInputs());
                addItemOutputs(builder, recipe, this.layout.flow.itemOutputs());
                PrimitiveJeiCategory.addFluid(
                        builder,
                        RecipeIngredientRole.OUTPUT,
                        recipe.fluidOutput(),
                        PrimitiveFluidSlotGeometry.PRESSING_OUTPUT);
                break;
            case PIT_KILN, PIT_BURN:
                {
                    addItemInputs(builder, recipe, this.layout.flow.itemInputs());
                    addChanceOutputs(builder, recipe, this.layout.flow.itemOutputs());
                    break;
                }
            case BARREL:
                {
                    addItemInputs(builder, recipe, this.layout.flow.itemInputs());
                    PrimitiveJeiCategory.addFluid(
                            builder,
                            RecipeIngredientRole.INPUT,
                            recipe.fluidInput(),
                            PrimitiveFluidSlotGeometry.BARREL_INPUT);
                    PrimitiveJeiCategory.addFluid(
                            builder,
                            RecipeIngredientRole.OUTPUT,
                            recipe.fluidOutput(),
                            PrimitiveFluidSlotGeometry.BARREL_OUTPUT);
                    break;
                }
            case SOAKING_POT:
                {
                    addItemInputs(builder, recipe, this.layout.flow.itemInputs());
                    PrimitiveJeiCategory.addFluid(
                            builder,
                            RecipeIngredientRole.INPUT,
                            recipe.fluidInput(),
                            PrimitiveFluidSlotGeometry.SOAKING_POT_INPUT);
                    addItemOutputs(builder, recipe, this.layout.flow.itemOutputs());
                    break;
                }
            case TANNING_RACK:
                {
                    addItemInputs(builder, recipe, this.layout.flow.itemInputs());
                    addChanceOutputs(builder, recipe, this.layout.flow.itemOutputs());
                    break;
                }
            case STONE_SAWMILL:
                addItemInputs(builder, recipe, this.layout.flow.itemInputs());
                addChanceOutputs(builder, recipe, this.layout.flow.itemOutputs());
                break;
            case STONE_KILN:
                addStoneMachineFlow(builder, recipe, StoneMachineRecipeLayout.KILN);
                break;
            case STONE_CRUCIBLE:
                PrimitiveJeiCategory.addItemInputs(
                        builder,
                        recipe,
                        new int[][] {{StoneMachineRecipeLayout.CRUCIBLE.input().x(),
                                StoneMachineRecipeLayout.CRUCIBLE.input().y()}}
                );
                PrimitiveJeiCategory.addFluid(
                        builder,
                        RecipeIngredientRole.OUTPUT,
                        recipe.fluidOutput(),
                        PrimitiveFluidSlotGeometry.STONE_CRUCIBLE_OUTPUT);
                break;
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

    private static void addItemInputs(
            IRecipeLayoutBuilder builder,
            PrimitiveRecipeView recipe,
            List<PrimitiveRecipeLayout.Position> positions) {
        for (int index = 0; index < recipe.itemInputs().size() && index < positions.size(); ++index) {
            PrimitiveRecipeLayout.Position position = positions.get(index);
            builder.addInputSlot(position.x(), position.y()).addIngredients(recipe.itemInputs().get(index));
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

    private static void addItemOutputs(
            IRecipeLayoutBuilder builder,
            PrimitiveRecipeView recipe,
            List<PrimitiveRecipeLayout.Position> positions) {
        for (int index = 0; index < recipe.itemOutputs().size() && index < positions.size(); ++index) {
            PrimitiveRecipeLayout.Position position = positions.get(index);
            builder.addOutputSlot(position.x(), position.y()).addItemStack(recipe.itemOutputs().get(index));
        }
    }

    private static void addFluid(
            IRecipeLayoutBuilder builder,
            RecipeIngredientRole role,
            FluidStack fluid,
            PrimitiveFluidSlotGeometry geometry) {
        if (fluid.isEmpty()) {
            return;
        }
        builder
                .addSlot(role, geometry.contentX(), geometry.contentY())
                .setFluidRenderer(
                        (long) fluid.getAmount(),
                        false,
                        geometry.contentWidth(),
                        geometry.contentHeight())
                .addFluidStack(fluid.getFluid(), (long) fluid.getAmount());
    }

    public void draw(
            PrimitiveRecipeView recipe,
            IRecipeSlotsView slots,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        this.background.draw(graphics, 0, 0);
        if (this.flame != null) {
            this.flame.draw(graphics, this.layout.flameX, this.layout.flameY);
        }
        if (this.arrow != null) {
            this.arrow.draw(graphics, this.layout.arrowX, this.layout.arrowY);
        }
        drawCustomConditions(graphics, recipe);
        int textY = conditionY(recipe)
                + ProcessRuleLayout.of(this.layout.width, recipe.processRules().size()).height()
                + 2;
        if (recipe.processingTime() > 0) {
            drawCentered(graphics, processingTimeText(recipe), textY);
            textY += 10;
        }
        Component detail = recipe.detail();
        if (!detail.getString().isEmpty()) {
            drawCentered(graphics, detail, textY);
        }
    }

    private static void addChanceOutputs(
            IRecipeLayoutBuilder builder,
            PrimitiveRecipeView recipe,
            int resultX,
            int resultY,
            int chanceX,
            int chanceY) {
        if (recipe.itemOutputs().isEmpty()) {
            return;
        }
        builder.addOutputSlot(resultX, resultY).addItemStack(recipe.itemOutputs().getFirst());
        if (recipe.itemOutputs().size() > 1) {
            builder
                    .addOutputSlot(chanceX, chanceY)
                    .addItemStacks(recipe.itemOutputs().subList(1, recipe.itemOutputs().size()));
        }
    }

    private static void addChanceOutputs(
            IRecipeLayoutBuilder builder,
            PrimitiveRecipeView recipe,
            List<PrimitiveRecipeLayout.Position> positions) {
        if (positions.size() < 2) {
            throw new IllegalArgumentException("Chance-result layouts need two output positions");
        }
        PrimitiveRecipeLayout.Position result = positions.getFirst();
        PrimitiveRecipeLayout.Position chance = positions.get(1);
        addChanceOutputs(builder, recipe, result.x(), result.y(), chance.x(), chance.y());
    }

    private static void addStoneMachineFlow(
            IRecipeLayoutBuilder builder,
            PrimitiveRecipeView recipe,
            StoneMachineRecipeLayout layout
    ) {
        PrimitiveJeiCategory.addItemInputs(
                builder,
                recipe,
                new int[][] {{layout.input().x(), layout.input().y()}}
        );
        if (layout.hasSecondaryOutput()) {
            StoneMachineRecipeLayout.Position secondaryOutput = layout.secondaryOutput();
            addChanceOutputs(
                    builder,
                    recipe,
                    layout.output().x(),
                    layout.output().y(),
                    secondaryOutput.x(),
                    secondaryOutput.y()
            );
            return;
        }
        PrimitiveJeiCategory.addItemOutputs(
                builder,
                recipe,
                new int[][] {{layout.output().x(), layout.output().y()}}
        );
    }

    private void addToolRequirements(IRecipeLayoutBuilder builder, PrimitiveRecipeView recipe) {
        int x = Math.max(0, (this.layout.width - recipe.toolRequirements().size() * 18 + 2) / 2);
        int y = this.layout.backgroundHeight + 2;
        for (int index = 0; index < recipe.toolRequirements().size(); index++) {
            builder.addSlot(RecipeIngredientRole.CATALYST, x + index * 18, y)
                    .setStandardSlotBackground()
                    .addIngredients(recipe.toolRequirements().get(index).ingredient());
        }
    }

    @Override
    public void getTooltip(
            ITooltipBuilder tooltip,
            PrimitiveRecipeView recipe,
            IRecipeSlotsView slots,
            double mouseX,
            double mouseY
    ) {
        int conditionY = conditionY(recipe);
        for (int index = 0; index < recipe.toolRequirements().size(); index++) {
            int x = Math.max(0, (this.layout.width - recipe.toolRequirements().size() * 18 + 2) / 2) + index * 18;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= this.layout.backgroundHeight + 2
                    && mouseY < this.layout.backgroundHeight + 18) {
                recipe.toolRequirements().get(index).tooltip().forEach(tooltip::add);
            }
        }
        ProcessRuleLayout ruleLayout = ProcessRuleLayout.of(this.layout.width, recipe.processRules().size());
        for (int index = 0; index < recipe.processRules().size(); index++) {
            int iconX = ruleLayout.x(index);
            int iconY = ruleLayout.y(conditionY, index);
            if (mouseX >= iconX && mouseX < iconX + 16 && mouseY >= iconY && mouseY < iconY + 16) {
                appendRuleTooltip(tooltip, recipe.processRules().get(index));
            }
        }
    }

    private void drawCustomConditions(GuiGraphics graphics, PrimitiveRecipeView recipe) {
        int y = conditionY(recipe);
        ProcessRuleLayout ruleLayout = ProcessRuleLayout.of(this.layout.width, recipe.processRules().size());
        for (int index = 0; index < recipe.processRules().size(); index++) {
            ProcessRulePresentation presentation = recipe.processRules().get(index).presentation();
            graphics.blit(
                    ProcessRulePresentation.ATLAS,
                    ruleLayout.x(index),
                    ruleLayout.y(y, index),
                    presentation.u(),
                    presentation.v(),
                    16,
                    16,
                    ProcessRulePresentation.ATLAS_WIDTH,
                    ProcessRulePresentation.ATLAS_HEIGHT);
        }
    }

    private int conditionY(PrimitiveRecipeView recipe) {
        return this.layout.backgroundHeight + 2 + (recipe.toolRequirements().isEmpty() ? 0 : 20);
    }

    private int maximumRuleHeight() {
        return ProcessRuleLayout.of(this.layout.width, ProcessRuleType.values().length).height();
    }

    private static void appendRuleTooltip(ITooltipBuilder tooltip, ProcessRuleView rule) {
        ProcessRulePresentation.viewerTooltip(rule).forEach(tooltip::add);
    }

    private static Component processingTimeText(PrimitiveRecipeView recipe) {
        return Component.translatable(
                "gui.revivalages.recipe.time",
                String.format(Locale.ROOT, "%.1f", (double) recipe.processingTime() / 20.0));
    }

    private void drawCentered(GuiGraphics graphics, Component text, int y) {
        drawCenteredAt(graphics, text, this.getWidth() / 2, y);
    }

    private static void drawCenteredAt(GuiGraphics graphics, Component text, int centerX, int y) {
        int x = centerX - Minecraft.getInstance().font.width((FormattedText) text) / 2;
        graphics.drawString(
                Minecraft.getInstance().font,
                text,
                Math.max(0, x),
                y,
                -8355712,
                false);
    }

    public ResourceLocation getRegistryName(PrimitiveRecipeView recipe) {
        return recipe.id();
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
                24,
                17,
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
                24,
                17,
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
                24,
                17,
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
        final int arrowWidth;
        final int arrowHeight;
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
            this.arrowWidth = 24;
            this.arrowHeight = 17;
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
                int arrowWidth,
                int arrowHeight,
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
                    arrowWidth,
                    arrowHeight,
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
                int arrowWidth,
                int arrowHeight,
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
            this.arrowWidth = arrowWidth;
            this.arrowHeight = arrowHeight;
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
