package com.protyvkultury.revivalages.api.size;

import net.minecraft.world.item.ItemStack;

/**
 * Implemented by an item or the block behind a block item when its size depends
 * on stack state or must override data-pack definitions.
 */
public interface ItemSizeProvider {

    Size getSize(ItemStack stack);
}
