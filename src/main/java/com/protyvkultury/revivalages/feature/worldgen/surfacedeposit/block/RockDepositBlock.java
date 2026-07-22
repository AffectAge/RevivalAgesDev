package com.protyvkultury.revivalages.feature.worldgen.surfacedeposit.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RockDepositBlock extends VariantSurfaceDepositBlock<RockVariation> {

    public static final MapCodec<RockDepositBlock> CODEC = simpleCodec(RockDepositBlock::new);
    public static final EnumProperty<RockVariation> VARIATION =
            EnumProperty.create("variation", RockVariation.class);
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 3, 16);

    public RockDepositBlock(BlockBehaviour.Properties properties) {
        super(properties, VARIATION, RockVariation.TINY, RockVariation.values());
    }

    @Override
    protected MapCodec<? extends RockDepositBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIATION, WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }
}
