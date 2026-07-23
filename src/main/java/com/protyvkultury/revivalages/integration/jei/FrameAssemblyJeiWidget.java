package com.protyvkultury.revivalages.integration.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.protyvkultury.revivalages.RevivalAges;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.helpers.IJeiHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

final class FrameAssemblyJeiWidget implements ISlottedRecipeWidget, IJeiGuiEventListener {

    private static final ResourceLocation CONTROLS =
            RevivalAges.id("textures/gui/frame_assembly_controls.png");
    private final int x;
    private final int y;
    private final List<IRecipeSlotDrawable> ingredients;
    private final IJeiHelpers helpers;
    private final List<LayerButton> buttons;
    private int layer;

    FrameAssemblyJeiWidget(
            int x,
            int y,
            List<IRecipeSlotDrawable> ingredients,
            IJeiHelpers helpers
    ) {
        this.x = x;
        this.y = y;
        this.ingredients = ingredients;
        this.helpers = helpers;
        buttons = List.of(
                new LayerButton(x + 64, y, 16, () -> layer < 3, () -> layer++),
                new LayerButton(x + 64, y + 10, 28, () -> layer > 0, () -> layer--)
        );
    }

    @Override
    public void drawWidget(GuiGraphics graphics, double mouseX, double mouseY) {
        if (layer == 0) {
            drawIsometric(graphics);
        } else {
            drawLayer(graphics);
        }
        buttons.forEach(button -> button.draw(graphics, mouseX, mouseY));
    }

    private void drawIsometric(GuiGraphics graphics) {
        int index = 0;
        for (int gridY = 0; gridY < 3; gridY++) {
            for (int z = 2; z >= 0; z--) {
                for (int gridX = 0; gridX < 3; gridX++) {
                    IRecipeSlotDrawable ingredient = ingredients.get(index);
                    if (!ingredient.isEmpty()) {
                        graphics.pose().pushPose();
                        graphics.pose().translate(
                                x + 32 - gridX * 8 - z * 8,
                                y + 74 + gridX * 4 - z * 4 - gridY * 32,
                                index * 5.0D
                        );
                        ingredient.draw(graphics);
                        graphics.pose().popPose();
                    }
                    index++;
                }
            }
        }
    }

    private void drawLayer(GuiGraphics graphics) {
        int index = (layer - 1) * 9;
        for (int gridX = 0; gridX < 3; gridX++) {
            for (int z = 0; z < 3; z++) {
                IRecipeSlotDrawable ingredient = ingredients.get(index);
                if (!ingredient.isEmpty()) {
                    helpers.getGuiHelper().getSlotDrawable().draw(graphics, x + gridX * 18, y + 22 + z * 18);
                    graphics.pose().pushPose();
                    graphics.pose().translate(x + 1 + gridX * 18, y + 23 + z * 18, index * 5.0D);
                    ingredient.draw(graphics);
                    graphics.pose().popPose();
                }
                index++;
            }
        }
        graphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("gui.revivalages.frame_assembly.layer", layer),
                x + 1,
                y + 1,
                0,
                false
        );
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        slotAt(mouseX, mouseY).map(RecipeSlotUnderMouse::slot).ifPresent(slot -> slot.getTooltip(tooltip));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (LayerButton widget : buttons) {
            if (widget.contains(mouseX, mouseY)) {
                return widget.click();
            }
        }
        return false;
    }

    @Override
    public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
        return slotAt(mouseX, mouseY);
    }

    private Optional<RecipeSlotUnderMouse> slotAt(double mouseX, double mouseY) {
        if (layer == 0) {
            return Optional.empty();
        }
        int index = (layer - 1) * 9;
        for (int gridX = 0; gridX < 3; gridX++) {
            for (int z = 0; z < 3; z++) {
                IRecipeSlotDrawable ingredient = ingredients.get(index++);
                int slotX = x + 1 + gridX * 18;
                int slotY = y + 23 + z * 18;
                if (!ingredient.isEmpty()
                        && mouseX >= slotX && mouseX < slotX + 16
                        && mouseY >= slotY && mouseY < slotY + 16) {
                    return Optional.of(new RecipeSlotUnderMouse(ingredient, slotX, slotY));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public ScreenPosition getPosition() {
        return new ScreenPosition(x, y);
    }

    @Override
    public ScreenRectangle getArea() {
        return new ScreenRectangle(x, y, 94, 97);
    }

    private record LayerButton(
            int x,
            int y,
            int u,
            BooleanSupplier active,
            Runnable action
    ) {

        private boolean contains(double mouseX, double mouseY) {
            return mouseX > x && mouseX < x + 12 && mouseY > y && mouseY < y + 10;
        }

        private boolean click() {
            if (active.getAsBoolean()) {
                action.run();
                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return true;
        }

        private void draw(GuiGraphics graphics, double mouseX, double mouseY) {
            int v = !active.getAsBoolean() ? 20 : contains(mouseX, mouseY) ? 10 : 0;
            RenderSystem.enableDepthTest();
            graphics.blit(CONTROLS, x, y, u, v, 12, 10);
        }
    }
}
