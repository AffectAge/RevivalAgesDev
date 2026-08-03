package com.protyvkultury.revivalages.core.process;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;

/** Loader-neutral description of a real tool ingredient and its contextual viewer tooltip. */
public record ToolRequirementView(Ingredient ingredient, List<Component> tooltip) {

    public ToolRequirementView {
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException("Tool requirement ingredient cannot be empty");
        }
        tooltip = List.copyOf(tooltip);
    }
}
