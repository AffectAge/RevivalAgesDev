package com.protyvkultury.revivalages.feature.technology.knapping.menu;

import com.protyvkultury.revivalages.feature.technology.knapping.KnappingConfig;
import com.protyvkultury.revivalages.feature.technology.knapping.KnappingFeature;
import com.protyvkultury.revivalages.feature.technology.knapping.KnappingLayout;
import com.protyvkultury.revivalages.feature.technology.knapping.KnappingType;
import com.protyvkultury.revivalages.feature.technology.knapping.network.KnappingStatePayload;
import com.protyvkultury.revivalages.feature.technology.knapping.recipe.KnappingRecipe;
import com.protyvkultury.revivalages.internal.menu.ItemBackedMenu;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class KnappingMenu extends ItemBackedMenu {

    public static final int ALL_CELLS = (1 << 25) - 1;

    private final ResourceLocation typeId;
    private final Container output = new SimpleContainer(1);
    private final ItemStack initialInput;
    private int cells = ALL_CELLS;
    private boolean hasBeenModified;
    private boolean hasConsumedIngredient;
    private final Queue<Integer> acceptedCells = new ArrayDeque<>();

    public KnappingMenu(
            int containerId,
            Inventory inventory,
            ResourceLocation typeId,
            int hotbarIndex
    ) {
        super(KnappingFeature.MENU.get(), containerId, inventory, hotbarIndex);
        this.typeId = typeId;
        this.initialInput = openingStack();
        addSlot(new ResultSlot(output, 0, KnappingLayout.OUTPUT_X, KnappingLayout.OUTPUT_Y));
        addPlayerInventory(inventory);
        bindPlayerInventorySlots(1);
    }

    public ResourceLocation typeId() {
        return typeId;
    }

    public ItemStack initialInput() {
        return initialInput.copy();
    }

    public int cells() {
        return cells;
    }

    public boolean cellOn(int index) {
        return index >= 0 && index < 25 && (cells & (1 << index)) != 0;
    }

    public void applyServerState(int updatedCells, int feedbackCell) {
        cells = updatedCells & ALL_CELLS;
        if (feedbackCell >= 0 && feedbackCell < 25) {
            acceptedCells.add(feedbackCell);
        }
    }

    public int consumeAcceptedCell() {
        return acceptedCells.isEmpty() ? -1 : acceptedCells.remove();
    }

    public void removeCell(ServerPlayer player, int index) {
        if (player.containerMenu != this || index < 0 || index >= 25 || !cellOn(index)) {
            sendState(player, -1);
            return;
        }
        KnappingType type = type(player).orElse(null);
        ItemStack target = targetStack();
        if (type == null || !validTarget(type, target)) {
            player.closeContainer();
            return;
        }
        if (!hasBeenModified) {
            if (!type.consumeAfterComplete() && !consume(player, target, type.amountToConsume())) {
                player.closeContainer();
                return;
            }
            hasBeenModified = true;
            if (!type.consumeAfterComplete()) {
                hasConsumedIngredient = true;
            }
        }
        cells &= ~(1 << index);
        updateResult(player);
        broadcastChanges();
        sendState(player, index);
    }

    @Override
    public boolean stillValid(Player player) {
        if (!KnappingConfig.enabled() || type(player).isEmpty()) {
            return false;
        }
        return !targetStack().isEmpty() || hasBeenModified && !type(player).orElseThrow().consumeAfterComplete();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || isTargetMenuSlot(index)) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        if (index != 0 || !moveItemStackTo(source, 1, slots.size(), true)) {
            return ItemStack.EMPTY;
        }
        slot.onQuickCraft(source, copy);
        slot.onTake(player, source);
        return copy;
    }

    @Override
    public void removed(Player player) {
        if (!player.level().isClientSide) {
            ItemStack completed = output.removeItemNoUpdate(0);
            if (!completed.isEmpty()) {
                player.getInventory().placeItemBackInInventory(completed);
                consumeAfterComplete(player);
            }
        }
        super.removed(player);
    }

    private boolean validTarget(KnappingType type, ItemStack target) {
        if (hasBeenModified && !type.consumeAfterComplete()) {
            return target.isEmpty() || ItemStack.isSameItemSameComponents(target, initialInput);
        }
        return type.input().test(target)
                && ItemStack.isSameItemSameComponents(target, initialInput);
    }

    private void updateResult(ServerPlayer player) {
        Optional<KnappingRecipe> match = player.level().getRecipeManager()
                .getAllRecipesFor(KnappingFeature.RECIPE_TYPE.get())
                .stream()
                .map(net.minecraft.world.item.crafting.RecipeHolder::value)
                .filter(recipe -> recipe.matches(typeId, initialInput, cells))
                .findFirst();
        output.setItem(0, match.map(KnappingRecipe::result).orElse(ItemStack.EMPTY));
    }

    private void resultTaken(ServerPlayer player) {
        consumeAfterComplete(player);
        cells = 0;
        output.setItem(0, ItemStack.EMPTY);
        broadcastChanges();
        sendState(player, -1);
    }

    private void consumeAfterComplete(Player player) {
        KnappingType type = type(player).orElse(null);
        if (type != null && type.consumeAfterComplete() && !hasConsumedIngredient) {
            consume(player, targetStack(), type.amountToConsume());
            hasConsumedIngredient = true;
        }
    }

    private Optional<KnappingType> type(Player player) {
        Registry<KnappingType> registry = player.registryAccess().registryOrThrow(KnappingFeature.KNAPPING_TYPES);
        return Optional.ofNullable(registry.get(typeId));
    }

    private static boolean consume(Player player, ItemStack stack, int count) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        if (stack.getCount() < count) {
            return false;
        }
        stack.shrink(count);
        return true;
    }

    private void sendState(ServerPlayer player, int feedbackCell) {
        PacketDistributor.sendToPlayer(
                player,
                new KnappingStatePayload(containerId, cells, feedbackCell)
        );
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        inventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        KnappingLayout.PLAYER_INVENTORY_Y + row * 18
                ));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, KnappingLayout.HOTBAR_Y));
        }
    }

    private final class ResultSlot extends Slot {

        private ResultSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            KnappingType type = type(player).orElse(null);
            return type != null
                    && (!type.consumeAfterComplete()
                    || hasConsumedIngredient
                    || player.getAbilities().instabuild
                    || targetStack().getCount() >= type.amountToConsume());
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            if (player instanceof ServerPlayer serverPlayer) {
                resultTaken(serverPlayer);
            }
        }
    }
}
