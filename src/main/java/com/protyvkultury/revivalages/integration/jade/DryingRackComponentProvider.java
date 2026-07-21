package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.dryingrack.blockentity.DryingRackBlockEntity;
import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.DryingEnvironmentModifier;
import com.protyvkultury.revivalages.feature.technology.dryingrack.view.DryingRackView;
import com.protyvkultury.revivalages.feature.technology.dryingrack.view.DryingSlotView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum DryingRackComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = RevivalAges.id("drying_rack");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof DryingRackBlockEntity rack)) {
            return;
        }

        DryingRackView view = rack.view();
        tooltip.add(Component.translatable(
                "jade.revivalages.drying_rack.speed",
                formatPercent(view.speed(), false)
        ));
        tooltip.add(Component.translatable(
                "jade.revivalages.drying_rack.base." + view.environment().base().name().toLowerCase(Locale.ROOT),
                formatPercent(view.environment().baseSpeed(), true)
        ));
        for (DryingEnvironmentModifier modifier : view.environment().modifiers()) {
            tooltip.add(Component.translatable(
                    "jade.revivalages.drying_rack.modifier."
                            + modifier.type().name().toLowerCase(Locale.ROOT),
                    formatPercent(modifier.amount(), true)
            ));
        }
        tooltip.add(Component.translatable(
                "jade.revivalages.drying_rack.multiplier",
                String.format(Locale.ROOT, "%.2f", view.environment().rackMultiplier())
        ));

        IElementHelper elements = IElementHelper.get();
        for (DryingSlotView slot : view.slots()) {
            if (slot.stack().isEmpty()) {
                continue;
            }
            if (slot.processing() && !slot.recipeOutput().isEmpty()) {
                List<IElement> line = new ArrayList<>();
                line.add(elements.item(slot.stack()));
                line.add(elements.spacer(2, 0));
                line.add(elements.progress((float) slot.progress()));
                line.add(elements.spacer(2, 0));
                line.add(elements.item(slot.recipeOutput()));
                tooltip.add(line);
            } else if (slot.completed()) {
                tooltip.add(Component.translatable(
                        "jade.revivalages.drying_rack.complete",
                        slot.stack().getHoverName()
                ));
            } else {
                tooltip.add(Component.translatable(
                        "jade.revivalages.drying_rack.no_recipe",
                        slot.stack().getHoverName()
                ));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    private static String formatPercent(double value, boolean signed) {
        return String.format(Locale.ROOT, signed ? "%+.0f%%" : "%.0f%%", value * 100.0D);
    }
}
