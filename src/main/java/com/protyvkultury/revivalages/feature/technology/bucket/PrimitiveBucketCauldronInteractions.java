package com.protyvkultury.revivalages.feature.technology.bucket;

import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.technology.bucket.item.PrimitiveBucketItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

final class PrimitiveBucketCauldronInteractions {

    private PrimitiveBucketCauldronInteractions() {
    }

    static void register() {
        register(PrimitiveBucketFeature.WOODEN_BUCKET.get());
        register(PrimitiveBucketFeature.CLAY_BUCKET.get());
    }

    private static void register(PrimitiveBucketItem bucket) {
        CauldronInteraction.EMPTY.map().put(bucket, PrimitiveBucketCauldronInteractions::interact);
        CauldronInteraction.WATER.map().put(bucket, PrimitiveBucketCauldronInteractions::interact);
    }

    private static ItemInteractionResult interact(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack
    ) {
        if (!(stack.getItem() instanceof PrimitiveBucketItem bucket)
                || !ContentAvailability.isEnabled(bucket.contentKey())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, Direction.UP, stack)) {
            return ItemInteractionResult.FAIL;
        }

        FluidStack fluid = stack.getOrDefault(
                PrimitiveBucketFeature.BUCKET_FLUID.get(),
                SimpleFluidContent.EMPTY
        ).copy();
        if (fluid.is(FluidTags.WATER)) {
            return fillCauldron(state, level, pos, player, hand, stack, bucket);
        }
        if (fluid.isEmpty()) {
            return drainFullCauldron(state, level, pos, player, hand, stack, bucket);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static ItemInteractionResult fillCauldron(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            PrimitiveBucketItem bucket
    ) {
        int levelValue = state.is(Blocks.WATER_CAULDRON)
                ? state.getValue(LayeredCauldronBlock.LEVEL)
                : 0;
        if (!state.is(Blocks.CAULDRON) && !state.is(Blocks.WATER_CAULDRON) || levelValue >= 3) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            if (!player.getAbilities().instabuild) {
                ItemStack empty = bucket.emptyAfterUse(stack);
                player.setItemInHand(hand, empty);
                if (empty.isEmpty()) {
                    level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);
                }
            }
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(bucket));
            level.setBlockAndUpdate(
                    pos,
                    Blocks.WATER_CAULDRON.defaultBlockState()
                            .setValue(LayeredCauldronBlock.LEVEL, 3)
            );
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private static ItemInteractionResult drainFullCauldron(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            PrimitiveBucketItem bucket
    ) {
        if (!state.is(Blocks.WATER_CAULDRON)
                || state.getValue(LayeredCauldronBlock.LEVEL) != 3) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            if (!player.getAbilities().instabuild) {
                ItemStack filled = bucket.filledWith(
                        stack,
                        new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000)
                );
                if (stack.getCount() == 1) {
                    player.setItemInHand(hand, filled);
                } else {
                    stack.shrink(1);
                    if (!player.getInventory().add(filled)) {
                        player.drop(filled, false);
                    }
                }
            }
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(bucket));
            level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
