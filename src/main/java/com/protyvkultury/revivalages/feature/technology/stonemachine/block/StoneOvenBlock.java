package com.protyvkultury.revivalages.feature.technology.stonemachine.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineKind;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class StoneOvenBlock extends StoneMachineBlock {

    public static final MapCodec<StoneOvenBlock> CODEC = simpleCodec(StoneOvenBlock::new);

    public StoneOvenBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public StoneMachineKind kind() {
        return StoneMachineKind.OVEN;
    }

    @Override
    protected MapCodec<StoneOvenBlock> codec() {
        return CODEC;
    }
}
