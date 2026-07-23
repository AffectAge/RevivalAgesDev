package com.protyvkultury.revivalages.feature.technology.constructionframe.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameConfig;
import com.protyvkultury.revivalages.feature.technology.constructionframe.blockentity.ConstructionFrameBlockEntity;
import com.protyvkultury.revivalages.feature.technology.constructionframe.recipe.FrameGridPosition;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ConstructionFrameBlock extends BaseEntityBlock {

    public static final MapCodec<ConstructionFrameBlock> CODEC = simpleCodec(ConstructionFrameBlock::new);
    private static final VoxelShape BASE = box(0, 0, 0, 16, 1, 16);
    private static final VoxelShape[] CELLS = createCells();
    private static final Map<Integer, VoxelShape> SHAPE_CACHE = new ConcurrentHashMap<>();

    static {
        SHAPE_CACHE.put(0, BASE);
    }

    public ConstructionFrameBlock(BlockBehaviour.Properties properties) {
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

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof ConstructionFrameBlockEntity frame) {
            return SHAPE_CACHE.computeIfAbsent(frame.occupancyMask(), ConstructionFrameBlock::buildShape);
        }
        return BASE;
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
        if (!ConstructionFrameConfig.enabled()) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.revivalages.construction_frame.disabled"),
                        true
                );
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!(level.getBlockEntity(pos) instanceof ConstructionFrameBlockEntity frame)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (frame.tryAssemble(player, hand, stack, hit)) {
            return ItemInteractionResult.SUCCESS;
        }
        FrameGridPosition cell = selectedCell(hit, false);
        if (cell != null && frame.insert(cell.index(), stack, player.hasInfiniteMaterials())) {
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!ConstructionFrameConfig.enabled()) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.revivalages.construction_frame.disabled"),
                        true
                );
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        FrameGridPosition cell = selectedCell(hit, true);
        if (cell == null || !(level.getBlockEntity(pos) instanceof ConstructionFrameBlockEntity frame)
                || frame.item(cell.index()).isEmpty()) {
            return InteractionResult.PASS;
        }
        return ItemStackInteraction.extract(
                level,
                pos,
                player,
                frame.item(cell.index()),
                () -> frame.extract(cell.index())
        );
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof ConstructionFrameBlockEntity frame
                && !frame.isAssembling()) {
            frame.dropContents();
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConstructionFrameBlockEntity(pos, state);
    }

    @Nullable
    public static FrameGridPosition selectedCell(BlockHitResult hit, boolean extracting) {
        BlockPos blockPos = hit.getBlockPos();
        Vec3 local = hit.getLocation().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Direction direction = hit.getDirection();
        double offset = (extracting ? -1.0D : 1.0D) / 6.0D;
        int x = (int) Math.floor(local.x * 3.0D + direction.getStepX() * offset);
        int y = (int) Math.floor(local.y * 3.0D + direction.getStepY() * offset);
        int z = (int) Math.floor(local.z * 3.0D + direction.getStepZ() * offset);
        FrameGridPosition cell = new FrameGridPosition(x, y, z);
        return cell.valid() ? cell : null;
    }

    private static VoxelShape[] createCells() {
        VoxelShape[] cells = new VoxelShape[27];
        for (int index = 0; index < 27; index++) {
            FrameGridPosition cell = FrameGridPosition.fromIndex(index);
            cells[index] = Shapes.box(
                    cell.x() / 3.0D,
                    cell.y() / 3.0D,
                    cell.z() / 3.0D,
                    (cell.x() + 1) / 3.0D,
                    (cell.y() + 1) / 3.0D,
                    (cell.z() + 1) / 3.0D
            );
        }
        return cells;
    }

    private static VoxelShape buildShape(int mask) {
        VoxelShape shape = BASE;
        for (int index = 0; index < 27; index++) {
            if ((mask & (1 << index)) != 0) {
                shape = Shapes.or(shape, CELLS[index]);
            }
        }
        return shape.optimize();
    }
}
