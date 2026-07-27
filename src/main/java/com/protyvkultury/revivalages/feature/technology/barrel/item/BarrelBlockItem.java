package com.protyvkultury.revivalages.feature.technology.barrel.item;

import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
        boolean holdsHot = PrimitiveTechnologyConfig.WOODEN_CONTAINERS_HOLD_HOT_FLUIDS.get();
        tooltipComponents.add(Component.translatable(
                        "tooltip.revivalages.primitive_bucket.hot_fluids." + holdsHot)
                .withStyle(holdsHot ? ChatFormatting.GREEN : ChatFormatting.RED));
    }
}
