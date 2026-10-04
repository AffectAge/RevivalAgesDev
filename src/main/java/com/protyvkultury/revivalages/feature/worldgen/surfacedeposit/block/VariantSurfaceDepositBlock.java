package com.protyvkultury.revivalages.feature.worldgen.surfacedeposit.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Shared 1.21.1 implementation of variant cycling and the support lifecycle. */
abstract class VariantSurfaceDepositBlock<T extends Enum<T> & StringRepresentable>
        extends Block implements SimpleWaterloggedBlock {

    static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final EnumProperty<T> variationProperty;
    private final T[] variations;

    VariantSurfaceDepositBlock(
            BlockBehaviour.Properties properties,
            EnumProperty<T> variationProperty,
            T defaultVariation,
            T[] variations
    ) {
        super(properties);
        this.variationProperty = variationProperty;
        this.variations = variations.clone();
        registerDefaultState(stateDefinition.any()
                .setValue(variationProperty, defaultVariation)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState()
                .setValue(variationProperty, variations[0])
                .setValue(WATERLOGGED, fluid.is(Fluids.WATER));
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        return cycleForCreativePlayer(state, level, pos, player)
                ? ItemInteractionResult.sidedSuccess(level.isClientSide)
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        return cycleForCreativePlayer(state, level, pos, player)
                ? InteractionResult.sidedSuccess(level.isClientSide)
                : InteractionResult.PASS;
    }

    private boolean cycleForCreativePlayer(BlockState state, Level level, BlockPos pos, Player player) {
        if (!player.isCreative()) {
            return false;
        }
        if (!level.isClientSide) {
            T current = state.getValue(variationProperty);
            T next = variations[(current.ordinal() + 1) % variations.length];
            level.setBlock(pos, state.setValue(variationProperty, next), Block.UPDATE_ALL);
        }
        return true;
    }

    /** The random blockstate rotation has no saved property, so selection must cover all four orientations. */
    static VoxelShape randomlyRotatedShape(VoxelShape model) {
        VoxelShape result = model;
        VoxelShape current = model;
        for (int rotation = 1; rotation < 4; rotation++) {
            VoxelShape[] next = {Shapes.empty()};
            current.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    next[0] = Shapes.or(next[0], Shapes.box(
                            1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            current = next[0].optimize();
            result = Shapes.or(result, current);
        }
        return result.optimize();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return direction == Direction.DOWN && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
