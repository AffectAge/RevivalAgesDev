package com.protyvkultury.revivalages.feature.inventory.carriedweight.client;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.weight.WeightApi;
import com.protyvkultury.revivalages.api.weight.WeightResult;
import com.protyvkultury.revivalages.feature.inventory.carriedweight.CarriedWeightSettings;
import com.protyvkultury.revivalages.feature.inventory.carriedweight.WeightFormatting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class CarriedWeightClientEvents {

    private CarriedWeightClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(CarriedWeightClientEvents::registerGuiLayers);
        NeoForge.EVENT_BUS.addListener(CarriedWeightClientEvents::onTooltip);
        NeoForge.EVENT_BUS.addListener(CarriedWeightClientEvents::onLoggingOut);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                RevivalAges.id("carried_weight"),
                CarriedWeightHud::render
        );
    }

    private static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Minecraft minecraft = Minecraft.getInstance();
        if (stack.isEmpty()
                || minecraft.player == null
                || !WeightApi.enabled()
                || !CarriedWeightClientConfig.SHOW_TOOLTIPS.get()) {
            return;
        }
        WeightResult unit = WeightApi.getWeight(stack, minecraft.player);
        WeightResult total = unit.multiply(stack.getCount());
        boolean exact = Screen.hasShiftDown();
        String unitText = format(unit.weight(), exact);
        event.getToolTip().add(Component.translatable(
                "tooltip.revivalages.carried_weight",
                unitText
        ).withStyle(ChatFormatting.GRAY));
        if (unit.contentsWeight() > 0.0D) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.revivalages.carried_weight_contents",
                    format(unit.contentsWeight(), exact)
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (stack.getCount() > 1) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.revivalages.carried_weight_total",
                    format(total.weight(), exact)
            ).withStyle(ChatFormatting.GRAY));
        }
        int pockets = WeightApi.getPockets(stack, minecraft.player).orElse(0);
        if (pockets > 0) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.revivalages.carried_weight_pockets",
                    pockets
            ).withStyle(ChatFormatting.BLUE));
        }
        if (!exact && (stack.getCount() > 1 || unit.contentsWeight() >= 1_000.0D)) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.revivalages.carried_weight_shift"
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        CarriedWeightSettings.clearRemote();
    }

    private static String format(double value, boolean exact) {
        String number = exact ? WeightFormatting.exact(value) : WeightFormatting.compact(value);
        return Component.translatable("unit.revivalages.grams", number).getString();
    }
}
