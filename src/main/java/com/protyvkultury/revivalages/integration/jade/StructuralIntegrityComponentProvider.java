package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityConfig;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityTags;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.SupportDefinition;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.SupportService;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum StructuralIntegrityComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = RevivalAges.id("structural_integrity");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        SupportDefinition support = SupportService.definition(accessor.getBlockState());
        if (support != null) {
            tooltip.add(Component.translatable(
                    "jade.revivalages.support.range",
                    support.supportUp(),
                    support.supportDown(),
                    support.supportHorizontal()
            ));
        }
        boolean structural = accessor.getBlockState().is(StructuralIntegrityTags.CAN_COLLAPSE)
                || accessor.getBlockState().is(StructuralIntegrityTags.CAN_LANDSLIDE);
        if (!structural) {
            return;
        }
        if (!StructuralIntegrityConfig.collapsesEnabled() && !StructuralIntegrityConfig.landslidesEnabled()) {
            tooltip.add(Component.translatable("jade.revivalages.structural.disabled"));
        } else if (SupportService.isSupported(accessor.getLevel(), accessor.getPosition())) {
            tooltip.add(Component.translatable("jade.revivalages.structural.supported"));
        } else {
            tooltip.add(Component.translatable("jade.revivalages.structural.unsupported"));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
