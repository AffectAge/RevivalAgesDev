package com.protyvkultury.revivalages.feature.inventory.carriedweight;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.protyvkultury.revivalages.api.weight.WeightApi;
import com.protyvkultury.revivalages.api.weight.WeightResult;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;

final class CarriedWeightCommands {

    private CarriedWeightCommands() {
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("revivalages")
                .then(Commands.literal("weight")
                        .requires(source -> source.hasPermission(4))
                        .then(Commands.literal("get")
                                .then(Commands.literal("current")
                                        .executes(context -> current(context, context.getSource().getPlayerOrException()))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> current(
                                                        context,
                                                        EntityArgument.getPlayer(context, "player")
                                                ))))
                                .then(Commands.literal("capacity")
                                        .executes(context -> capacity(
                                                context,
                                                context.getSource().getPlayerOrException()
                                        ))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> capacity(
                                                        context,
                                                        EntityArgument.getPlayer(context, "player")
                                                ))))
                                .then(Commands.literal("base")
                                        .executes(CarriedWeightCommands::base))
                                .then(Commands.literal("bonus")
                                        .executes(context -> bonus(
                                                context,
                                                context.getSource().getPlayerOrException()
                                        ))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> bonus(
                                                        context,
                                                        EntityArgument.getPlayer(context, "player")
                                                )))))
                        .then(Commands.literal("set")
                                .then(Commands.literal("base")
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(1.0D))
                                                .executes(CarriedWeightCommands::setBase)))
                                .then(Commands.literal("bonus")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument(
                                                        "value",
                                                        DoubleArgumentType.doubleArg(0.0D)
                                                ).executes(CarriedWeightCommands::setBonus)))))
                        .then(Commands.literal("inspect")
                                .executes(CarriedWeightCommands::inspectHeld)
                                .then(Commands.literal("armor")
                                        .executes(CarriedWeightCommands::inspectArmor)))));
    }

    private static int current(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        context.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.revivalages.weight.current",
                        player.getDisplayName(),
                        WeightFormatting.exact(WeightApi.getCurrentWeight(player))
                ),
                false
        );
        return 1;
    }

    private static int capacity(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        context.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.revivalages.weight.capacity",
                        player.getDisplayName(),
                        WeightFormatting.exact(WeightApi.getCurrentCapacity(player))
                ),
                false
        );
        return 1;
    }

    private static int base(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.revivalages.weight.base",
                        WeightFormatting.exact(CarriedWeightSettings.snapshot().baseCapacity())
                ),
                false
        );
        return 1;
    }

    private static int bonus(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        context.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.revivalages.weight.bonus",
                        player.getDisplayName(),
                        WeightFormatting.exact(CarriedWeightFeature.capacityBonus(player))
                ),
                false
        );
        return 1;
    }

    private static int setBase(CommandContext<CommandSourceStack> context) {
        double value = DoubleArgumentType.getDouble(context, "value");
        CarriedWeightConfig.BASE_CAPACITY.set(value);
        CarriedWeightFeature.reloadSettings(context.getSource().getServer());
        context.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.revivalages.weight.set_base",
                        WeightFormatting.exact(value)
                ),
                true
        );
        return 1;
    }

    private static int setBonus(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        double value = DoubleArgumentType.getDouble(context, "value");
        AttributeInstance attribute = player.getAttribute(CarriedWeightFeature.CARRY_CAPACITY_BONUS);
        if (attribute == null) {
            context.getSource().sendFailure(Component.translatable(
                    "command.revivalages.weight.missing_attribute",
                    player.getDisplayName()
            ));
            return 0;
        }
        attribute.setBaseValue(value);
        CarriedWeightFeature.updatePlayerNow(player);
        context.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.revivalages.weight.set_bonus",
                        player.getDisplayName(),
                        WeightFormatting.exact(value)
                ),
                true
        );
        return 1;
    }

    private static int inspectHeld(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        WeightResult result = WeightApi.getWeight(stack, player);
        context.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.revivalages.weight.inspect",
                        stack.getHoverName(),
                        WeightFormatting.exact(result.weight()),
                        WeightFormatting.exact(result.contentsWeight())
                ),
                false
        );
        return 1;
    }

    private static int inspectArmor(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int pockets = 0;
        for (ItemStack stack : player.getInventory().armor) {
            pockets += WeightApi.getPockets(stack, player).orElse(0);
        }
        int finalPockets = pockets;
        context.getSource().sendSuccess(
                () -> Component.translatable(
                        "command.revivalages.weight.inspect_armor",
                        finalPockets,
                        WeightFormatting.exact(
                                finalPockets * CarriedWeightSettings.snapshot().pocketCapacity()
                        )
                ),
                false
        );
        return 1;
    }
}
