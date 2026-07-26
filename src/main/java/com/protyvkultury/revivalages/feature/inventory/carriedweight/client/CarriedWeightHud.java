package com.protyvkultury.revivalages.feature.inventory.carriedweight.client;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.weight.WeightApi;
import com.protyvkultury.revivalages.feature.inventory.carriedweight.WeightFormatting;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;

final class CarriedWeightHud {

    private static final ResourceLocation EMPTY =
            RevivalAges.id("textures/gui/carried_weight/empty.png");
    private static final ResourceLocation OVERLOAD =
            RevivalAges.id("textures/gui/carried_weight/overload.png");
    private static final ResourceLocation STRENGTH =
            RevivalAges.id("textures/gui/carried_weight/strength.png");
    private static final int TEXTURE_SIZE = 16;
    private static final int EDGE_MARGIN = 2;
    private static final int TEXT_GAP = 3;

    private CarriedWeightHud() {
    }

    static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || minecraft.options.hideGui
                || minecraft.getDebugOverlay().showDebugScreen()
                || !WeightApi.enabled()
                || !CarriedWeightClientConfig.SHOW_HUD.get()) {
            return;
        }
        double current = WeightApi.getCurrentWeight(minecraft.player);
        double capacity = WeightApi.getCurrentCapacity(minecraft.player);
        if (capacity <= 0.0D) {
            return;
        }
        CarriedWeightClientConfig.HudStyle style = CarriedWeightClientConfig.HUD_STYLE.get();
        int width = style == CarriedWeightClientConfig.HudStyle.SPRITE
                ? CarriedWeightClientConfig.SPRITE_SIZE.get()
                : CarriedWeightClientConfig.BAR_WIDTH.get();
        int height = style == CarriedWeightClientConfig.HudStyle.SPRITE
                ? CarriedWeightClientConfig.SPRITE_SIZE.get()
                : CarriedWeightClientConfig.BAR_HEIGHT.get();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        Position position = position(screenWidth, screenHeight, width, height, style);
        boolean overloaded = WeightApi.isOverloaded(minecraft.player);

        if (style == CarriedWeightClientConfig.HudStyle.SPRITE) {
            renderSprite(graphics, position.x, position.y, width, height, current, capacity, overloaded);
            if (minecraft.player.hasEffect(MobEffects.DAMAGE_BOOST)
                    || minecraft.player.hasEffect(MobEffects.DIG_SPEED)) {
                blit(graphics, STRENGTH, position.x, position.y, width, height);
            }
        } else {
            renderBar(graphics, position.x, position.y, width, height, current, capacity, overloaded);
        }
        renderText(
                graphics,
                minecraft.font,
                position,
                width,
                height,
                screenWidth,
                screenHeight,
                current,
                capacity
        );
    }

    private static void renderSprite(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            double current,
            double capacity,
            boolean overloaded
    ) {
        blit(graphics, EMPTY, x, y, width, height);
        if (overloaded || current >= capacity) {
            blit(graphics, OVERLOAD, x, y, width, height);
        } else if (current > 0.0D) {
            int fill = Mth.clamp((int) Math.ceil(current / capacity * 12.0D), 1, 12);
            blit(
                    graphics,
                    RevivalAges.id("textures/gui/carried_weight/filled_" + fill + ".png"),
                    x,
                    y,
                    width,
                    height
            );
        }
    }

    private static void renderBar(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            double current,
            double capacity,
            boolean overloaded
    ) {
        double ratio = Mth.clamp(current / capacity, 0.0D, 1.0D);
        int fill = (int) Math.round(width * ratio);
        graphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0x99000000);
        graphics.fill(x, y, x + width, y + height, 0xFF2A2A2A);
        graphics.fill(x, y, x + fill, y + height, color(current, capacity));
        graphics.renderOutline(
                x - 2,
                y - 2,
                width + 4,
                height + 4,
                overloaded ? 0xFFFF3030 : 0xAAFFFFFF
        );
    }

    private static void renderText(
            GuiGraphics graphics,
            Font font,
            Position element,
            int elementWidth,
            int elementHeight,
            int screenWidth,
            int screenHeight,
            double current,
            double capacity
    ) {
        String text = hudText(current, capacity);
        if (text.isEmpty()) {
            return;
        }
        int textWidth = font.width(text);
        int textHeight = font.lineHeight;
        Position textPosition = textPosition(
                element,
                elementWidth,
                elementHeight,
                textWidth,
                textHeight,
                screenWidth,
                screenHeight
        );
        int color = 0xFF000000 | CarriedWeightClientConfig.TEXT_COLOR.get();
        graphics.drawString(
                font,
                text,
                textPosition.x,
                textPosition.y,
                color,
                CarriedWeightClientConfig.TEXT_SHADOW.get()
        );
    }

    private static String hudText(double current, double capacity) {
        double percent = capacity <= 0.0D ? 0.0D : current / capacity * 100.0D;
        return switch (CarriedWeightClientConfig.TEXT_MODE.get()) {
            case NONE -> "";
            case CURRENT -> WeightFormatting.compact(current);
            case MAX -> WeightFormatting.compact(capacity);
            case PERCENT -> String.format(Locale.ROOT, "%.0f%%", percent);
            case REMAINING -> WeightFormatting.compact(Math.max(0.0D, capacity - current));
            case CURRENT_MAX -> WeightFormatting.compact(current) + "/" + WeightFormatting.compact(capacity);
            case CURRENT_MAX_PERCENT -> WeightFormatting.compact(current)
                    + "/"
                    + WeightFormatting.compact(capacity)
                    + " ("
                    + String.format(Locale.ROOT, "%.0f%%", percent)
                    + ")";
        };
    }

    private static Position position(
            int screenWidth,
            int screenHeight,
            int width,
            int height,
            CarriedWeightClientConfig.HudStyle style
    ) {
        int bottomMargin = style == CarriedWeightClientConfig.HudStyle.SPRITE ? 10 : 24;
        return switch (CarriedWeightClientConfig.HUD_POSITION.get()) {
            case TOP_LEFT -> new Position(10, 10);
            case TOP_RIGHT -> new Position(screenWidth - width - 10, 10);
            case CENTER_LEFT -> new Position(10, screenHeight / 2 - height / 2);
            case CENTER_RIGHT -> new Position(
                    screenWidth - width - 10,
                    screenHeight / 2 - height / 2
            );
            case BOTTOM_LEFT -> new Position(10, screenHeight - height - bottomMargin);
            case BOTTOM_RIGHT -> new Position(
                    screenWidth - width - 10,
                    screenHeight - height - bottomMargin
            );
            case HOTBAR_LEFT -> new Position(
                    screenWidth / 2 - 91 - width - 10,
                    screenHeight - height - 4
            );
            case HOTBAR_RIGHT -> new Position(
                    screenWidth / 2 + 101,
                    screenHeight - height - 4
            );
            case CENTER_HOTBAR -> new Position(
                    screenWidth / 2 - width / 2,
                    screenHeight - height - 35
            );
            case CUSTOM -> new Position(
                    (int) (screenWidth * CarriedWeightClientConfig.CUSTOM_X.get()) - width / 2,
                    (int) (screenHeight * CarriedWeightClientConfig.CUSTOM_Y.get()) - height / 2
            );
        };
    }

    private static Position textPosition(
            Position element,
            int elementWidth,
            int elementHeight,
            int textWidth,
            int textHeight,
            int screenWidth,
            int screenHeight
    ) {
        CarriedWeightClientConfig.HudTextPosition requested =
                CarriedWeightClientConfig.TEXT_POSITION.get();
        int x;
        int y;
        switch (requested) {
            case ABOVE -> {
                x = element.x + (elementWidth - textWidth) / 2;
                y = element.y - textHeight - TEXT_GAP;
            }
            case LEFT -> {
                x = element.x - textWidth - 4;
                y = element.y + (elementHeight - textHeight) / 2;
            }
            case RIGHT -> {
                x = element.x + elementWidth + 4;
                y = element.y + (elementHeight - textHeight) / 2;
            }
            case INSIDE -> {
                x = element.x + (elementWidth - textWidth) / 2;
                y = element.y + (elementHeight - textHeight) / 2;
            }
            case CUSTOM -> {
                x = element.x;
                y = element.y;
            }
            case BELOW -> {
                x = element.x + (elementWidth - textWidth) / 2;
                y = element.y + elementHeight + TEXT_GAP;
            }
            default -> throw new IllegalStateException("Unexpected text position");
        }
        x += CarriedWeightClientConfig.TEXT_X_OFFSET.get();
        y += CarriedWeightClientConfig.TEXT_Y_OFFSET.get();
        if (CarriedWeightClientConfig.KEEP_TEXT_ON_SCREEN.get()) {
            x = clamp(x, textWidth, screenWidth);
            y = clamp(y, textHeight, screenHeight);
        }
        return new Position(x, y);
    }

    private static int clamp(int value, int size, int screenSize) {
        int maximum = Math.max(EDGE_MARGIN, screenSize - size - EDGE_MARGIN);
        return Mth.clamp(value, EDGE_MARGIN, maximum);
    }

    private static int color(double current, double capacity) {
        double percent = capacity <= 0.0D ? 0.0D : current / capacity * 100.0D;
        if (percent >= 100.0D) {
            return 0xFFFF3030;
        }
        if (percent >= 80.0D) {
            return 0xFFFFAA00;
        }
        if (percent >= 50.0D) {
            return 0xFFFFFF55;
        }
        return 0xFF55FF55;
    }

    private static void blit(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int width,
            int height
    ) {
        graphics.blit(texture, x, y, width, height, 0.0F, 0.0F, 16, 16, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private record Position(int x, int y) {
    }
}
