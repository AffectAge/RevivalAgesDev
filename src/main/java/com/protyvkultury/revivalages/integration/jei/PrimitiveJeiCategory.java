package com.protyvkultury.revivalages.integration.jei;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.core.process.ProcessRuleType;
import com.protyvkultury.revivalages.core.process.ProcessRuleView;
import com.protyvkultury.revivalages.core.process.ProcessRulePresentation;
import com.protyvkultury.revivalages.core.process.ProcessRuleLayout;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveFluidSlotGeometry;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeView;
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
            case CAMPFIRE, STONE_OVEN:
                {
                    PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{0, 0}});
                    PrimitiveJeiCategory.addItemOutputs(builder, recipe, new int[][] {{60, 10}});
                    break;
                }
            case CHOPPING, ANVIL:
                {
                    PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{0, 17}});
                    PrimitiveJeiCategory.addItemOutputs(builder, recipe, new int[][] {{60, 18}, {83, 18}});
                    break;
                }
            case GRINDING:
                PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{34, 27}});
                PrimitiveJeiCategory.addItemOutputs(builder, recipe, new int[][] {{90, 27}, {90, 50}});
                break;
            case PRESSING:
                PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{34, 32}});
                PrimitiveJeiCategory.addItemOutputs(builder, recipe, new int[][] {{90, 32}});
                PrimitiveJeiCategory.addFluid(
                        builder,
                        RecipeIngredientRole.OUTPUT,
                        recipe.fluidOutput(),
                        PrimitiveFluidSlotGeometry.PRESSING_OUTPUT);
                break;
            case PIT_KILN, PIT_BURN:
                {
                    PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{0, 22}});
                    addChanceOutputs(builder, recipe, 60, 18, 83, 22);
                    break;
                }
            case BARREL:
                {
                    PrimitiveJeiCategory.addItemInputs(
                            builder, recipe, new int[][] {{0, 0}, {19, 0}, {0, 19}, {19, 19}});
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
                    PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{0, 0}});
                    PrimitiveJeiCategory.addFluid(
                            builder,
                            RecipeIngredientRole.INPUT,
                            recipe.fluidInput(),
                            PrimitiveFluidSlotGeometry.SOAKING_POT_INPUT);
                    PrimitiveJeiCategory.addItemOutputs(builder, recipe, new int[][] {{60, 19}});
                    break;
                }
            case TANNING_RACK:
                {
                    PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{1, 9}});
                    PrimitiveJeiCategory.addItemOutputs(builder, recipe, new int[][] {{53, 9}, {77, 9}});
                    break;
                }
            case STONE_SAWMILL:
                PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{0, 0}, {0, 19}});
                addChanceOutputs(builder, recipe, 60, 16, 83, 20);
                break;
            case STONE_KILN:
                PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{0, 0}});
                addChanceOutputs(builder, recipe, 60, 10, 83, 14);
                break;
            case STONE_CRUCIBLE:
                PrimitiveJeiCategory.addItemInputs(builder, recipe, new int[][] {{0, 0}});
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
            IRecipeLayoutBuilder builder, PrimitiveRecipeView recipe, int resultX, int resultY, int chanceX, int chanceY) {
        if (recipe.itemOutputs().isEmpty()) {
            return;
        }
        builder.addOutputSlot(resultX, resultY).addItemStack(recipe.itemOutputs().getFirst());
        if (recipe.itemOutputs().size() > 1) {
            builder.addOutputSlot(chanceX, chanceY).addItemStacks(recipe.itemOutputs().subList(1, recipe.itemOutputs().size()));
        }
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
            graphics.blit(ProcessRulePresentation.ATLAS, ruleLayout.x(index), ruleLayout.y(y, index), presentation.u(), presentation.v(), 16, 16,
                    ProcessRulePresentation.ATLAS_WIDTH, ProcessRulePresentation.ATLAS_HEIGHT);
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
        CAMPFIRE("campfire", 82, 33, 82, 14, 24, 17, 24, 10, true, 82, 0, 1, 19),
        CHOPPING("chopping", 82, 40, 82, 0, 24, 17, 24, 18, false, 0, 0, 0, 0),
        PIT_KILN("pit_kiln", 101, 54, 101, 14, 24, 17, 24, 18, true, 101, 0, 1, 7),
        PIT_BURN("pit_kiln", 101, 54, 101, 14, 24, 17, 24, 18, true, 101, 0, 1, 6),
        BARREL("barrel", 97, 51, 101, 0, 24, 17, 42, 19, false, 0, 0, 0, 0),
        SOAKING_POT("soaking_pot", 82, 56, 82, 0, 24, 17, 24, 19, false, 0, 0, 0, 0),
        TANNING_RACK("tanning_rack", 95, 45, 119, 0, 24, 17, 24, 10, false, 0, 0, 0, 0),
        STONE_SAWMILL("stone_sawmill", 101, 38, 101, 0, 24, 17, 24, 16, false, 0, 0, 0, 0),
        STONE_OVEN("stone_oven", 82, 33, 82, 14, 24, 17, 24, 10, true, 82, 0, 1, 19),
        STONE_KILN("stone_kiln", 101, 46, 101, 14, 24, 17, 24, 10, true, 101, 0, 1, 19),
        STONE_CRUCIBLE("stone_crucible", 82, 33, 82, 14, 24, 17, 24, 10, true, 82, 0, 1, 19),
        ANVIL("anvil", 82, 40, 82, 0, 24, 17, 24, 18, false, 0, 0, 0, 0),
        GRINDING("animal_power_grinding", 146, 85, 0, 0, 0, 0, 0, 0, false, 0, 0, 0, 0, true),
        PRESSING("animal_power_pressing", 146, 74, 0, 0, 0, 0, 0, 0, false, 0, 0, 0, 0, true);

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
        }
    }
}
