package com.protyvkultury.revivalages.integration.kubejs;

import com.protyvkultury.revivalages.api.size.SizeApi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Read-only KubeJS boundary. Data-pack JSON remains the only mutation path for
 * item-size definitions and container policies.
 */
public enum ItemSizeScriptBindings {
    INSTANCE;

    public String sizeOf(ItemStack stack) {
        return SizeApi.getSize(stack).getSerializedName();
    }

    public boolean canInsertIntoBlock(BlockState container, ItemStack stack) {
        return SizeApi.canInsert(container, stack);
    }

    public boolean canInsertIntoItem(ItemStack container, ItemStack stack) {
        return SizeApi.canInsert(container, stack);
    }
}
