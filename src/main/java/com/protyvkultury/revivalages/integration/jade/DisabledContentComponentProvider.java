package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Replaces normal probe details with one stable availability notice for preserved blocks. */
public enum DisabledContentComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = RevivalAges.id("disabled_content");

    public static boolean isDisabled(BlockAccessor accessor) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(accessor.getBlock());
        return id.getNamespace().equals(RevivalAges.MOD_ID)
                && !ContentAvailability.isBlockEnabled(id);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!isDisabled(accessor)) {
            return;
        }
        Set<ContentKey> keys = ContentAvailability.blockKeys(
                BuiltInRegistries.BLOCK.getKey(accessor.getBlock()));
        Component content = keys.isEmpty()
                ? Component.literal(BuiltInRegistries.BLOCK.getKey(accessor.getBlock()).toString())
                : keys.iterator().next().displayName();
        tooltip.add(Component.translatable("jade.revivalages.content.disabled", content));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
