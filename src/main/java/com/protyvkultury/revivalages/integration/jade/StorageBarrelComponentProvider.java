package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.barrel.storage.StorageBarrelBlock;
import com.protyvkultury.revivalages.feature.technology.barrel.storage.StorageBarrelBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum StorageBarrelComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation ID = RevivalAges.id("storage_barrel");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        boolean sealed = accessor.getBlockState().getValue(StorageBarrelBlock.SEALED);
        tooltip.add(Component.translatable("jade.revivalages.storage_barrel.sealed." + sealed));
        if (accessor.getBlockEntity() instanceof StorageBarrelBlockEntity barrel) {
            tooltip.add(Component.translatable(
                    "jade.revivalages.storage_barrel.capacity",
                    barrel.getContainerSize()
            ));
        }
        if (sealed) {
            tooltip.add(Component.translatable("jade.revivalages.storage_barrel.preserved"));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
