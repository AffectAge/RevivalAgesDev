package com.protyvkultury.revivalages.feature.inventory.itemsize.mixin;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Connects chest menu slots to the same container insertion check used by
 * automation. Vanilla's base Slot accepts every stack without consulting its
 * Container, so the block-entity boundary alone does not cover manual clicks.
 */
@Mixin(Slot.class)
public abstract class ChestSlotMixin {

    @Shadow
    @Final
    public Container container;

    @Shadow
    @Final
    private int slot;

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void revivalages$applyChestInsertionRule(
            ItemStack stack,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (container instanceof ChestBlockEntity || container instanceof CompoundContainer) {
            callback.setReturnValue(container.canPlaceItem(slot, stack));
        }
    }
}
