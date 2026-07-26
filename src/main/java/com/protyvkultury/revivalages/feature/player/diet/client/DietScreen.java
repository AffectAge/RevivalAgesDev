package com.protyvkultury.revivalages.feature.player.diet.client;

import com.protyvkultury.revivalages.feature.player.diet.DietFeature;
import com.protyvkultury.revivalages.feature.player.diet.DietState;
import com.protyvkultury.revivalages.api.diet.DietGroup;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class DietScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private int scroll;

    public DietScreen() {
        super(Component.translatable("screen.revivalages.diet"));
    }

    private void renderPanel(GuiGraphics graphics) {
        int left = width / 2 - 110;
        int top = height / 2 - 82;
        graphics.fill(left, top, left + 220, top + 164, 0xE0101010);
        graphics.fill(left + 1, top + 1, left + 219, top + 163, 0xE0282828);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderPanel(graphics);
        int left = width / 2 - 110;
        int top = height / 2 - 82;
        graphics.drawCenteredString(font, title, width / 2, top + 8, 0xFFFFFF);

        if (minecraft != null && minecraft.player != null) {
            DietState state = DietFeature.state(minecraft.player);
            List<Map.Entry<ResourceLocation, Double>> values = new ArrayList<>(state.values().entrySet());
            values.sort(Comparator.comparing(entry -> entry.getKey().toString()));
            int visibleRows = 5;
            scroll = Math.clamp(scroll, 0, Math.max(0, values.size() - visibleRows));
            for (int row = 0; row < visibleRows && scroll + row < values.size(); row++) {
                Map.Entry<ResourceLocation, Double> entry = values.get(scroll + row);
                DietGroup group = minecraft.player.registryAccess()
                        .registryOrThrow(DietFeature.DIET_GROUPS)
                        .get(entry.getKey());
                renderRow(graphics, entry.getKey(), entry.getValue(), group,
                        left + 14, top + 28 + row * ROW_HEIGHT);
            }
        }
    }

    private void renderRow(
            GuiGraphics graphics,
            ResourceLocation id,
            double value,
            DietGroup group,
            int x,
            int y
    ) {
        String name = Component.translatable("diet_group." + id.getNamespace() + "." + id.getPath()).getString();
        if (group != null) {
            graphics.renderItem(group.icon(), x, y - 2);
        }
        graphics.drawString(font, name, x + 20, y, 0xE0E0E0, false);
        graphics.drawString(font, String.format("%.1f", value), x + 170, y, 0xFFFFFF, false);
        graphics.fill(x + 20, y + 11, x + 190, y + 19, 0xFF111111);
        int filled = Math.clamp((int) Math.round(value * 1.7D), 0, 170);
        int color = group == null ? color(value) : 0xFF000000 | group.color();
        graphics.fill(x + 20, y + 11, x + 20 + filled, y + 19, color);
    }

    private static int color(double value) {
        if (value < 20.0D) {
            return 0xFFC84135;
        }
        if (value < 50.0D) {
            return 0xFFD79A35;
        }
        return 0xFF54A866;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, scroll + (scrollY < 0.0D ? 1 : -1));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
