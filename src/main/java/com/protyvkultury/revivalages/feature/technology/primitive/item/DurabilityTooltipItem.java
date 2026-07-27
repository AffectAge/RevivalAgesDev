package com.protyvkultury.revivalages.feature.technology.primitive.item;

import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyClientConfig;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class DurabilityTooltipItem extends Item {

    public DurabilityTooltipItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (!PrimitiveTechnologyClientConfig.SHOW_DURABILITY_TOOLTIPS.get()) {
            return;
        }
        int maximum = stack.getMaxDamage();
        int remaining = maximum - stack.getDamageValue();
        tooltip.add(Component.translatable(
                        remaining == maximum
                                ? "tooltip.revivalages.durability.full"
                                : "tooltip.revivalages.durability",
                        remaining,
                        maximum
                )
                .withStyle(ChatFormatting.GRAY));
    }
}
