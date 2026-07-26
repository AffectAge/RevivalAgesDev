package com.protyvkultury.revivalages.feature.food.spoilage.mixin;

import com.protyvkultury.revivalages.feature.food.spoilage.FoodSpoilageFeature;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class FoodSlotMixin {

    @Unique
    private int revivalages$targetCountBeforeInsert;
    @Unique
    private ItemStack revivalages$sourceBeforeInsert = ItemStack.EMPTY;

    @Inject(method = "safeInsert(Lnet/minecraft/world/item/ItemStack;I)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"))
    private void revivalages$captureFreshness(
            ItemStack source,
            int maximum,
            CallbackInfoReturnable<ItemStack> callback
    ) {
        ItemStack target = ((Slot) (Object) this).getItem();
        revivalages$targetCountBeforeInsert = target.getCount();
        revivalages$sourceBeforeInsert = source.copy();
    }

    @Inject(method = "safeInsert(Lnet/minecraft/world/item/ItemStack;I)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"))
    private void revivalages$inheritOldestDate(
            ItemStack source,
            int maximum,
            CallbackInfoReturnable<ItemStack> callback
    ) {
        ItemStack target = ((Slot) (Object) this).getItem();
        if (target.getCount() > revivalages$targetCountBeforeInsert
                && FoodSpoilageFeature.mayStack(target, revivalages$sourceBeforeInsert)) {
            FoodSpoilageFeature.inheritOldestAfterMerge(target, revivalages$sourceBeforeInsert);
        }
        revivalages$sourceBeforeInsert = ItemStack.EMPTY;
    }
}
