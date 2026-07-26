package com.protyvkultury.revivalages.feature.food.spoilage.mixin;

import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public abstract class FoodCraftingResultMixin {

    @Shadow
    @Final
    private CraftingContainer craftSlots;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void revivalages$inheritFreshness(
            Player player,
            ItemStack output,
            CallbackInfo callback
    ) {
        List<ItemStack> inputs = craftSlots.getItems().stream().map(ItemStack::copy).toList();
        player.level().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftSlots.asCraftInput(), player.level())
                .ifPresent(recipe -> FoodFreshnessApi.transformOutput(output, inputs, recipe.id()));
    }
}
