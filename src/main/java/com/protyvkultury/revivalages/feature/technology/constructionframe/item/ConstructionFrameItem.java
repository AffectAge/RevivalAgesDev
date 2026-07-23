package com.protyvkultury.revivalages.feature.technology.constructionframe.item;

import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameConfig;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public final class ConstructionFrameItem extends BlockItem {

    public ConstructionFrameItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.revivalages.construction_frame.insert")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.revivalages.construction_frame.remove")
                .withStyle(ChatFormatting.GRAY));
        if (!ConstructionFrameConfig.enabled()) {
            tooltip.add(Component.translatable("message.revivalages.construction_frame.disabled")
                    .withStyle(ChatFormatting.RED));
        }
    }
}
