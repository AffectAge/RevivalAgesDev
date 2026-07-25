package com.protyvkultury.revivalages.internal.menu;

import java.util.EnumSet;
import java.util.Set;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public abstract class ItemBackedMenu extends AbstractContainerMenu {

    private static final Set<ClickType> ILLEGAL_TARGET_CLICKS =
            EnumSet.of(ClickType.QUICK_MOVE, ClickType.PICKUP, ClickType.THROW, ClickType.SWAP);

    protected final Inventory playerInventory;
    private final int hotbarIndex;
    private final ItemStack openingStack;
    private int targetMenuSlot = -1;

    protected ItemBackedMenu(MenuType<?> type, int containerId, Inventory inventory, int hotbarIndex) {
        super(type, containerId);
        if (hotbarIndex < 0 || hotbarIndex >= Inventory.getSelectionSize()) {
            throw new IllegalArgumentException("Item-backed menu hotbar index must be between 0 and 8");
        }
        this.playerInventory = inventory;
        this.hotbarIndex = hotbarIndex;
        this.openingStack = inventory.getItem(hotbarIndex).copy();
    }

    protected final void bindPlayerInventorySlots(int firstPlayerSlot) {
        targetMenuSlot = firstPlayerSlot + 27 + hotbarIndex;
    }

    protected final ItemStack targetStack() {
        if (targetMenuSlot < 0 || targetMenuSlot >= slots.size()) {
            return ItemStack.EMPTY;
        }
        return slots.get(targetMenuSlot).getItem();
    }

    protected final ItemStack openingStack() {
        return openingStack.copy();
    }

    protected final int hotbarIndex() {
        return hotbarIndex;
    }

    protected final boolean isTargetMenuSlot(int slotIndex) {
        return slotIndex == targetMenuSlot;
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType clickType, Player player) {
        if ((isTargetMenuSlot(slotIndex) && ILLEGAL_TARGET_CLICKS.contains(clickType))
                || (button == hotbarIndex && clickType == ClickType.SWAP)
                || ((button == 40 || button >= 0 && button <= 9) && clickType == ClickType.SWAP)
                || clickType == ClickType.PICKUP_ALL) {
            return;
        }
        super.clicked(slotIndex, button, clickType, player);
    }
}
