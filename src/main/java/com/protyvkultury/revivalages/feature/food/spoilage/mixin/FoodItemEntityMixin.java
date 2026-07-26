package com.protyvkultury.revivalages.feature.food.spoilage.mixin;

import com.protyvkultury.revivalages.feature.food.spoilage.FoodSpoilageFeature;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class FoodItemEntityMixin {

    @Inject(method = "merge(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)"
            + "Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"))
    private static void revivalages$inheritOldestDate(
            ItemStack target,
            ItemStack source,
            int maximum,
            CallbackInfoReturnable<ItemStack> callback
    ) {
        if (FoodSpoilageFeature.mayStack(target, source)) {
            FoodSpoilageFeature.inheritOldestAfterMerge(target, source);
        }
    }
}
