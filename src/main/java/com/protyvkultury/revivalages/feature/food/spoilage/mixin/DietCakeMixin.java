package com.protyvkultury.revivalages.feature.food.spoilage.mixin;

import com.protyvkultury.revivalages.feature.player.diet.DietFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CakeBlock.class)
public abstract class DietCakeMixin {

    @Inject(method = "eat", at = @At("RETURN"))
    private static void revivalages$recordConsumedSlice(
            LevelAccessor level,
            BlockPos pos,
            BlockState state,
            Player player,
            CallbackInfoReturnable<InteractionResult> callback
    ) {
        if (callback.getReturnValue() == InteractionResult.SUCCESS && player instanceof ServerPlayer serverPlayer) {
            DietFeature.recordCakeSlice(serverPlayer);
        }
    }
}
