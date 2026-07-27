package com.protyvkultury.revivalages.feature.technology.ignition.item;

import com.protyvkultury.revivalages.api.ignition.HeldIgnitableBlock;
import com.protyvkultury.revivalages.api.ignition.HeldIgniter;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyClientConfig;
import com.protyvkultury.revivalages.feature.technology.ignition.IgnitionFeature;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Slow, low-durability igniter with a held-use interaction. */
public final class FlintAndTinderItem extends Item implements HeldIgniter {

    private static final String TARGET_POS = "revivalages:ignition_target_pos";
    private static final String TARGET_DIMENSION = "revivalages:ignition_target_dimension";

    public FlintAndTinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!ContentAvailability.isEnabled(ContentKey.FLINT_AND_TINDER)) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        rememberTarget(level, player, hit);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return PrimitiveTechnologyConfig.FLINT_AND_TINDER_USE_TICKS.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (ContentAvailability.isEnabled(ContentKey.FLINT_AND_TINDER)
                && entity instanceof Player player) {
            BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            if (!matchesRememberedTarget(level, player, hit)) {
                player.stopUsingItem();
                clearTarget(player);
                return;
            }
            if (level.isClientSide) {
                level.addParticle(ParticleTypes.SMOKE,
                        hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                        0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!ContentAvailability.isEnabled(ContentKey.FLINT_AND_TINDER)
                || !(entity instanceof Player player)) {
            return stack;
        }
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (!matchesRememberedTarget(level, player, hit)) {
            clearTarget(player);
            return stack;
        }
        clearTarget(player);
        BlockPos pos = hit.getBlockPos();
        boolean ignited = false;
        if (!level.isClientSide) {
            if (level.getBlockState(pos).getBlock() instanceof HeldIgnitableBlock target) {
                ignited = target.igniteFromHeldItem(
                        level,
                        pos,
                        level.getBlockState(pos),
                        player,
                        hit.getDirection()
                );
            } else {
                BlockPos firePos = pos.relative(hit.getDirection());
                if (BaseFireBlock.canBePlacedAt(level, firePos, player.getDirection())) {
                    level.setBlock(firePos, BaseFireBlock.getState(level, firePos), 11);
                    ignited = true;
                }
            }
            if (ignited) {
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0F,
                        level.random.nextFloat() * 0.4F + 0.8F);
                if (!player.hasInfiniteMaterials()) {
                    consumeUse(stack);
                }
                player.getCooldowns().addCooldown(this, PrimitiveTechnologyConfig.FLINT_AND_TINDER_COOLDOWN_TICKS.get());
            }
        }
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player) {
            clearTarget(player);
        }
        super.releaseUsing(stack, level, entity, timeCharged);
    }

    private static void rememberTarget(Level level, Player player, BlockHitResult hit) {
        CompoundTag data = player.getPersistentData();
        data.putLong(TARGET_POS, hit.getBlockPos().asLong());
        data.putString(TARGET_DIMENSION, level.dimension().location().toString());
    }

    private static boolean matchesRememberedTarget(Level level, Player player, BlockHitResult hit) {
        if (hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        CompoundTag data = player.getPersistentData();
        ResourceLocation dimension = ResourceLocation.tryParse(data.getString(TARGET_DIMENSION));
        return data.contains(TARGET_POS)
                && level.dimension().location().equals(dimension)
                && BlockPos.of(data.getLong(TARGET_POS)).equals(hit.getBlockPos());
    }

    private static void clearTarget(Player player) {
        CompoundTag data = player.getPersistentData();
        data.remove(TARGET_POS);
        data.remove(TARGET_DIMENSION);
    }

    private static void consumeUse(ItemStack stack) {
        int maximum = PrimitiveTechnologyConfig.FLINT_AND_TINDER_MAX_USES.get();
        int remaining = stack.getOrDefault(IgnitionFeature.IGNITER_USES.get(), maximum) - 1;
        if (remaining <= 0) {
            stack.shrink(1);
        } else {
            stack.set(IgnitionFeature.IGNITER_USES.get(), remaining);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getOrDefault(IgnitionFeature.IGNITER_USES.get(),
                PrimitiveTechnologyConfig.FLINT_AND_TINDER_MAX_USES.get())
                < PrimitiveTechnologyConfig.FLINT_AND_TINDER_MAX_USES.get();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int maximum = PrimitiveTechnologyConfig.FLINT_AND_TINDER_MAX_USES.get();
        return Math.round(13.0F * stack.getOrDefault(IgnitionFeature.IGNITER_USES.get(), maximum)
                / Math.max(1, maximum));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int maximum = PrimitiveTechnologyConfig.FLINT_AND_TINDER_MAX_USES.get();
        float fraction = stack.getOrDefault(IgnitionFeature.IGNITER_USES.get(), maximum) / (float) Math.max(1, maximum);
        return Mth.hsvToRgb(fraction / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (!PrimitiveTechnologyClientConfig.SHOW_DURABILITY_TOOLTIPS.get()) {
            return;
        }
        int maximum = PrimitiveTechnologyConfig.FLINT_AND_TINDER_MAX_USES.get();
        int remaining = stack.getOrDefault(IgnitionFeature.IGNITER_USES.get(), maximum);
        tooltip.add(Component.translatable(
                        remaining == maximum
                                ? "tooltip.revivalages.durability.full"
                                : "tooltip.revivalages.durability",
                        remaining,
                        maximum
                )
                .withStyle(ChatFormatting.GRAY));
    }
}
