package com.protyvkultury.revivalages.feature.technology.campfire.block;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.blockentity.CampfireBlockEntity;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveTags;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CampfireBlock extends BaseEntityBlock {

    public static final MapCodec<CampfireBlock> CODEC = simpleCodec(CampfireBlock::new);
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    public static final IntegerProperty FUEL = IntegerProperty.create("fuel", 0, 8);
    public static final IntegerProperty ASH = IntegerProperty.create("ash", 0, 8);
    private static final VoxelShape LOG_SHAPE = box(0, 0, 0, 16, 6, 16);
    private static final VoxelShape TINDER_SHAPE = box(4, 0, 4, 12, 5, 12);

    public CampfireBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(LIT, false)
                .setValue(FUEL, 0)
                .setValue(ASH, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(LIT, FUEL, ASH);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(FUEL) > 0) {
            return LOG_SHAPE;
        }
        int ash = state.getValue(ASH);
        return ash > 0 ? box(2, 0, 2, 14, ash, 14) : TINDER_SHAPE;
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
        if (!(level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (campfire.isDead()) {
            return ItemInteractionResult.CONSUME;
        }
        if (stack.is(Items.WATER_BUCKET) && state.getValue(LIT)) {
            if (!level.isClientSide) {
                campfire.extinguish();
                if (!player.hasInfiniteMaterials()) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) {
            if (!campfire.canIgnite()) {
                return ItemInteractionResult.CONSUME;
            }
            if (!level.isClientSide) {
                campfire.ignite();
                if (!player.hasInfiniteMaterials()) {
                    if (stack.is(Items.FLINT_AND_STEEL)) {
                        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                    } else {
                        stack.shrink(1);
                    }
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.is(net.minecraft.tags.ItemTags.SHOVELS) && campfire.ashLevel() > 0) {
            if (!level.isClientSide) {
                ItemStackInteraction.giveOrDrop(level, pos, player,
                        new ItemStack(PrimitiveMaterialsFeature.PIT_ASH.get()));
                campfire.removeAsh();
                if (!player.hasInfiniteMaterials()) {
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.is(PrimitiveTags.CAMPFIRE_FUELS) || stack.is(net.minecraft.tags.ItemTags.LOGS)) {
            if (!campfire.canAddLog()) {
                return ItemInteractionResult.CONSUME;
            }
            if (!level.isClientSide) {
                campfire.addLog(stack, player.hasInfiniteMaterials());
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!campfire.cookingStack().isEmpty()) {
            return ItemInteractionResult.CONSUME;
        }
        if (!level.getBlockState(pos.above()).is(PrimitiveTags.CAMPFIRE_OCCUPANTS) && campfire.canCook(stack)) {
            return ItemStackInteraction.insert(level, true,
                    () -> campfire.insertCookingStack(stack, player.hasInfiniteMaterials()));
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
            if (!campfire.cookingStack().isEmpty()) {
                if (level.getBlockState(pos.above()).is(PrimitiveTags.CAMPFIRE_OCCUPANTS)) {
                    return InteractionResult.CONSUME;
                }
                return ItemStackInteraction.extract(level, pos, player,
                        campfire.cookingStack(), campfire::extractCookingStack);
            }
            if (campfire.canRemoveLog()) {
                ItemStack visible = campfire.logStack(campfire.fuelLevel() - 1);
                InteractionResult result = ItemStackInteraction.extract(level, pos, player, visible, campfire::removeLog);
                if (!level.isClientSide
                        && state.getValue(LIT)
                        && !wearsFrostWalker(level, player)
                        && level.random.nextDouble() < PrimitiveTechnologyConfig.CAMPFIRE_PLAYER_BURN_CHANCE.get()) {
                    player.hurt(level.damageSources().hotFloor(),
                            PrimitiveTechnologyConfig.CAMPFIRE_PLAYER_BURN_DAMAGE.get().floatValue());
                }
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(LIT)
                && entity instanceof LivingEntity living
                && !entity.fireImmune()
                && !wearsFrostWalker(level, living)) {
            entity.hurt(level.damageSources().inFire(), PrimitiveTechnologyConfig.CAMPFIRE_ENTITY_BURN_DAMAGE.get().floatValue());
            entity.setRemainingFireTicks(Math.max(entity.getRemainingFireTicks(), 40));
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
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
        return direction == Direction.DOWN && !canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
            Containers.dropContents(level, pos, campfire.drops());
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CampfireBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, CampfireFeature.BLOCK_ENTITY.get(), CampfireBlockEntity::clientTick)
                : createTickerHelper(type, CampfireFeature.BLOCK_ENTITY.get(), CampfireBlockEntity::serverTick);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(LIT) ? super.getFluidState(state) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        return direction == Direction.UP && state.getValue(LIT);
    }

    private static boolean wearsFrostWalker(Level level, LivingEntity entity) {
        return EnchantmentHelper.getEnchantmentLevel(
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FROST_WALKER),
                entity
        ) > 0;
    }

}
