package com.protyvkultury.revivalages.feature.technology.knapping.client;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.knapping.KnappingLayout;
import com.protyvkultury.revivalages.feature.technology.knapping.menu.KnappingMenu;
import com.protyvkultury.revivalages.feature.technology.knapping.network.KnappingCellPayload;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class KnappingScreen extends AbstractContainerScreen<KnappingMenu> {

    private static final ResourceLocation BACKGROUND =
            RevivalAges.id("textures/gui/knapping_screen.png");
    private static final ResourceLocation ROCK =
            ResourceLocation.withDefaultNamespace("textures/block/stone.png");
    private static final ResourceLocation FLINT =
            ResourceLocation.withDefaultNamespace("textures/item/flint.png");
    private static final Map<ResourceLocation, ResourceLocation> SPLITTER_TEXTURES = Map.ofEntries(
            Map.entry(RevivalAges.id("cobblestone_splitter"), blockTexture("cobblestone")),
            Map.entry(RevivalAges.id("granite_splitter"), blockTexture("granite")),
            Map.entry(RevivalAges.id("diorite_splitter"), blockTexture("diorite")),
            Map.entry(RevivalAges.id("andesite_splitter"), blockTexture("andesite")),
            Map.entry(RevivalAges.id("sandstone_splitter"), blockTexture("sandstone")),
            Map.entry(RevivalAges.id("red_sandstone_splitter"), blockTexture("red_sandstone")),
            Map.entry(RevivalAges.id("end_stone_splitter"), blockTexture("end_stone")),
            Map.entry(RevivalAges.id("netherrack_splitter"), blockTexture("netherrack")),
            Map.entry(RevivalAges.id("soul_soil_splitter"), blockTexture("soul_soil"))
    );
    private static final ResourceLocation CLAY =
            RevivalAges.id("textures/gui/knapping/clay.png");
    private static final ResourceLocation CLAY_DISABLED =
            RevivalAges.id("textures/gui/knapping/clay_disabled.png");
    private static final ResourceLocation LEATHER =
            RevivalAges.id("textures/gui/knapping/leather.png");
    private static final ResourceLocation HORN =
            RevivalAges.id("textures/gui/knapping/horn.png");
    private static final ResourceLocation HORN_DISABLED =
            RevivalAges.id("textures/gui/knapping/horn_disabled.png");
    private final List<DustParticle> particles = new ArrayList<>();
    private final boolean[] pendingCells = new boolean[25];

    public KnappingScreen(KnappingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = KnappingLayout.WIDTH;
        imageHeight = KnappingLayout.HEIGHT;
        inventoryLabelY += KnappingLayout.PLAYER_INVENTORY_OFFSET_Y + 2;
        titleLabelY -= 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        for (int index = 0; index < 25; index++) {
            int x = leftPos + KnappingLayout.GRID_X + index % 5 * KnappingLayout.CELL_SIZE;
            int y = topPos + KnappingLayout.GRID_Y + index / 5 * KnappingLayout.CELL_SIZE;
            ResourceLocation texture = cellTexture(menu.cellOn(index));
            if (texture != null) {
                graphics.blit(
                        texture,
                        x,
                        y,
                        0,
                        0,
                        KnappingLayout.CELL_SIZE,
                        KnappingLayout.CELL_SIZE,
                        16,
                        16
                );
            }
        }
        for (DustParticle particle : particles) {
            int alpha = Math.min(255, particle.life * 16);
            int color = alpha << 24 | 0x00D8C09A;
            graphics.fill(
                    leftPos + (int) particle.x,
                    topPos + (int) particle.y,
                    leftPos + (int) particle.x + 2,
                    topPos + (int) particle.y + 2,
                    color
            );
        }
    }

    private ResourceLocation cellTexture(boolean enabled) {
        return textureFor(menu.initialInput(), menu.typeId(), enabled);
    }

    public static ResourceLocation textureFor(ItemStack input, ResourceLocation typeId, boolean enabled) {
        return switch (typeId.getPath()) {
            case "clay" -> enabled ? CLAY : CLAY_DISABLED;
            case "leather" -> enabled ? LEATHER : null;
            case "horn" -> enabled ? HORN : HORN_DISABLED;
            default -> enabled ? materialTexture(input) : null;
        };
    }

    private static ResourceLocation materialTexture(ItemStack input) {
        if (input.is(net.minecraft.world.item.Items.FLINT)) {
            return FLINT;
        }
        return SPLITTER_TEXTURES.getOrDefault(BuiltInRegistries.ITEM.getKey(input.getItem()), ROCK);
    }

    private static ResourceLocation blockTexture(String path) {
        return ResourceLocation.withDefaultNamespace("textures/block/" + path + ".png");
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        Iterator<DustParticle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            DustParticle particle = iterator.next();
            particle.x += particle.velocityX;
            particle.y += particle.velocityY;
            particle.velocityY += 0.04D;
            if (--particle.life <= 0) {
                iterator.remove();
            }
        }
        for (int index = 0; index < pendingCells.length; index++) {
            if (!menu.cellOn(index)) {
                pendingCells[index] = false;
            }
        }
        int accepted;
        while ((accepted = menu.consumeAcceptedCell()) >= 0) {
            playClickFeedback(accepted % 5, accepted / 5);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int localX = (int) mouseX - leftPos - KnappingLayout.GRID_X;
            int localY = (int) mouseY - topPos - KnappingLayout.GRID_Y;
            if (localX >= 0
                    && localY >= 0
                    && localX < KnappingLayout.CELL_SIZE * 5
                    && localY < KnappingLayout.CELL_SIZE * 5) {
                int cell = localX / KnappingLayout.CELL_SIZE + localY / KnappingLayout.CELL_SIZE * 5;
                if (!menu.cellOn(cell) || pendingCells[cell]) {
                    return true;
                }
                pendingCells[cell] = true;
                PacketDistributor.sendToServer(new KnappingCellPayload(menu.containerId, cell));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return mouseClicked(mouseX, mouseY, button) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void playClickFeedback(int column, int row) {
        if (minecraft != null && minecraft.player != null) {
            var registry = minecraft.player.registryAccess()
                    .registryOrThrow(com.protyvkultury.revivalages.feature.technology.knapping.KnappingFeature.KNAPPING_TYPES);
            var type = registry.get(menu.typeId());
            if (type != null) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(type.clickSound().value(), 1.0F));
            }
        }
        spawnClickParticles(column, row);
    }

    private void spawnClickParticles(int column, int row) {
        if (!com.protyvkultury.revivalages.feature.technology.knapping.KnappingConfig.screenParticles()
                || minecraft == null
                || minecraft.player == null
                || minecraft.level == null) {
            return;
        }
        var registry = minecraft.player.registryAccess()
                .registryOrThrow(com.protyvkultury.revivalages.feature.technology.knapping.KnappingFeature.KNAPPING_TYPES);
        var type = registry.get(menu.typeId());
        if (type == null || !type.spawnsParticles()) {
            return;
        }
        double centerX = KnappingLayout.GRID_X + column * KnappingLayout.CELL_SIZE
                + KnappingLayout.CELL_SIZE / 2.0D;
        double centerY = KnappingLayout.GRID_Y + row * KnappingLayout.CELL_SIZE
                + KnappingLayout.CELL_SIZE / 2.0D;
        int amount = minecraft.level.random.nextInt(4);
        for (int index = 0; index < amount; index++) {
            particles.add(new DustParticle(
                    centerX,
                    centerY,
                    (minecraft.level.random.nextDouble() - 0.5D) * 1.2D,
                    -minecraft.level.random.nextDouble() * 0.8D,
                    10 + minecraft.level.random.nextInt(8)
            ));
        }
    }

    private static final class DustParticle {
        private double x;
        private double y;
        private final double velocityX;
        private double velocityY;
        private int life;

        private DustParticle(double x, double y, double velocityX, double velocityY, int life) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.life = life;
        }
    }
}
