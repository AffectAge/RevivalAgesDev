package com.protyvkultury.revivalages.feature.technology.stonemachine.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineKind;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class StoneSawmillBlock extends StoneMachineBlock {

    public static final MapCodec<StoneSawmillBlock> CODEC = simpleCodec(StoneSawmillBlock::new);

    public StoneSawmillBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public StoneMachineKind kind() {
        return StoneMachineKind.SAWMILL;
    }

    @Override
    protected MapCodec<StoneSawmillBlock> codec() {
        return CODEC;
    }
}
