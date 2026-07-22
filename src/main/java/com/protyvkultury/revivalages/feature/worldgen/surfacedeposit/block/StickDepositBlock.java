package com.protyvkultury.revivalages.feature.worldgen.surfacedeposit.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class StickDepositBlock extends VariantSurfaceDepositBlock<StickVariation> {

    public static final MapCodec<StickDepositBlock> CODEC = simpleCodec(StickDepositBlock::new);
    public static final EnumProperty<StickVariation> VARIATION =
            EnumProperty.create("variation", StickVariation.class);
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 1, 16);

    public StickDepositBlock(BlockBehaviour.Properties properties) {
        super(properties, VARIATION, StickVariation.SMALL, StickVariation.values());
    }

    @Override
    protected MapCodec<? extends StickDepositBlock> codec() {
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

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true;
    }
}
