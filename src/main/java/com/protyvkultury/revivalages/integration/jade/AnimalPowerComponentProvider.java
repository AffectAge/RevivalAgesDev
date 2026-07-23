package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.animalpower.block.AnimalMachineBlock;
import com.protyvkultury.revivalages.feature.technology.animalpower.blockentity.AnimalMachineBlockEntity;
import com.protyvkultury.revivalages.feature.technology.animalpower.blockentity.HandGrindstoneBlockEntity;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum AnimalPowerComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = RevivalAges.id("animal_power");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (blockEntity == null
                && accessor.getBlockState().hasProperty(AnimalMachineBlock.HALF)
                && accessor.getBlockState().getValue(AnimalMachineBlock.HALF) == DoubleBlockHalf.UPPER) {
            blockEntity = accessor.getLevel().getBlockEntity(accessor.getPosition().below());
        }
        if (blockEntity instanceof HandGrindstoneBlockEntity hand) {
            appendItemProgress(tooltip, hand.item(0), hand.recipeOutput(), hand.progress());
            return;
        }
        if (!(blockEntity instanceof AnimalMachineBlockEntity machine)) {
            return;
        }
        if (machine.workerId().isPresent()) {
            Component workerName = accessor.getLevel()
                    .getEntitiesOfClass(
                            Mob.class,
                            new AABB(machine.getBlockPos()).inflate(64.0D),
                            mob -> mob.getUUID().equals(machine.workerId().orElse(null)))
                    .stream()
                    .findFirst()
                    .map(Mob::getDisplayName)
                    .orElse(Component.translatable("jade.revivalages.animal_power.worker.unloaded"));
            tooltip.add(Component.translatable(
                    "jade.revivalages.animal_power.worker.attached",
                    workerName
            ));
        } else {
            tooltip.add(Component.translatable("jade.revivalages.animal_power.worker.missing"));
        }
        tooltip.add(Component.translatable(
                machine.workAreaValid()
                        ? "jade.revivalages.animal_power.area.valid"
                        : "jade.revivalages.animal_power.area.invalid"
        ));
        tooltip.add(Component.translatable("jade.revivalages.animal_power.state." + machine.blockingState()));
        appendItemProgress(tooltip, machine.item(0), machine.recipeOutput(), machine.progress());
        if (!machine.fluidTank().isEmpty()) {
            tooltip.add(Component.translatable(
                    "jade.revivalages.animal_power.fluid",
                    machine.fluidTank().getFluid().getHoverName(),
                    machine.fluidTank().getFluidAmount(),
                    machine.fluidTank().getCapacity()
            ));
        }
    }

    private static void appendItemProgress(ITooltip tooltip, ItemStack input, ItemStack output, double progress) {
        if (input.isEmpty()) {
            return;
        }
        IElementHelper elements = IElementHelper.get();
        IElement result = output.isEmpty() ? elements.spacer(16, 16) : elements.item(output);
        tooltip.add(List.of(
                elements.item(input),
                elements.spacer(2, 0),
                elements.progress((float) Math.clamp(progress, 0.0D, 1.0D)),
                elements.spacer(2, 0),
                result
        ));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
