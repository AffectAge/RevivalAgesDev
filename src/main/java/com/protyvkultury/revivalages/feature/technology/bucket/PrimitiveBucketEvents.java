package com.protyvkultury.revivalages.feature.technology.bucket;

import com.protyvkultury.revivalages.feature.technology.bucket.item.PrimitiveBucketItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class PrimitiveBucketEvents {

    private PrimitiveBucketEvents() {
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Cow cow) || cow.isBaby()) {
            return;
        }
        Player player = event.getEntity();
        ItemStack held = player.getItemInHand(event.getHand());
        if (!(held.getItem() instanceof PrimitiveBucketItem bucket)
                || !held.getOrDefault(PrimitiveBucketFeature.BUCKET_FLUID.get(), SimpleFluidContent.EMPTY).isEmpty()) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide));
        if (player.level().isClientSide) {
            return;
        }
        ItemStack filled = held.copyWithCount(1);
        bucket.createHandler(filled).fill(
                new FluidStack(NeoForgeMod.MILK.value(), 1000), IFluidHandler.FluidAction.EXECUTE);
        if (held.getCount() == 1) {
            player.setItemInHand(event.getHand(), filled);
        } else {
            held.shrink(1);
            if (!player.getInventory().add(filled)) {
                player.drop(filled, false);
            }
        }
        player.level().playSound(null, cow.blockPosition(), SoundEvents.COW_MILK, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
