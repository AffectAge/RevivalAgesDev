package com.protyvkultury.revivalages.integration.emi;

import com.protyvkultury.revivalages.RevivalAges;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.ButtonWidget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

final class FrameAssemblyEmiWidget extends Widget {

    private static final net.minecraft.resources.ResourceLocation CONTROLS =
            RevivalAges.id("textures/gui/frame_assembly_controls.png");
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final List<EmiIngredient> ingredients;
    private final List<ButtonWidget> buttons;
    private final Map<Integer, List<SlotWidget>> slotsByLayer = new HashMap<>();
    private int layer;

    FrameAssemblyEmiWidget(int x, int y, int width, int height, List<EmiIngredient> ingredients) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        buttons = List.of(
                new ButtonWidget(x + 64, y, 12, 10, 16, 0, CONTROLS, () -> layer < 3, this::up),
                new ButtonWidget(x + 64, y + 10, 12, 10, 28, 0, CONTROLS, () -> layer > 0, this::down)
        );
        for (int gridLayer = 0; gridLayer < 3; gridLayer++) {
            List<SlotWidget> slots = new ArrayList<>();
            int index = gridLayer * 9;
            for (int gridX = 0; gridX < 3; gridX++) {
                for (int z = 0; z < 3; z++) {
                    EmiIngredient ingredient = ingredients.get(index++);
                    if (!ingredient.isEmpty()) {
                        slots.add(new SlotWidget(ingredient, x + gridX * 18, y + 22 + z * 18));
                    }
                }
            }
            slotsByLayer.put(gridLayer, List.copyOf(slots));
        }
    }

    private void up(double mouseX, double mouseY, int button) {
        layer = Math.min(3, layer + 1);
    }

    private void down(double mouseX, double mouseY, int button) {
        layer = Math.max(0, layer - 1);
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        for (ButtonWidget widget : buttons) {
            if (widget.getBounds().contains(mouseX, mouseY)) {
                return widget.mouseClicked(mouseX, mouseY, button);
            }
        }
        if (layer > 0) {
            for (SlotWidget slot : slotsByLayer.getOrDefault(layer - 1, List.of())) {
                if (slot.getBounds().contains(mouseX, mouseY)) {
                    return slot.mouseClicked(mouseX, mouseY, button);
                }
            }
        }
        return false;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        if (layer > 0) {
            for (SlotWidget slot : slotsByLayer.getOrDefault(layer - 1, List.of())) {
                if (slot.getBounds().contains(mouseX, mouseY)) {
                    return slot.getTooltip(mouseX, mouseY);
                }
            }
        }
        return List.of();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (layer == 0) {
            renderIsometric(graphics, delta);
        } else {
            for (SlotWidget slot : slotsByLayer.getOrDefault(layer - 1, List.of())) {
                slot.render(graphics, mouseX, mouseY, delta);
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
        buttons.forEach(button -> button.render(graphics, mouseX, mouseY, delta));
    }

    private void renderIsometric(GuiGraphics graphics, float delta) {
        int index = 0;
        for (int gridY = 0; gridY < 3; gridY++) {
            for (int z = 2; z >= 0; z--) {
                for (int gridX = 0; gridX < 3; gridX++) {
                    EmiIngredient ingredient = ingredients.get(index);
                    if (!ingredient.isEmpty()) {
                        graphics.pose().pushPose();
                        graphics.pose().translate(0.0D, 0.0D, index * 5.0D);
                        ingredient.render(
                                graphics,
                                x + 32 - gridX * 8 - z * 8,
                                y + 74 + gridX * 4 - z * 4 - gridY * 32,
                                delta,
                                EmiIngredient.RENDER_ICON
                        );
                        graphics.pose().popPose();
                    }
                    index++;
                }
            }
        }
    }
}
