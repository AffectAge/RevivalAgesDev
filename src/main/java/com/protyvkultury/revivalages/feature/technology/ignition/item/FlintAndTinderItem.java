package com.protyvkultury.revivalages.feature.technology.ignition.item;

import com.protyvkultury.revivalages.feature.technology.campfire.blockentity.CampfireBlockEntity;
import com.protyvkultury.revivalages.feature.technology.ignition.blockentity.WoodTorchBlockEntity;
import com.protyvkultury.revivalages.feature.technology.pitburn.PitBurnFeature;
import com.protyvkultury.revivalages.feature.technology.pitkiln.blockentity.PitKilnBlockEntity;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.ignition.IgnitionFeature;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Slow, low-durability igniter with a held-use interaction. */
public final class FlintAndTinderItem extends Item {

    public FlintAndTinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
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
        if (level.isClientSide && remainingUseDuration % 4 == 0 && entity instanceof Player player) {
            BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            if (hit.getType() == HitResult.Type.BLOCK) {
                level.addParticle(ParticleTypes.SMOKE,
                        hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                        0.0D, 0.01D, 0.0D);
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return stack;
        }
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return stack;
        }
        BlockPos pos = hit.getBlockPos();
        boolean ignited = false;
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire && campfire.canIgnite()) {
                campfire.ignite();
                ignited = true;
            } else if (level.getBlockEntity(pos) instanceof PitKilnBlockEntity kiln && kiln.canIgnite()) {
                kiln.ignite();
                ignited = true;
            } else if (level.getBlockEntity(pos) instanceof WoodTorchBlockEntity torch) {
                ignited = torch.ignite();
            } else if (level.getBlockState(pos).is(PitBurnFeature.LOG_PILE.get())) {
                ignited = PitBurnFeature.ignite(level, pos);
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
}
