package com.protyvkultury.revivalages.feature.inventory.itemsize;

import com.protyvkultury.revivalages.api.size.SizeApi;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/**
 * Emits player-facing feedback only for real rejected insertion actions. Slot
 * eligibility queries and automation remain silent.
 */
public final class ItemSizeRejectionFeedback {

    private static final String LAST_FEEDBACK_TICK = "revivalages.item_size.last_rejection_tick";

    private ItemSizeRejectionFeedback() {
    }

    public static void handleMenuClick(
            AbstractContainerMenu menu,
            int slotId,
            int button,
            ClickType clickType,
            Player player
    ) {
        if (!ItemSizeSettings.enabled()) {
            return;
        }
        RejectedInsertion rejected = findRejectedInsertion(menu, slotId, button, clickType, player);
        if (rejected != null) {
            notifyPlayer(player, rejected.stack());
        }
    }

    public static void notifyPlayer(Player player, ItemStack rejectedStack) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || rejectedStack.isEmpty()
                || (!configValue(ItemSizeConfig.REJECTION_ACTIONBAR_ENABLED)
                && !configValue(ItemSizeConfig.REJECTION_SOUND_ENABLED))) {
            return;
        }
        long gameTime = serverPlayer.level().getGameTime();
        CompoundTag playerData = serverPlayer.getPersistentData();
        int cooldownTicks = configValue(ItemSizeConfig.REJECTION_COOLDOWN_TICKS);
        if (playerData.contains(LAST_FEEDBACK_TICK)) {
            long elapsed = gameTime - playerData.getLong(LAST_FEEDBACK_TICK);
            if (elapsed >= 0L && elapsed < cooldownTicks) {
                return;
            }
        }
        playerData.putLong(LAST_FEEDBACK_TICK, gameTime);

        if (configValue(ItemSizeConfig.REJECTION_ACTIONBAR_ENABLED)) {
            serverPlayer.displayClientMessage(Component.translatable(
                    "message.revivalages.item_size.container_rejected",
                    rejectedStack.getHoverName()
            ), true);
        }
        if (configValue(ItemSizeConfig.REJECTION_SOUND_ENABLED)) {
            serverPlayer.playNotifySound(
                    SoundEvents.NOTE_BLOCK_BASS.value(),
                    SoundSource.PLAYERS,
                    configValue(ItemSizeConfig.REJECTION_SOUND_VOLUME).floatValue(),
                    configValue(ItemSizeConfig.REJECTION_SOUND_PITCH).floatValue()
            );
        }
    }

    private static RejectedInsertion findRejectedInsertion(
            AbstractContainerMenu menu,
            int slotId,
            int button,
            ClickType clickType,
            Player player
    ) {
        if (slotId < 0 || slotId >= menu.slots.size()) {
            return null;
        }
        Slot clicked = menu.getSlot(slotId);
        if (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_CRAFT) {
            ItemStack carried = menu.getCarried();
            return rejectedByChestSlot(clicked, carried) ? new RejectedInsertion(carried) : null;
        }
        if (clickType == ClickType.SWAP) {
            Inventory inventory = player.getInventory();
            if (button < 0 || button >= inventory.getContainerSize()) {
                return null;
            }
            ItemStack swapped = inventory.getItem(button);
            return rejectedByChestSlot(clicked, swapped) ? new RejectedInsertion(swapped) : null;
        }
        if (clickType == ClickType.QUICK_MOVE && !isChestContainer(clicked.container) && clicked.hasItem()) {
            for (Slot target : menu.slots) {
                if (rejectedByChestSlot(target, clicked.getItem())) {
                    return new RejectedInsertion(clicked.getItem());
                }
            }
        }
        return null;
    }

    private static boolean rejectedByChestSlot(Slot slot, ItemStack stack) {
        if (stack.isEmpty() || !isChestContainer(slot.container)) {
            return false;
        }
        if (slot.container instanceof ChestBlockEntity chest) {
            return !SizeApi.canInsert(chest.getBlockState(), stack);
        }
        return !slot.container.canPlaceItem(slot.getContainerSlot(), stack);
    }

    private static boolean isChestContainer(Container container) {
        return container instanceof ChestBlockEntity || container instanceof CompoundContainer;
    }

    private static <T> T configValue(net.neoforged.neoforge.common.ModConfigSpec.ConfigValue<T> value) {
        return ItemSizeConfig.SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    private record RejectedInsertion(ItemStack stack) {
    }
}
