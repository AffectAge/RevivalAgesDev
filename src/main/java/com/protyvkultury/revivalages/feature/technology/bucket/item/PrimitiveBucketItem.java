package com.protyvkultury.revivalages.feature.technology.bucket.item;

import com.protyvkultury.revivalages.feature.technology.bucket.PrimitiveBucketFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Reusable universal bucket with material wear and heat damage. */
public final class PrimitiveBucketItem extends Item {

    public enum Material {
        WOODEN,
        CLAY
    }

    private static final int CAPACITY = 1000;
    private final Material material;

    public PrimitiveBucketItem(Material material, Properties properties) {
        super(properties);
        this.material = material;
    }

    public int maximumUses() {
        return material == Material.WOODEN
                ? PrimitiveTechnologyConfig.WOODEN_BUCKET_MAX_USES.get()
                : PrimitiveTechnologyConfig.CLAY_BUCKET_MAX_USES.get();
    }

    public net.neoforged.neoforge.fluids.capability.IFluidHandlerItem createHandler(ItemStack stack) {
        return new PrimitiveBucketFluidHandler(PrimitiveBucketFeature.BUCKET_FLUID, stack, CAPACITY, maximumUses());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        FluidStack heldFluid = held.getOrDefault(PrimitiveBucketFeature.BUCKET_FLUID.get(), SimpleFluidContent.EMPTY).copy();
        if (!heldFluid.isEmpty() && heldFluid.is(NeoForgeMod.MILK.value())) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(held);
        }
        BlockHitResult hit = getPlayerPOVHitResult(level, player,
                heldFluid.isEmpty() ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(held);
        }
        BlockPos pos = hit.getBlockPos();
        FluidStack contained = heldFluid;
        if (contained.isEmpty()) {
            ItemStack single = held.copyWithCount(1);
            FluidActionResult result = FluidUtil.tryPickUpFluid(single, player, level, pos, hit.getDirection());
            if (!result.isSuccess()) {
                return InteractionResultHolder.pass(held);
            }
            if (!level.isClientSide) {
                replaceOne(player, hand, held, result.getResult());
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
        }

        BlockState target = level.getBlockState(pos);
        BlockPos placement = target.canBeReplaced() ? pos : pos.relative(hit.getDirection());
        FluidActionResult result = FluidUtil.tryPlaceFluid(player, level, hand, placement, held, contained);
        if (!result.isSuccess()) {
            return InteractionResultHolder.fail(held);
        }
        if (!level.isClientSide) {
            player.setItemInHand(hand, result.getResult());
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        FluidStack fluid = stack.getOrDefault(PrimitiveBucketFeature.BUCKET_FLUID.get(), SimpleFluidContent.EMPTY).copy();
        return !fluid.isEmpty() && fluid.is(NeoForgeMod.MILK.value()) ? UseAnim.DRINK : UseAnim.NONE;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        FluidStack fluid = stack.getOrDefault(PrimitiveBucketFeature.BUCKET_FLUID.get(), SimpleFluidContent.EMPTY).copy();
        if (!fluid.isEmpty() && fluid.is(NeoForgeMod.MILK.value())) {
            if (!level.isClientSide) {
                entity.removeAllEffects();
                createHandler(stack).drain(1000, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        return stack;
    }

    private static void replaceOne(Player player, InteractionHand hand, ItemStack original, ItemStack result) {
        if (original.getCount() == 1) {
            player.setItemInHand(hand, result);
            return;
        }
        original.shrink(1);
        if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || level.getGameTime() % 20L != 0L || !(entity instanceof LivingEntity living)) {
            return;
        }
        FluidStack fluid = stack.getOrDefault(PrimitiveBucketFeature.BUCKET_FLUID.get(), SimpleFluidContent.EMPTY).copy();
        if (fluid.isEmpty()) {
            return;
        }
        boolean hot = fluid.getFluidType().getTemperature(fluid) >= PrimitiveTechnologyConfig.HOT_FLUID_TEMPERATURE.get();
        int wear = material == Material.WOODEN
                ? PrimitiveTechnologyConfig.WOODEN_BUCKET_FULL_DAMAGE_PER_SECOND.get()
                : 0;
        if (hot) {
            wear += material == Material.WOODEN
                    ? PrimitiveTechnologyConfig.WOODEN_BUCKET_HOT_DAMAGE_PER_SECOND.get()
                    : PrimitiveTechnologyConfig.CLAY_BUCKET_HOT_DAMAGE_PER_SECOND.get();
            double damage = material == Material.WOODEN
                    ? PrimitiveTechnologyConfig.WOODEN_BUCKET_PLAYER_DAMAGE_PER_SECOND.get()
                    : PrimitiveTechnologyConfig.CLAY_BUCKET_PLAYER_DAMAGE_PER_SECOND.get();
            if (damage > 0.0D) {
                living.hurt(level.damageSources().onFire(), (float) damage);
            }
        }
        if (wear > 0) {
            damageContainer(stack, level, living, fluid, wear);
        }
    }

    private void damageContainer(ItemStack stack, Level level, LivingEntity holder, FluidStack fluid, int amount) {
        int uses = stack.getOrDefault(PrimitiveBucketFeature.BUCKET_USES.get(), maximumUses()) - amount;
        if (uses > 0) {
            stack.set(PrimitiveBucketFeature.BUCKET_USES.get(), uses);
            return;
        }
        if (PrimitiveTechnologyConfig.PRIMITIVE_BUCKET_DROP_FLUID_ON_BREAK.get()) {
            BlockPos pos = holder.blockPosition();
            BlockState current = level.getBlockState(pos);
            if (current.canBeReplaced()
                    && !fluid.is(NeoForgeMod.MILK.value())
                    && fluid.getFluidType().canBePlacedInLevel(level, pos, fluid)) {
                level.setBlock(pos, fluid.getFluidType().getBlockForFluidState(
                        level, pos, fluid.getFluid().defaultFluidState()), 11);
            }
        }
        stack.shrink(1);
        level.playSound(null, holder.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getOrDefault(PrimitiveBucketFeature.BUCKET_USES.get(), maximumUses()) < maximumUses();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int uses = stack.getOrDefault(PrimitiveBucketFeature.BUCKET_USES.get(), maximumUses());
        return Math.round(13.0F * uses / Math.max(1, maximumUses()));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float fraction = stack.getOrDefault(PrimitiveBucketFeature.BUCKET_USES.get(), maximumUses())
                / (float) Math.max(1, maximumUses());
        return Mth.hsvToRgb(fraction / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltip, flag);
        FluidStack fluid = stack.getOrDefault(PrimitiveBucketFeature.BUCKET_FLUID.get(), SimpleFluidContent.EMPTY).copy();
        if (!fluid.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.revivalages.primitive_bucket.fluid", fluid.getHoverName())
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable(
                        "tooltip.revivalages.primitive_bucket.uses",
                        stack.getOrDefault(PrimitiveBucketFeature.BUCKET_USES.get(), maximumUses()), maximumUses())
                .withStyle(ChatFormatting.GRAY));
    }
}
