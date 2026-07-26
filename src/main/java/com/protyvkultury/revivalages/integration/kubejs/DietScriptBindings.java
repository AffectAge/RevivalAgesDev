package com.protyvkultury.revivalages.integration.kubejs;

import com.protyvkultury.revivalages.api.diet.DietApi;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public enum DietScriptBindings {
    INSTANCE;

    public Map<ResourceLocation, Double> values(Player player) {
        return DietApi.values(player);
    }

    public Map<ResourceLocation, Double> groups(ItemStack stack) {
        return DietApi.contribution(stack).map(value -> value.groups()).orElse(Map.of());
    }
}
