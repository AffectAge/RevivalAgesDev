package com.protyvkultury.revivalages.feature.food.spoilage.mixin;

import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ingredient.class)
public abstract class ExpiredIngredientMixin {

    @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void revivalages$rejectExpiredFood(
            ItemStack stack,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (FoodFreshnessApi.expired(stack)) {
            callback.setReturnValue(false);
        }
    }
}
