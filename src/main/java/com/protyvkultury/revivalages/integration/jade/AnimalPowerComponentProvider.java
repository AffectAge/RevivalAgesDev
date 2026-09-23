package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.animalpower.blockentity.HandGrindstoneBlockEntity;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum AnimalPowerComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = RevivalAges.id("hand_grinding");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (DisabledContentComponentProvider.isDisabled(accessor)) {
            return;
        }
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (blockEntity instanceof HandGrindstoneBlockEntity hand) {
            appendItemProgress(tooltip, hand.item(0), hand.recipeOutput(), hand.progress());
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
