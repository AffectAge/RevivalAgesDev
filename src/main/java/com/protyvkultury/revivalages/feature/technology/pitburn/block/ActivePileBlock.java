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
        double x = pos.getX() + 0.5D;
        double z = pos.getZ() + 0.5D;
        for (int index = 0; index < 4; index++) {
            level.addParticle(
                    ParticleTypes.SMOKE,
                    x + random.nextDouble() - 0.5D,
                    pos.getY() + (index < 2 ? 2.0D : 1.0D),
                    z + random.nextDouble() - 0.5D,
                    0.0D,
                    (index & 1) == 0 ? 0.1D : 0.15D,
                    0.0D
            );
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
