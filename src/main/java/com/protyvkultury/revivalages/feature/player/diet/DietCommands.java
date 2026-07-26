package com.protyvkultury.revivalages.feature.player.diet;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.protyvkultury.revivalages.api.diet.DietApi;
import java.util.Comparator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

final class DietCommands {

    private DietCommands() {
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var get = Commands.literal("get")
                .executes(context -> show(context.getSource(), context.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> show(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player")
                        )));
        var set = Commands.literal("set")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("group", StringArgumentType.word())
                                .then(Commands.argument(
                                        "value",
                                        DoubleArgumentType.doubleArg(0.0D, 100.0D)
                                ).executes(context -> set(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player"),
                                        StringArgumentType.getString(context, "group"),
                                        DoubleArgumentType.getDouble(context, "value")
                                )))));
        dispatcher.register(Commands.literal("revivalages")
                .then(Commands.literal("diet").then(get).then(set)));
    }

    private static int show(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(
                () -> Component.literal(player.getScoreboardName() + " Diet:"),
                false
        );
        DietApi.values(player).entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                .forEach(entry -> source.sendSuccess(
                        () -> Component.literal("  " + entry.getKey() + " = "
                                + String.format(java.util.Locale.ROOT, "%.2f", entry.getValue())),
                        false
                ));
        return 1;
    }

    private static int set(CommandSourceStack source, ServerPlayer player, String idText, double value) {
        ResourceLocation id = ResourceLocation.tryParse(idText);
        if (id == null) {
            source.sendFailure(Component.literal("Invalid diet group ID."));
            return 0;
        }
        DietApi.set(player, id, value);
        source.sendSuccess(
                () -> Component.literal("Set " + id + " to " + value + " for " + player.getScoreboardName()),
                true
        );
        return 1;
    }
}
