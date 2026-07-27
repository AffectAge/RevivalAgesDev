package com.protyvkultury.revivalages.feature.technology.barrel.storage;

import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
import com.protyvkultury.revivalages.feature.food.spoilage.FoodFreshnessService;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public final class StorageBarrelBlockEntity extends BaseContainerBlockEntity {

    private static final int MAX_SLOTS = 54;
    private NonNullList<ItemStack> items = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
    private final IItemHandler itemHandler = new GuardedItemHandler();

    public StorageBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(BarrelFeature.STORAGE_BARREL_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StorageBarrelBlockEntity barrel) {
        if (!ContentAvailability.isEnabled(ContentKey.STORAGE_BARREL)) {
            return;
        }
        int interval = PrimitiveTechnologyConfig.STORAGE_BARREL_MATERIALIZATION_INTERVAL.get();
        if (level.getGameTime() % interval != 0L) {
            return;
        }
        boolean changed = false;
        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            ItemStack before = barrel.items.get(slot);
            ItemStack after = FoodFreshnessApi.materialize(before);
            if (after != before) {
                barrel.items.set(slot, after);
                changed = true;
            }
        }
        if (changed) {
            barrel.setChanged();
        }
    }

    public boolean seal() {
        if (level == null || getBlockState().getValue(StorageBarrelBlock.SEALED)) {
            return false;
        }
        setPreserved(true);
        level.setBlock(worldPosition, getBlockState().setValue(StorageBarrelBlock.SEALED, true), Block.UPDATE_ALL);
        level.invalidateCapabilities(worldPosition);
        setChanged();
        return true;
    }

    public void unseal(Player player) {
        if (level == null || !getBlockState().getValue(StorageBarrelBlock.SEALED)) {
            return;
        }
        setPreserved(false);
        level.setBlock(worldPosition, getBlockState().setValue(StorageBarrelBlock.SEALED, false), Block.UPDATE_ALL);
        level.invalidateCapabilities(worldPosition);
        ItemStack lid = new ItemStack(BarrelFeature.BARREL_LID.get());
        if (!player.addItem(lid)) {
            Block.popResource(level, worldPosition.above(), lid);
        }
        setChanged();
    }

    private void setPreserved(boolean preserved) {
        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (preserved) {
                FoodFreshnessApi.applyTrait(stack, FoodFreshnessService.PRESERVED);
            } else {
                FoodFreshnessApi.removeTrait(stack, FoodFreshnessService.PRESERVED);
            }
        }
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            Block.popResource(level, worldPosition, removeItemNoUpdate(slot));
        }
        if (getBlockState().getValue(StorageBarrelBlock.SEALED)) {
            Block.popResource(level, worldPosition, new ItemStack(BarrelFeature.BARREL_LID.get()));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return ContentAvailability.isEnabled(ContentKey.STORAGE_BARREL)
                && !getBlockState().getValue(StorageBarrelBlock.SEALED)
                && super.stillValid(player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !getBlockState().getValue(StorageBarrelBlock.SEALED)
                && ContentAvailability.isEnabled(ContentKey.STORAGE_BARREL)
                && super.canPlaceItem(slot, stack);
    }

    public IItemHandler itemHandler() {
        return itemHandler;
    }

    @Override
    public int getContainerSize() {
        int configured = Math.max(
                9,
                Math.min(MAX_SLOTS, PrimitiveTechnologyConfig.STORAGE_BARREL_SLOTS.get() / 9 * 9)
        );
        for (int slot = MAX_SLOTS - 1; slot >= configured; slot--) {
            if (!items.get(slot).isEmpty()) {
                return Math.min(MAX_SLOTS, ((slot + 1 + 8) / 9) * 9);
            }
        }
        return configured;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> loaded) {
        items = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
        for (int slot = 0; slot < Math.min(MAX_SLOTS, loaded.size()); slot++) {
            items.set(slot, loaded.get(slot));
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.revivalages.storage_barrel");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        int rows = getContainerSize() / 9;
        MenuType<ChestMenu> type = switch (rows) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            case 6 -> MenuType.GENERIC_9x6;
            default -> MenuType.GENERIC_9x3;
        };
        return new ChestMenu(type, containerId, inventory, this, rows);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
    }

    private final class GuardedItemHandler implements IItemHandler {

        private final InvWrapper delegate = new InvWrapper(StorageBarrelBlockEntity.this);

        private boolean accessible() {
            return ContentAvailability.isEnabled(ContentKey.STORAGE_BARREL)
                    && PrimitiveTechnologyConfig.STORAGE_BARREL_AUTOMATION.get()
                    && !getBlockState().getValue(StorageBarrelBlock.SEALED);
        }

        @Override
        public int getSlots() {
            return delegate.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return accessible() ? delegate.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return accessible() ? delegate.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return accessible() ? delegate.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return delegate.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return accessible() && delegate.isItemValid(slot, stack);
        }
    }
}
