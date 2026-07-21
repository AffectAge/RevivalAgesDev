package com.protyvkultury.revivalages.feature.technology.barrel.item;

import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;

public final class BarrelBlockItem extends BlockItem {

    public BarrelBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        FluidStack fluid = stack.getOrDefault(PrimitiveMaterialsFeature.STORED_FLUID.get(), FluidStack.EMPTY);
        if (!fluid.isEmpty()) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.revivalages.barrel.fluid",
                    fluid.getHoverName(),
                    fluid.getAmount()
            ).withStyle(ChatFormatting.GRAY));
            if (!PrimitiveTechnologyConfig.WOODEN_CONTAINERS_HOLD_HOT_FLUIDS.get()
                    && fluid.getFluidType().getTemperature(fluid)
                    >= PrimitiveTechnologyConfig.HOT_FLUID_TEMPERATURE.get()) {
                tooltipComponents.add(Component.translatable("tooltip.revivalages.barrel.hot_fluid")
                        .withStyle(ChatFormatting.RED));
            }
        }
        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        long itemCount = contents.stream().filter(item -> !item.isEmpty()).count();
        if (itemCount > 0) {
            tooltipComponents.add(Component.translatable("tooltip.revivalages.barrel.items", itemCount)
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
