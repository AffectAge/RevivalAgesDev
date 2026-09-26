package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineKind;
import com.protyvkultury.revivalages.feature.technology.stonemachine.blockentity.StoneMachineBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.fluid.JadeFluidObject;

public enum StoneMachineComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = RevivalAges.id("stone_machines");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof StoneMachineBlockEntity machine)) {
            return;
        }
        appendProcess(tooltip, machine);
        tooltip.add(Component.translatable("jade.revivalages.stone_machine.state."
                + (machine.isLit() ? "lit" : "unlit"))
                .withStyle(machine.isLit() ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (machine.kind() == StoneMachineKind.SAWMILL && machine.blade().isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.stone_machine.no_blade")
                    .withStyle(ChatFormatting.RED));
        }
        if (!machine.isLit() && !machine.input().isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.stone_machine.no_fuel")
                    .withStyle(ChatFormatting.RED));
        }
        if (!machine.firstOutput().isEmpty()) {
            tooltip.add(List.of(IElementHelper.get().item(machine.firstOutput())));
            if (!machine.input().isEmpty()) {
                tooltip.add(Component.translatable("jade.revivalages.stone_machine.output_blocked"));
            }
        }
        if (machine.airflowBonus() > 0.0F) {
            tooltip.add(Component.translatable(
                    "jade.revivalages.stone_machine.airflow",
                    String.format(Locale.ROOT, "%.2f", machine.airflowBonus())
            ));
        }
        if (machine.isLit()) {
            tooltip.add(Component.translatable(
                    "jade.revivalages.stone_machine.speed",
                    String.format(Locale.ROOT, "%.2f", 1.0F + machine.airflowBonus())));
        }
        if (machine.recipeFailureChance() > 0.0F) {
            tooltip.add(Component.translatable(
                    "jade.revivalages.stone_machine.failure",
                    String.format(Locale.ROOT, "%.0f%%", machine.recipeFailureChance() * 100.0F)));
        }
        if (machine.recipeWoodChips() > 0) {
            tooltip.add(Component.translatable(
                    "jade.revivalages.stone_machine.wood_chips",
                    machine.recipeWoodChips(),
                    String.format(Locale.ROOT, "%.0f%%",
                            machine.woodChipChanceForView() * 100.0D)));
        }
    }

    private static void appendProcess(ITooltip tooltip, StoneMachineBlockEntity machine) {
        ItemStack input = machine.input();
        if (input.isEmpty()) {
            return;
        }
        IElementHelper elements = IElementHelper.get();
        List<IElement> line = new ArrayList<>();
        line.add(elements.item(input));
        if (machine.kind() == StoneMachineKind.SAWMILL && !machine.blade().isEmpty()) {
            line.add(elements.item(machine.blade()));
        }
        if (!machine.fuel().isEmpty()) {
            line.add(elements.item(machine.fuel()));
        }
        line.add(elements.spacer(2, 0));
        line.add(JadeProgressElement.of(elements, machine.progress()));
        line.add(elements.spacer(2, 0));
        if (machine.kind() == StoneMachineKind.CRUCIBLE) {
            FluidStack result = machine.recipeFluidResult();
            if (!result.isEmpty()) {
                line.add(elements.fluid(JadeFluidObject.of(
                        result.getFluid(),
                        result.getAmount(),
                        result.getComponentsPatch()
                )));
            }
        } else {
            ItemStack result = machine.recipeItemResult();
            if (!result.isEmpty()) {
                line.add(elements.item(result));
            }
        }
        tooltip.add(line);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
