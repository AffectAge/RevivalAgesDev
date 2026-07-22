package com.protyvkultury.revivalages.feature.technology.pitburn.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.feature.technology.pitburn.blockentity.PitBurnBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class AshPileBlock extends BaseEntityBlock {

    public static final MapCodec<AshPileBlock> CODEC = simpleCodec(AshPileBlock::new);

    public AshPileBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PitBurnBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PitBurnBlockEntity burn) {
            Containers.dropContents(level, pos, burn.drops());
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
