package com.protyvkultury.revivalages.core.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Spawns completed recipe results above a device, independent of block-drop rules. */
public final class RecipeOutputDrops {

    private RecipeOutputDrops() {
    }

    /** Spawns stack-sized results above the block on the server, even when block drops are disabled. */
    public static void spawnAbove(Level level, BlockPos pos, ItemStack result) {
        if (!(level instanceof ServerLevel) || result.isEmpty()) {
            return;
        }
        ItemStack remaining = result.copy();
        while (!remaining.isEmpty()) {
            ItemStack part = remaining.split(Math.min(remaining.getCount(), remaining.getMaxStackSize()));
            ItemEntity drop = new ItemEntity(
                    level,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.65D,
                    pos.getZ() + 0.5D,
                    part
            );
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
        }
    }
}
