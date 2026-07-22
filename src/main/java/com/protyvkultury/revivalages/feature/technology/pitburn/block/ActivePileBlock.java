package com.protyvkultury.revivalages.feature.technology.pitburn.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.feature.technology.pitburn.PitBurnFeature;
import com.protyvkultury.revivalages.feature.technology.pitburn.blockentity.PitBurnBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;

public final class ActivePileBlock extends BaseEntityBlock {

    public static final MapCodec<ActivePileBlock> CODEC = simpleCodec(ActivePileBlock::new);

    public ActivePileBlock(BlockBehaviour.Properties properties) {
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type
    ) {
        return level.isClientSide
                ? null
                : createTickerHelper(type, PitBurnFeature.BLOCK_ENTITY.get(), PitBurnBlockEntity::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.getX() + 0.2D + random.nextDouble() * 0.6D,
                    pos.getY() + 1.0D,
                    pos.getZ() + 0.2D + random.nextDouble() * 0.6D,
                    0.0D, 0.035D, 0.0D);
        }
    }

    @Override
    protected void neighborChanged(
            BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
            BlockPos neighborPos, boolean movedByPiston
    ) {
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PitBurnBlockEntity burn) {
            burn.requireStructureValidation();
        }
    }
}
