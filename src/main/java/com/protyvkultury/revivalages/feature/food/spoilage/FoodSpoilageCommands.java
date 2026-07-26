package com.protyvkultury.revivalages.feature.food.spoilage;

import com.mojang.brigadier.CommandDispatcher;
import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class FoodSpoilageCommands {

    private FoodSpoilageCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("revivalages")
                .then(Commands.literal("food")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("clock").executes(context -> {
                            long now = FoodFreshnessApi.now();
                            context.getSource().sendSuccess(
                                    () -> Component.literal("Food spoilage clock: " + now + " ticks"),
                                    false
                            );
                            return (int) Math.min(Integer.MAX_VALUE, now);
                        }))
                        .then(Commands.literal("held").executes(context -> {
                            ItemStack held = context.getSource().getPlayerOrException().getMainHandItem();
                            String message = FoodFreshnessApi.profile(held).isEmpty()
                                    ? "Held item is not perishable."
                                    : "Freshness: " + FoodFreshnessApi.remaining(held)
                                            + " ticks; traits: "
                                            + FoodFreshnessApi.state(held).map(state -> state.traits().toString())
                                                    .orElse("uninitialized");
                            context.getSource().sendSuccess(() -> Component.literal(message), false);
                            return 1;
                        }))));
    }
}
