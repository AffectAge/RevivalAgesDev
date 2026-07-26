package com.protyvkultury.revivalages.feature.food.spoilage.mixin;

import com.protyvkultury.revivalages.feature.food.spoilage.FoodSpoilageFeature;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class FoodStackingMixin {

    @Inject(method = "isSameItemSameComponents", at = @At("HEAD"), cancellable = true)
    private static void revivalages$compareFreshness(
            ItemStack first,
            ItemStack second,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (first.isEmpty() || second.isEmpty() || !first.is(second.getItem())) {
            return;
        }
        var component = FoodSpoilageFeature.FOOD_STATE.get();
        if (!first.has(component) && !second.has(component)) {
            return;
        }
        ItemStack firstWithoutFreshness = first.copy();
        ItemStack secondWithoutFreshness = second.copy();
        firstWithoutFreshness.remove(component);
        secondWithoutFreshness.remove(component);
        if (!ItemStack.isSameItemSameComponents(firstWithoutFreshness, secondWithoutFreshness)) {
            callback.setReturnValue(false);
            return;
        }
        callback.setReturnValue(FoodSpoilageFeature.mayStack(first, second));
    }
}
