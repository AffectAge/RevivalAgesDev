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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class StickDepositBlock extends VariantSurfaceDepositBlock<StickVariation> {

    public static final MapCodec<StickDepositBlock> CODEC = simpleCodec(StickDepositBlock::new);
    public static final EnumProperty<StickVariation> VARIATION =
            EnumProperty.create("variation", StickVariation.class);
    private static final VoxelShape SMALL_SHAPE = randomlyRotatedShape(box(2, 0, 7, 12, 1, 8));
    private static final VoxelShape MEDIUM_SHAPE = randomlyRotatedShape(Shapes.or(
            box(2, 0, 3, 3, 1, 14),
            box(5, 0.01, 6, 13, 1.01, 7)
    ));
    private static final VoxelShape LARGE_SHAPE = randomlyRotatedShape(Shapes.or(
            box(2, 0, 1, 3, 1, 15),
            box(10, 0.01, 2, 11, 1.01, 10),
            box(5, 0.02, 11, 12, 1.02, 12)
    ));

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
        return switch (state.getValue(VARIATION)) {
            case SMALL -> SMALL_SHAPE;
            case MEDIUM -> MEDIUM_SHAPE;
            case LARGE -> LARGE_SHAPE;
        };
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true;
    }
}
