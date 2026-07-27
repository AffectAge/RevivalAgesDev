package com.protyvkultury.revivalages.feature.technology.ignition.item;

import com.protyvkultury.revivalages.feature.technology.ignition.WoodTorchSettings;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public final class WoodTorchItem extends BlockItem {

    public WoodTorchItem(Block block, Properties properties) {
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
        WoodTorchSettings.Snapshot settings = WoodTorchSettings.clientSnapshot();
        boolean burnsUp = settings.burnsUp();
        boolean rainExtinguishes = settings.rainExtinguishes();
        tooltip.add(Component.translatable("tooltip.revivalages.wood_torch.burns_up." + burnsUp)
                .withStyle(burnsUp ? ChatFormatting.RED : ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.revivalages.wood_torch.rain_extinguishes." + rainExtinguishes)
                .withStyle(rainExtinguishes ? ChatFormatting.RED : ChatFormatting.GREEN));
    }
}
