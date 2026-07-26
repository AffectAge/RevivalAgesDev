package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.size.ContainerSizePolicy;
import com.protyvkultury.revivalages.api.size.Size;
import com.protyvkultury.revivalages.api.size.SizeApi;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ItemSizeComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation ID = RevivalAges.id("item_size");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!SizeApi.enabled()) {
            return;
        }
        if (!accessor.getBlockState().is(Blocks.CHEST)
                && !accessor.getBlockState().is(Blocks.TRAPPED_CHEST)) {
            return;
        }
        ContainerSizePolicy policy = SizeApi.blockPolicy(accessor.getBlockState());
        if (policy == null) {
            return;
        }
        Size maximum = SizeApi.effectiveMaximum(accessor.getBlock(), policy.maxSize());
        tooltip.add(Component.translatable(
                "jade.revivalages.item_size.maximum",
                Component.translatable("size.revivalages." + maximum.getSerializedName())
        ));
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
