package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineKind;
import com.protyvkultury.revivalages.feature.technology.stonemachine.blockentity.StoneMachineBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

public enum StoneMachineComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = RevivalAges.id("stone_machines");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof StoneMachineBlockEntity machine)) {
            return;
        }
        tooltip.add(Component.translatable("jade.revivalages.stone_machine.state."
                + (machine.isLit() ? "lit" : "unlit")));
        tooltip.add(Component.translatable(
                "jade.revivalages.stone_machine.fuel",
                machine.fuel().isEmpty() ? Component.literal("-") : machine.fuel().getHoverName(),
                machine.burnTime()
        ));
        if (machine.kind() == StoneMachineKind.SAWMILL && machine.blade().isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.stone_machine.no_blade"));
        }
        if (!machine.isLit() && machine.burnTime() <= 0 && machine.fuel().isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.stone_machine.no_fuel"));
        }
        if (!machine.firstOutput().isEmpty()) {
            tooltip.add(Component.translatable(machine.input().isEmpty()
                    ? "jade.revivalages.stone_machine.output_ready"
                    : "jade.revivalages.stone_machine.output_blocked"));
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
        if (machine.totalTicks() > 0) {
            tooltip.add(Component.translatable(
                    "jade.revivalages.stone_machine.time",
                    String.format(Locale.ROOT, "%.1f", machine.elapsedTicks() / 20.0D),
                    String.format(Locale.ROOT, "%.1f", machine.totalTicks() / 20.0D)));
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
        appendProcess(tooltip, machine);
        if (machine.kind() == StoneMachineKind.CRUCIBLE) {
            appendFluid(tooltip, machine.fluidTank().getFluid(), machine.fluidTank().getCapacity());
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
        line.add(elements.spacer(2, 0));
        line.add(elements.progress((float) Math.clamp(machine.progress(), 0.0D, 1.0D)));
        line.add(elements.spacer(2, 0));
        if (machine.kind() == StoneMachineKind.CRUCIBLE) {
            FluidStack result = machine.recipeFluidResult();
            if (!result.isEmpty()) {
                tooltip.add(Component.translatable(
                        "jade.revivalages.barrel.result", result.getHoverName(), result.getAmount()));
            }
        } else {
            ItemStack result = machine.recipeItemResult();
            if (!result.isEmpty()) {
                line.add(elements.item(result));
            }
        }
        tooltip.add(line);
    }

    private static void appendFluid(ITooltip tooltip, FluidStack fluid, int capacity) {
        if (fluid.isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.primitive.fluid.empty", capacity));
        } else {
            tooltip.add(Component.translatable(
                    "jade.revivalages.primitive.fluid", fluid.getHoverName(), fluid.getAmount(), capacity));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
