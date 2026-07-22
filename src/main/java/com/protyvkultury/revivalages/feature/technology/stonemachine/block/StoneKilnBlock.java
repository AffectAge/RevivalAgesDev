package com.protyvkultury.revivalages.feature.technology.stonemachine.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineKind;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class StoneKilnBlock extends StoneMachineBlock {

    public static final MapCodec<StoneKilnBlock> CODEC = simpleCodec(StoneKilnBlock::new);

    public StoneKilnBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public StoneMachineKind kind() {
        return StoneMachineKind.KILN;
    }

    @Override
    protected MapCodec<StoneKilnBlock> codec() {
        return CODEC;
    }
}
