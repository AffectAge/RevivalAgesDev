package com.protyvkultury.revivalages.feature.technology.campfire.item;

import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.blockentity.CampfireBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class TinderItem extends Item {

    public TinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.revivalages.tinder.place")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!ContentAvailability.isEnabled(ContentKey.CAMPFIRE)) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
        BlockState support = level.getBlockState(placePos.below());
        if (!level.getBlockState(placePos).canBeReplaced()
                || !support.isFaceSturdy(level, placePos.below(), net.minecraft.core.Direction.UP)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.setBlock(placePos, CampfireFeature.CAMPFIRE.get().defaultBlockState(), 3);
            if (level.getBlockEntity(placePos) instanceof CampfireBlockEntity campfire) {
                campfire.setHasTinder(true);
            }
            level.playSound(
                    null,
                    placePos,
                    SoundEvents.GRASS_PLACE,
                    SoundSource.PLAYERS,
                    1.0F,
                    (float) (1.0D + level.random.nextGaussian() * 0.4D));
            if (context.getPlayer() == null || !context.getPlayer().hasInfiniteMaterials()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
