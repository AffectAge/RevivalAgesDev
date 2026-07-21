package com.protyvkultury.revivalages.core.interaction;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * NeoForge port of the click semantics supplied by Athenaeum's
 * {@code InteractionItemStack}: held items insert or operate, while an empty
 * hand extracts the visible stack.
 */
public final class ItemStackInteraction {

    private ItemStackInteraction() {
    }

    public static ItemInteractionResult insert(Level level, boolean accepted, Runnable serverAction) {
        if (!accepted) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            serverAction.run();
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    public static InteractionResult extract(
            Level level,
            BlockPos pos,
            Player player,
            ItemStack visibleStack,
            Supplier<ItemStack> serverExtraction
    ) {
        if (visibleStack.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            giveOrDrop(level, pos, player, serverExtraction.get());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static void giveOrDrop(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (!stack.isEmpty() && !player.addItem(stack)) {
            Block.popResource(level, pos, stack);
        }
    }
}
