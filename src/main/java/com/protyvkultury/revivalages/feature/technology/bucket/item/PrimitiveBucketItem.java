package com.protyvkultury.revivalages.feature.technology.bucket.item;

import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.bucket.PrimitiveBucketFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
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
import net.minecraft.world.item.crafting.RecipeType;
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
import net.neoforged.neoforge.common.EffectCures;

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
                ? PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.WOODEN_BUCKET_MAX_USES)
                : PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.CLAY_BUCKET_MAX_USES);
    }

    public int emptyStackSize() {
        return material == Material.WOODEN
                ? PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.WOODEN_BUCKET_STACK_SIZE)
                : PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.CLAY_BUCKET_STACK_SIZE);
    }

    public int hotTemperature() {
        return material == Material.WOODEN
                ? PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.WOODEN_BUCKET_HOT_TEMPERATURE)
                : PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.CLAY_BUCKET_HOT_TEMPERATURE);
    }

    public boolean milkEnabled() {
        return material == Material.WOODEN
                ? PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.WOODEN_BUCKET_MILK_ENABLED)
                : PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.CLAY_BUCKET_MILK_ENABLED);
    }

    private boolean dropsFluidWhenBroken() {
        return material == Material.WOODEN
                ? PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.WOODEN_BUCKET_DROP_FLUID_ON_BREAK)
                : PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.CLAY_BUCKET_DROP_FLUID_ON_BREAK);
    }

    private int hotWearPerSecond() {
        return material == Material.WOODEN
                ? PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.WOODEN_BUCKET_HOT_DAMAGE_PER_SECOND)
                : PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.CLAY_BUCKET_HOT_DAMAGE_PER_SECOND);
    }

    private int fullWearPerSecond() {
        return material == Material.WOODEN
                ? PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.WOODEN_BUCKET_FULL_DAMAGE_PER_SECOND)
                : PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.CLAY_BUCKET_FULL_DAMAGE_PER_SECOND);
    }

    public ContentKey contentKey() {
        return material == Material.WOODEN ? ContentKey.WOODEN_BUCKET : ContentKey.CLAY_BUCKET;
    }

    public net.neoforged.neoforge.fluids.capability.IFluidHandlerItem createHandler(ItemStack stack) {
        return new PrimitiveBucketFluidHandler(
                PrimitiveBucketFeature.BUCKET_FLUID,
                stack,
                CAPACITY,
                maximumUses(),
                emptyStackSize()
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!ContentAvailability.isEnabled(contentKey())) {
            return InteractionResultHolder.fail(held);
        }
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
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, hit.getDirection(), held)) {
            return InteractionResultHolder.fail(held);
        }
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
        if (!level.mayInteract(player, placement)
                || !player.mayUseItemAt(placement, hit.getDirection(), held)) {
            return InteractionResultHolder.fail(held);
        }
        FluidActionResult result = FluidUtil.tryPlaceFluid(player, level, hand, placement, held, contained);
        if (!result.isSuccess()) {
            return InteractionResultHolder.fail(held);
        }
        if (!level.isClientSide) {
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, result.getResult());
                if (result.getResult().isEmpty()) {
                    playBreakSound(level, player.blockPosition());
                }
            }
            player.awardStat(Stats.ITEM_USED.get(this));
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
        if (!ContentAvailability.isEnabled(contentKey())) {
            return stack;
        }
        FluidStack fluid = stack.getOrDefault(PrimitiveBucketFeature.BUCKET_FLUID.get(), SimpleFluidContent.EMPTY).copy();
        if (!fluid.isEmpty() && fluid.is(NeoForgeMod.MILK.value())) {
            if (entity instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
                serverPlayer.awardStat(Stats.ITEM_USED.get(this));
            }
            if (!level.isClientSide) {
                entity.removeEffectsCuredBy(EffectCures.MILK);
            }
            if (entity instanceof Player player && player.getAbilities().instabuild) {
                return stack;
            }
            ItemStack result = emptyAfterUse(stack);
            if (result.isEmpty() && !level.isClientSide) {
                playBreakSound(level, entity.blockPosition());
            }
            return result;
        }
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        FluidStack fluid = fluid(stack);
        return fluid.isEmpty()
                ? super.getName(stack)
                : Component.translatable(
                        "item.revivalages.primitive_bucket.filled",
                        super.getName(stack),
                        fluid.getHoverName()
                );
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return fluid(stack).isEmpty() ? emptyStackSize() : 1;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return !fluid(stack).isEmpty();
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return hasCraftingRemainingItem(stack) ? emptyAfterUse(stack) : ItemStack.EMPTY;
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return fluid(stack).is(FluidTags.LAVA)
                ? PrimitiveTechnologyConfig.effective(PrimitiveTechnologyConfig.PRIMITIVE_BUCKET_LAVA_BURN_TIME)
                : 0;
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
        if (!ContentAvailability.isEnabled(contentKey())
                || level.isClientSide
                || level.getGameTime() % 20L != 0L
                || !(entity instanceof LivingEntity living)) {
            return;
        }
        FluidStack fluid = fluid(stack);
        if (fluid.isEmpty()) {
            return;
        }
        boolean hot = isHot(fluid);
        int wear = fullWearPerSecond();
        if (hot) {
            wear += hotWearPerSecond();
            double damage = material == Material.WOODEN
                    ? PrimitiveTechnologyConfig.effective(
                            PrimitiveTechnologyConfig.WOODEN_BUCKET_PLAYER_DAMAGE_PER_SECOND)
                    : PrimitiveTechnologyConfig.effective(
                            PrimitiveTechnologyConfig.CLAY_BUCKET_PLAYER_DAMAGE_PER_SECOND);
            if (damage > 0.0D) {
                living.hurt(level.damageSources().onFire(), (float) damage);
                living.setRemainingFireTicks(Math.max(living.getRemainingFireTicks(), 20));
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
        if (dropsFluidWhenBroken()) {
            BlockPos pos = holder.blockPosition();
            if (!fluid.is(NeoForgeMod.MILK.value())
                    && fluid.getFluidType().canBePlacedInLevel(level, pos, fluid)) {
                FluidUtil.tryPlaceFluid(
                        holder instanceof Player player ? player : null,
                        level,
                        InteractionHand.MAIN_HAND,
                        pos,
                        stack.copyWithCount(1),
                        fluid
                );
            }
        }
        stack.shrink(1);
        playBreakSound(level, holder.blockPosition());
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
        int uses = stack.getOrDefault(PrimitiveBucketFeature.BUCKET_USES.get(), maximumUses());
        tooltip.add(Component.translatable(
                        uses == maximumUses()
                                ? "tooltip.revivalages.primitive_bucket.uses.full"
                                : "tooltip.revivalages.primitive_bucket.uses",
                        uses,
                        maximumUses())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
                        "tooltip.revivalages.primitive_bucket.hot_fluids." + (hotWearPerSecond() <= 0))
                .withStyle(hotWearPerSecond() <= 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    public FluidStack fluid(ItemStack stack) {
        return stack.getOrDefault(
                PrimitiveBucketFeature.BUCKET_FLUID.get(),
                SimpleFluidContent.EMPTY
        ).copy();
    }

    public ItemStack emptyAfterUse(ItemStack stack) {
        ItemStack result = stack.copyWithCount(1);
        result.remove(PrimitiveBucketFeature.BUCKET_FLUID.get());
        result.set(DataComponents.MAX_STACK_SIZE, emptyStackSize());
        int uses = stack.getOrDefault(PrimitiveBucketFeature.BUCKET_USES.get(), maximumUses()) - 1;
        if (uses <= 0) {
            return ItemStack.EMPTY;
        }
        result.set(PrimitiveBucketFeature.BUCKET_USES.get(), uses);
        return result;
    }

    public ItemStack filledWith(ItemStack empty, FluidStack fluid) {
        ItemStack result = empty.copyWithCount(1);
        result.set(PrimitiveBucketFeature.BUCKET_FLUID.get(), SimpleFluidContent.copyOf(fluid));
        result.set(DataComponents.MAX_STACK_SIZE, 1);
        return result;
    }

    public boolean isHot(FluidStack fluid) {
        return !fluid.isEmpty() && fluid.getFluidType().getTemperature(fluid) >= hotTemperature();
    }

    private static void playBreakSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);
    }
}
