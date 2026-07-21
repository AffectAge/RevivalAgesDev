package com.protyvkultury.revivalages.feature.technology.campfire.item;

import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.blockentity.CampfireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class TinderItem extends Item {

    public TinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
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
            if (context.getPlayer() == null || !context.getPlayer().hasInfiniteMaterials()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
