package com.protyvkultury.revivalages.feature.technology.primitive.client;

import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineFeature;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.FluidStack;

public final class PrimitiveItemTooltipEvents {

    private PrimitiveItemTooltipEvents() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PrimitiveItemTooltipEvents::appendTooltip);
    }

    private static void appendTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        if (stack.is(BarrelFeature.BARREL_ITEM.get())) {
            appendBarrel(stack, tooltip);
            return;
        }
        if (stack.is(StoneMachineFeature.STONE_SAWMILL_ITEM.get())
                || stack.is(StoneMachineFeature.STONE_OVEN_ITEM.get())
                || stack.is(StoneMachineFeature.STONE_KILN_ITEM.get())
                || stack.is(StoneMachineFeature.STONE_CRUCIBLE_ITEM.get())) {
            appendStoneMachine(stack, tooltip);
        }
    }

    private static void appendBarrel(ItemStack stack, List<Component> tooltip) {
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.revivalages.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        FluidStack fluid = stack.getOrDefault(PrimitiveMaterialsFeature.STORED_FLUID.get(), FluidStack.EMPTY);
        if (!fluid.isEmpty()) {
            tooltip.add(Component.translatable(
                    "tooltip.revivalages.barrel.fluid",
                    fluid.getHoverName(),
                    fluid.getAmount()
            ).withStyle(ChatFormatting.GRAY));
        }
        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        contents.stream()
                .filter(item -> !item.isEmpty())
                .forEach(item -> tooltip.add(Component.translatable(
                        "tooltip.revivalages.barrel.item_stack",
                        item.getHoverName(),
                        item.getCount()
                ).withStyle(ChatFormatting.GRAY)));
    }

    private static void appendStoneMachine(ItemStack stack, List<Component> tooltip) {
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.revivalages.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        tooltip.add(Component.translatable(
                "tooltip.revivalages.stone_machine.input_capacity",
                PrimitiveTechnologyConfig.STONE_MACHINE_INPUT_LIMIT.get()
        ).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
                "tooltip.revivalages.stone_machine.fuel_capacity",
                PrimitiveTechnologyConfig.STONE_MACHINE_FUEL_LIMIT.get()
        ).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
                "tooltip.revivalages.stone_machine.burn_modifier",
                PrimitiveTechnologyConfig.STONE_MACHINE_FUEL_MULTIPLIER.get()
        ).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
                "tooltip.revivalages.stone_machine.keep_heat."
                        + PrimitiveTechnologyConfig.STONE_MACHINE_KEEP_HEAT.get()
        ).withStyle(ChatFormatting.GRAY));
        if (stack.is(StoneMachineFeature.STONE_CRUCIBLE_ITEM.get())) {
            tooltip.add(Component.translatable(
                    "tooltip.revivalages.stone_machine.tank_capacity",
                    PrimitiveTechnologyConfig.STONE_CRUCIBLE_CAPACITY.get()
            ).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.revivalages.primitive_bucket.hot_fluids.true")
                    .withStyle(ChatFormatting.GREEN));
        }
    }
}
