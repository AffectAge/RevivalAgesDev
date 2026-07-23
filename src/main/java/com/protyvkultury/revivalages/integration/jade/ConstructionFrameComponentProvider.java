package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameConfig;
import com.protyvkultury.revivalages.feature.technology.constructionframe.blockentity.ConstructionFrameBlockEntity;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum ConstructionFrameComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = RevivalAges.id("construction_frame");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof ConstructionFrameBlockEntity frame)) {
            return;
        }
        if (!ConstructionFrameConfig.enabled()) {
            tooltip.add(Component.translatable("message.revivalages.construction_frame.disabled"));
            return;
        }
        tooltip.add(Component.translatable(
                "jade.revivalages.construction_frame.filled",
                frame.occupiedCells()
        ));
        frame.matchingRecipe().ifPresent(recipe -> {
            IElementHelper elements = IElementHelper.get();
            IElement result = elements.item(recipe.result());
            tooltip.add(List.of(
                    result,
                    elements.spacer(2, 0),
                    elements.text(Component.translatable(
                            "jade.revivalages.construction_frame.result",
                            recipe.result().getHoverName()
                    ))
            ));
            ItemStack[] tools = recipe.tool().getItems();
            if (tools.length > 0) {
                tooltip.add(Component.translatable(
                        "jade.revivalages.construction_frame.tool",
                        tools[0].getHoverName()
                ));
            }
            if (frame.isPlacementBlocked(recipe)) {
                tooltip.add(Component.translatable("jade.revivalages.construction_frame.blocked"));
            }
        });
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
