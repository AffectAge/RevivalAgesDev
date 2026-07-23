package com.protyvkultury.revivalages.core.interaction;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Shared NeoForge implementation of item-stack click semantics: held items
 * insert or operate, while an empty
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
            playExtractionSound(level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Preserves the shared audible feedback for a player taking a visible stack
     * from an interaction handler.
     */
    public static void playExtractionSound(Level level, BlockPos pos) {
        level.playSound(
                null,
                pos,
                SoundEvents.ITEM_PICKUP,
                SoundSource.BLOCKS,
                0.25F,
                (float) (1.0D + level.random.nextGaussian() * 0.4D));
    }

    public static void giveOrDrop(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (!stack.isEmpty() && !player.addItem(stack)) {
            Block.popResource(level, pos, stack);
        }
    }
}
