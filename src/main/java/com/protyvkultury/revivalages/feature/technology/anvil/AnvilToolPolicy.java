package com.protyvkultury.revivalages.feature.technology.anvil;

import com.protyvkultury.revivalages.core.process.ToolRequirementView;
import com.protyvkultury.revivalages.feature.technology.anvil.recipe.AnvilRecipe;
import com.protyvkultury.revivalages.feature.technology.anvil.recipe.AnvilTool;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Canonical Anvil tool-to-ingredient mapping used by the viewer read model. */
public final class AnvilToolPolicy {

    private AnvilToolPolicy() {
    }

    public static Ingredient ingredient(AnvilTool tool) {
        return switch (tool) {
            case HAMMER -> Ingredient.of(AnvilTags.HAMMERS);
            case PICKAXE -> Ingredient.of(ItemTags.PICKAXES);
        };
    }

    public static boolean matches(ItemStack stack, AnvilTool tool) {
        return switch (tool) {
            case HAMMER -> stack.is(AnvilTags.HAMMERS);
            case PICKAXE -> stack.is(ItemTags.PICKAXES);
        };
    }

    public static AnvilTool typeFor(ItemStack stack) {
        return matches(stack, AnvilTool.HAMMER) ? AnvilTool.HAMMER : AnvilTool.PICKAXE;
    }

    public static ToolRequirementView viewerRequirement(AnvilRecipe recipe) {
        String toolKey = "gui.revivalages.recipe.tool." + recipe.tool().getSerializedName();
        return new ToolRequirementView(ingredient(recipe.tool()), List.of(
                Component.translatable("gui.revivalages.tool_requirement.anvil", Component.translatable(toolKey)),
                Component.translatable("gui.revivalages.tool_requirement.anvil_hits", recipe.hits()),
                Component.translatable(PrimitiveTechnologyConfig.ANVIL_USE_TOOL_DURABILITY.get()
                        ? "gui.revivalages.tool_requirement.anvil_durability_enabled"
                        : "gui.revivalages.tool_requirement.anvil_durability_disabled"),
                Component.translatable("gui.revivalages.tool_requirement.anvil_hunger")
        ));
    }
}
