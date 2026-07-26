package com.protyvkultury.revivalages.feature.player.diet.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.protyvkultury.revivalages.api.diet.DietApi;
import com.protyvkultury.revivalages.api.diet.DietContribution;
import com.protyvkultury.revivalages.feature.player.diet.DietSettings;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

public final class DietClientEvents {

    private static final KeyMapping OPEN = new KeyMapping(
            "key.revivalages.open_diet",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            "key.categories.inventory"
    );

    private DietClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(DietClientEvents::registerKeys);
        NeoForge.EVENT_BUS.addListener(DietClientEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(DietClientEvents::onTooltip);
        NeoForge.EVENT_BUS.addListener(DietClientEvents::onScreenInit);
        NeoForge.EVENT_BUS.addListener(DietClientEvents::onLogout);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (OPEN.consumeClick()) {
            if (minecraft.player != null && DietSettings.enabled()) {
                minecraft.setScreen(new DietScreen());
            }
        }
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)
                || !DietClientConfig.SHOW_INVENTORY_BUTTON.get()
                || !DietSettings.enabled()) {
            return;
        }
        event.addListener(net.minecraft.client.gui.components.Button.builder(
                Component.translatable("button.revivalages.diet"),
                button -> Minecraft.getInstance().setScreen(new DietScreen())
        ).bounds(screen.getGuiLeft() + 126, screen.getGuiTop() + 61, 20, 20).build());
    }

    private static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !DietClientConfig.SHOW_TOOLTIPS.get()) {
            return;
        }
        DietContribution contribution = DietApi.contribution(stack).orElse(null);
        if (contribution == null || !DietSettings.enabled()) {
            return;
        }
        var food = stack.getFoodProperties(Minecraft.getInstance().player);
        double nutrition;
        if (food != null) {
            nutrition = food.nutrition();
        } else if (stack.is(net.minecraft.world.item.Items.MILK_BUCKET)) {
            nutrition = DietSettings.milkNutrition();
        } else if (stack.is(net.minecraft.world.item.Items.CAKE)) {
            nutrition = DietSettings.cakeSliceNutrition();
        } else {
            nutrition = 0.0D;
        }
        int count = contribution.groups().size();
        double reduction = Math.max(0.0D, 1.0D - DietSettings.multiGroupReduction() * Math.max(0, count - 1));
        String groups = contribution.groups().entrySet().stream()
                .map(entry -> formatGroup(entry, nutrition * DietSettings.nutritionMultiplier()
                        * entry.getValue() * reduction))
                .sorted()
                .collect(java.util.stream.Collectors.joining(", "));
        event.getToolTip().add(Component.translatable(
                "tooltip.revivalages.diet",
                groups
        ).withStyle(ChatFormatting.GREEN));
    }

    private static String formatGroup(Map.Entry<ResourceLocation, Double> entry, double gain) {
        String name = Component.translatable(
                "diet_group." + entry.getKey().getNamespace() + "." + entry.getKey().getPath()
        ).getString();
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            return name + " +" + String.format("%.2f", gain);
        }
        return name;
    }

    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        DietSettings.clearRemote();
    }
}
