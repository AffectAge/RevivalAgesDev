package com.protyvkultury.revivalages.feature.technology.animalpower.recipe;

import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class WoodVariantChoppingBlockRecipe extends CustomRecipe {

    public WoodVariantChoppingBlockRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != 3 || input.height() != 3) {
            return false;
        }
        return input.getItem(0, 0).is(Items.LEAD)
                && input.getItem(1, 0).is(Tags.Items.RODS_WOODEN)
                && input.getItem(2, 0).is(Items.LEAD)
                && input.getItem(0, 1).is(Tags.Items.RODS_WOODEN)
                && input.getItem(1, 1).is(Items.FLINT)
                && input.getItem(2, 1).is(Tags.Items.RODS_WOODEN)
                && input.getItem(0, 2).is(Tags.Items.RODS_WOODEN)
                && input.getItem(1, 2).is(ItemTags.LOGS)
                && input.getItem(2, 2).is(Tags.Items.RODS_WOODEN);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        if (input.width() != 3 || input.height() != 3) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(AnimalPowerFeature.HORSE_CHOPPING_BLOCK_ITEM.get());
        result.set(
                AnimalPowerFeature.WOOD_VARIANT.get(),
                BuiltInRegistries.ITEM.getKey(input.getItem(1, 2).getItem())
        );
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimalPowerFeature.WOOD_VARIANT_RECIPE_SERIALIZER.get();
    }
}
