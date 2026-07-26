package com.protyvkultury.revivalages.integration.kubejs;

import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public enum FoodFreshnessScriptBindings {
    INSTANCE;

    public boolean perishable(ItemStack stack) {
        return FoodFreshnessApi.profile(stack).isPresent();
    }

    public boolean expired(ItemStack stack) {
        return FoodFreshnessApi.expired(stack);
    }

    public long remainingTicks(ItemStack stack) {
        return FoodFreshnessApi.remaining(stack);
    }

    public List<ResourceLocation> traits(ItemStack stack) {
        return FoodFreshnessApi.state(stack).map(value -> value.traits()).orElse(List.of());
    }
}
