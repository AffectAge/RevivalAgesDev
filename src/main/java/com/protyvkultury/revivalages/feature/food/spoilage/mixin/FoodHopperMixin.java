package com.protyvkultury.revivalages.feature.food.spoilage.mixin;

import com.protyvkultury.revivalages.feature.food.spoilage.FoodSpoilageFeature;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class FoodHopperMixin {

    @Unique
    private static final ThreadLocal<MoveContext> REVIVALAGES$MOVE_CONTEXT = new ThreadLocal<>();

    @ModifyVariable(method = "tryMoveInItem", at = @At("HEAD"), argsOnly = true)
    private static ItemStack revivalages$materializeMovingStack(ItemStack moving) {
        return com.protyvkultury.revivalages.api.food.FoodFreshnessApi.materialize(moving);
    }

    @Inject(method = "tryMoveInItem", at = @At("HEAD"))
    private static void revivalages$captureFreshness(
            Container source,
            Container destination,
            ItemStack moving,
            int slot,
            Direction side,
            CallbackInfoReturnable<ItemStack> callback
    ) {
        ItemStack target = destination.getItem(slot);
        REVIVALAGES$MOVE_CONTEXT.set(new MoveContext(destination, slot, target.getCount(), moving.copy()));
    }

    @Inject(method = "tryMoveInItem", at = @At("RETURN"))
    private static void revivalages$inheritOldestDate(
            Container source,
            Container destination,
            ItemStack moving,
            int slot,
            Direction side,
            CallbackInfoReturnable<ItemStack> callback
    ) {
        MoveContext context = REVIVALAGES$MOVE_CONTEXT.get();
        REVIVALAGES$MOVE_CONTEXT.remove();
        if (context == null || context.destination() != destination || context.slot() != slot) {
            return;
        }
        ItemStack target = destination.getItem(slot);
        if (target.getCount() > context.targetCount()
                && FoodSpoilageFeature.mayStack(target, context.source())) {
            FoodSpoilageFeature.inheritOldestAfterMerge(target, context.source());
        }
    }

    private record MoveContext(Container destination, int slot, int targetCount, ItemStack source) {
    }
}
