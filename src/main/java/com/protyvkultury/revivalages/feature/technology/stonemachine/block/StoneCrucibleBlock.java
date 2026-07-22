package com.protyvkultury.revivalages.feature.technology.stonemachine.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineKind;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class StoneCrucibleBlock extends StoneMachineBlock {

    public static final MapCodec<StoneCrucibleBlock> CODEC = simpleCodec(StoneCrucibleBlock::new);

    public StoneCrucibleBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public StoneMachineKind kind() {
        return StoneMachineKind.CRUCIBLE;
    }

    @Override
    protected MapCodec<StoneCrucibleBlock> codec() {
        return CODEC;
    }
}
