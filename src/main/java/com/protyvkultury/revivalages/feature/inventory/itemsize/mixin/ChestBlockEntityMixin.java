package com.protyvkultury.revivalages.feature.inventory.itemsize.mixin;

import com.protyvkultury.revivalages.api.size.SizeApi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds the missing vanilla insertion boundary used by both ChestMenu slots and
 * NeoForge's InvWrapper. Existing contents and extraction are intentionally
 * untouched.
 */
@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin {

    public boolean canPlaceItem(int slot, ItemStack stack) {
        ChestBlockEntity chest = (ChestBlockEntity) (Object) this;
        return SizeApi.canInsert(chest.getBlockState(), stack);
    }
}
