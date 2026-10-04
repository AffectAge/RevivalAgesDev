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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RockDepositBlock extends VariantSurfaceDepositBlock<RockVariation> {

    public static final MapCodec<RockDepositBlock> CODEC = simpleCodec(RockDepositBlock::new);
    public static final EnumProperty<RockVariation> VARIATION =
            EnumProperty.create("variation", RockVariation.class);
    private static final VoxelShape TINY_SHAPE = randomlyRotatedShape(box(6, 0, 6, 9, 1, 8));
    private static final VoxelShape SMALL_SHAPE = randomlyRotatedShape(Shapes.or(
            box(3, 0, 9, 5, 1, 13),
            box(11, 0, 4.5, 14, 1, 6.5)
    ));
    private static final VoxelShape MEDIUM_SHAPE = randomlyRotatedShape(Shapes.or(
            box(9, 0, 3, 12, 1, 7),
            box(2, 0, 2, 4, 1, 7),
            box(10, 0, 10.5, 13, 1, 12.5),
            box(6, 0, 10.5, 8, 1, 12.5)
    ));
    private static final VoxelShape LARGE_SHAPE = randomlyRotatedShape(Shapes.or(
            box(2, 0, 5, 6, 2, 12),
            box(9, 0, 11, 14, 1, 14),
            box(10, 0, 4, 15, 1, 6)
    ));

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
        return switch (state.getValue(VARIATION)) {
            case TINY -> TINY_SHAPE;
            case SMALL -> SMALL_SHAPE;
            case MEDIUM -> MEDIUM_SHAPE;
            case LARGE -> LARGE_SHAPE;
        };
    }
}
