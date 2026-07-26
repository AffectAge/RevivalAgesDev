package com.protyvkultury.revivalages.feature.inventory.itemsize.mixin;

import com.protyvkultury.revivalages.feature.inventory.itemsize.ItemSizeRejectionFeedback;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Observes actual menu actions so player feedback is not emitted by repeated
 * slot eligibility queries.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class ChestMenuFeedbackMixin {

    @Inject(method = "doClick", at = @At("HEAD"))
    private void revivalages$notifyRejectedChestInsertion(
            int slotId,
            int button,
            ClickType clickType,
            Player player,
            CallbackInfo callback
    ) {
        ItemSizeRejectionFeedback.handleMenuClick(
                (AbstractContainerMenu) (Object) this,
                slotId,
                button,
                clickType,
                player
        );
    }
}
