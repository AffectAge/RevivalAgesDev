package com.protyvkultury.revivalages.feature.food.spoilage.client;

import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
import com.protyvkultury.revivalages.api.food.FoodState;
import com.protyvkultury.revivalages.feature.food.spoilage.FoodSpoilageSettings;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class FoodSpoilageClientEvents {

    private FoodSpoilageClientEvents() {
    }

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(FoodSpoilageClientEvents::onTooltip);
        NeoForge.EVENT_BUS.addListener(FoodSpoilageClientEvents::onLogout);
    }

    private static void onTooltip(ItemTooltipEvent event) {
        if (FoodSpoilageClientConfig.TOOLTIP_MODE.get()
                == FoodSpoilageClientConfig.TooltipMode.OFF) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (FoodFreshnessApi.profile(stack).isEmpty()) {
            return;
        }
        FoodState state = FoodFreshnessApi.state(stack).orElse(null);
        if (state == null) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.revivalages.food_fresh"
            ).withStyle(ChatFormatting.GREEN));
            return;
        }
        long remaining = FoodFreshnessApi.remaining(stack);
        event.getToolTip().add(Component.translatable(
                remaining <= 0L
                        ? "tooltip.revivalages.food_spoiled"
                        : "tooltip.revivalages.food_time_left",
                formatTicks(remaining)
        ).withStyle(remaining <= 0L ? ChatFormatting.RED : ChatFormatting.YELLOW));
        if (!state.traits().isEmpty()) {
            String traits = state.traits().stream()
                    .map(id -> Component.translatable(
                            "food_trait." + id.getNamespace() + "." + id.getPath()
                    ).getString())
                    .sorted()
                    .collect(java.util.stream.Collectors.joining(", "));
            event.getToolTip().add(Component.translatable(
                    "tooltip.revivalages.food_traits",
                    traits
            ).withStyle(ChatFormatting.AQUA));
        }
    }

    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        FoodSpoilageSettings.clearRemote();
    }

    private static String formatTicks(long ticks) {
        long seconds = Math.max(0L, ticks / 20L);
        long days = TimeUnit.SECONDS.toDays(seconds);
        seconds -= TimeUnit.DAYS.toSeconds(days);
        long hours = TimeUnit.SECONDS.toHours(seconds);
        seconds -= TimeUnit.HOURS.toSeconds(hours);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        if (days > 0L) {
            return days + "d " + hours + "h";
        }
        if (hours > 0L) {
            return hours + "h " + minutes + "m";
        }
        return Math.max(1L, minutes) + "m";
    }
}
