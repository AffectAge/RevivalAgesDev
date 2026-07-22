package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.anvil.block.AnvilBlock;
import com.protyvkultury.revivalages.feature.technology.anvil.blockentity.AnvilBlockEntity;
import com.protyvkultury.revivalages.feature.technology.anvil.recipe.AnvilTool;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum AnvilComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = RevivalAges.id("anvil");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof AnvilBlockEntity anvil)) {
            return;
        }
        tooltip.add(Component.translatable(
                "jade.revivalages.anvil.damage", accessor.getBlockState().getValue(AnvilBlock.DAMAGE) + 1));
        if (anvil.input().isEmpty()) {
            return;
        }
        if (anvil.requiredHits() > 0) {
            tooltip.add(Component.translatable("jade.revivalages.anvil.hits", anvil.hits(), anvil.requiredHits()));
            tooltip.add(Component.translatable(
                    anvil.activeTool() == AnvilTool.HAMMER
                            ? "gui.revivalages.recipe.tool.hammer"
                            : "gui.revivalages.recipe.tool.pickaxe"));
        } else {
            tooltip.add(Component.translatable("jade.revivalages.anvil.select_tool"));
        }
        List<IElement> line = new ArrayList<>();
        IElementHelper elements = IElementHelper.get();
        line.add(elements.item(anvil.input()));
        line.add(elements.spacer(2, 0));
        line.add(elements.progress((float) anvil.progress()));
        line.add(elements.spacer(2, 0));
        if (!anvil.recipeOutput().isEmpty()) {
            line.add(elements.item(anvil.recipeOutput()));
        }
        tooltip.add(line);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
