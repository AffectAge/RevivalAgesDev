package com.protyvkultury.revivalages.feature.technology.pitkiln.blockentity;

import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import com.protyvkultury.revivalages.core.machine.BurnableStructureTracker;
import com.protyvkultury.revivalages.feature.technology.pitkiln.PitKilnFeature;
import com.protyvkultury.revivalages.feature.technology.pitkiln.block.PitKilnBlock;
import com.protyvkultury.revivalages.feature.technology.pitkiln.block.PitKilnStage;
import com.protyvkultury.revivalages.feature.technology.pitkiln.recipe.PitKilnRecipe;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveTags;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class PitKilnBlockEntity extends BlockEntity {

    private static final int STRUCTURE_VALIDATION_INTERVAL = 20;
    private static final int MAXIMUM_INVALID_TICKS = 100;

    private static final int INPUT_SLOT = 0;
    private static final int FIRST_LOG_SLOT = 1;
    private static final int LAST_LOG_SLOT = 3;
    private static final int FIRST_OUTPUT_SLOT = 4;
    private static final int LAST_OUTPUT_SLOT = 12;

    private final NonNullList<ItemStack> items = NonNullList.withSize(13, ItemStack.EMPTY);
    private int elapsedTicks;
    private int totalTicks;
    private int rainTicks;
    private final BurnableStructureTracker structureTracker =
            new BurnableStructureTracker(STRUCTURE_VALIDATION_INTERVAL, MAXIMUM_INVALID_TICKS);
    private PitKilnRecipe activeRecipe;

    public PitKilnBlockEntity(BlockPos pos, BlockState state) {
        super(PitKilnFeature.BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PitKilnBlockEntity kiln) {
        if (state.getValue(PitKilnBlock.STAGE) != PitKilnStage.ACTIVE) {
            return;
        }
        BurnableStructureTracker.Result structure = kiln.structureTracker.tick(kiln::isStructureValid);
        if (structure == BurnableStructureTracker.Result.FAILED) {
            kiln.failAll();
            return;
        }
        if (structure == BurnableStructureTracker.Result.INVALID_GRACE) {
            kiln.elapsedTicks = 0;
            if (kiln.structureTracker.invalidTicks() % 20 == 1) {
                kiln.sync();
            } else {
                kiln.setChanged();
            }
            return;
        }
        kiln.ensureFireAbove();
        if (PrimitiveTechnologyConfig.PIT_KILN_RAIN_EXTINGUISHES.get() && level.isRainingAt(pos.above())) {
            kiln.rainTicks++;
            if (kiln.rainTicks >= PrimitiveTechnologyConfig.PIT_KILN_RAIN_EXTINGUISH_TICKS.get()) {
                kiln.rainTicks = 0;
                kiln.elapsedTicks = 0;
                kiln.totalTicks = 0;
                kiln.clearFireAbove();
                level.setBlock(pos, state.setValue(PitKilnBlock.STAGE, PitKilnStage.THATCH), Block.UPDATE_ALL);
                kiln.sync();
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                return;
            }
        } else {
            kiln.rainTicks = 0;
        }
        kiln.elapsedTicks++;
        if (kiln.elapsedTicks >= kiln.totalTicks) {
            kiln.complete();
        } else {
            kiln.setChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PitKilnBlockEntity kiln) {
        if (state.getValue(PitKilnBlock.STAGE) != PitKilnStage.ACTIVE || level.getGameTime() % 10L != 0L) {
            return;
        }
        level.addParticle(ParticleTypes.FLAME,
                pos.getX() + 0.2D + level.random.nextDouble() * 0.6D,
                pos.getY() + 1.0D,
                pos.getZ() + 0.2D + level.random.nextDouble() * 0.6D,
                0.0D, 0.03D, 0.0D);
        level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                pos.getX() + 0.5D,
                pos.getY() + 1.0D,
                pos.getZ() + 0.5D,
                0.0D, 0.04D, 0.0D);
        if (PrimitiveTechnologyConfig.PROGRESS_PARTICLES.get()) {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D,
                    0.0D, 0.02D, 0.0D);
        }
    }

    public ItemStack input() {
        return items.get(INPUT_SLOT);
    }

    public int logCount() {
        int count = 0;
        for (int slot = FIRST_LOG_SLOT; slot <= LAST_LOG_SLOT; slot++) {
            if (!items.get(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public ItemStack logStack(int index) {
        int slot = FIRST_LOG_SLOT + index;
        return slot >= FIRST_LOG_SLOT && slot <= LAST_LOG_SLOT ? items.get(slot) : ItemStack.EMPTY;
    }

    public ItemStack displayOutput() {
        for (int slot = FIRST_OUTPUT_SLOT; slot <= LAST_OUTPUT_SLOT; slot++) {
            if (!items.get(slot).isEmpty()) {
                return items.get(slot);
            }
        }
        return ItemStack.EMPTY;
    }

    public double progress() {
        return totalTicks <= 0 ? 0.0D : Math.min(1.0D, elapsedTicks / (double) totalTicks);
    }

    public ItemStack recipeOutput() {
        return activeRecipe == null ? ItemStack.EMPTY : activeRecipe.result();
    }

    public boolean canInsert(ItemStack stack) {
        if (level == null || stack.isEmpty() || getBlockState().getValue(PitKilnBlock.STAGE) != PitKilnStage.EMPTY) {
            return false;
        }
        ItemStack current = items.get(INPUT_SLOT);
        return (current.isEmpty() || ItemStack.isSameItemSameComponents(current, stack))
                && current.getCount() < PrimitiveTechnologyConfig.PIT_KILN_MAX_STACK_SIZE.get()
                && findRecipe(stack).isPresent();
    }

    public void insert(ItemStack source, boolean infinite) {
        if (!canInsert(source)) {
            return;
        }
        if (items.get(INPUT_SLOT).isEmpty()) {
            items.set(INPUT_SLOT, source.copyWithCount(1));
        } else {
            items.get(INPUT_SLOT).grow(1);
        }
        if (!infinite) {
            source.shrink(1);
        }
        resolveRecipe();
        sync();
    }

    public ItemStack extractInput() {
        ItemStack result = items.get(INPUT_SLOT);
        items.set(INPUT_SLOT, ItemStack.EMPTY);
        activeRecipe = null;
        sync();
        return result;
    }

    public void addThatch() {
        if (level != null && !items.get(INPUT_SLOT).isEmpty()) {
            level.setBlock(worldPosition, getBlockState().setValue(PitKilnBlock.STAGE, PitKilnStage.THATCH), Block.UPDATE_ALL);
            sync();
        }
    }

    public boolean canAddLog() {
        PitKilnStage stage = getBlockState().getValue(PitKilnBlock.STAGE);
        return (stage == PitKilnStage.THATCH || stage == PitKilnStage.WOOD) && logCount() < 3;
    }

    public void addLog(ItemStack source, boolean infinite) {
        if (!canAddLog()) {
            return;
        }
        for (int slot = FIRST_LOG_SLOT; slot <= LAST_LOG_SLOT; slot++) {
            if (items.get(slot).isEmpty()) {
                items.set(slot, source.copyWithCount(1));
                if (!infinite) {
                    source.shrink(1);
                }
                if (logCount() == 3 && level != null) {
                    level.setBlock(worldPosition, getBlockState().setValue(PitKilnBlock.STAGE, PitKilnStage.WOOD), Block.UPDATE_ALL);
                }
                sync();
                return;
            }
        }
    }

    public ItemStack removeLog() {
        for (int slot = LAST_LOG_SLOT; slot >= FIRST_LOG_SLOT; slot--) {
            if (!items.get(slot).isEmpty()) {
                ItemStack result = items.get(slot);
                items.set(slot, ItemStack.EMPTY);
                if (level != null && getBlockState().getValue(PitKilnBlock.STAGE) == PitKilnStage.WOOD) {
                    level.setBlock(worldPosition, getBlockState().setValue(PitKilnBlock.STAGE, PitKilnStage.THATCH), Block.UPDATE_ALL);
                }
                sync();
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean canIgnite() {
        resolveRecipe();
        return getBlockState().getValue(PitKilnBlock.STAGE) == PitKilnStage.WOOD
                && activeRecipe != null
                && logCount() == 3
                && isStructureValid();
    }

    public void ignite() {
        if (!canIgnite() || level == null) {
            return;
        }
        int count = items.get(INPUT_SLOT).getCount();
        int max = PrimitiveTechnologyConfig.PIT_KILN_MAX_STACK_SIZE.get();
        double percentage = max <= 1 ? 1.0D : (count - 1) / (double) (max - 1);
        double variable = PrimitiveTechnologyConfig.PIT_KILN_VARIABLE_SPEED.get();
        double scalar = (1.0D - variable) * percentage + variable;
        totalTicks = Math.max(1, (int) Math.round(activeRecipe.burnTime()
                * PrimitiveTechnologyConfig.PIT_KILN_DURATION_MULTIPLIER.get()
                * scalar));
        elapsedTicks = 0;
        rainTicks = 0;
        for (int slot = FIRST_LOG_SLOT; slot <= LAST_LOG_SLOT; slot++) {
            items.set(slot, ItemStack.EMPTY);
        }
        level.setBlock(worldPosition, getBlockState().setValue(PitKilnBlock.STAGE, PitKilnStage.ACTIVE), Block.UPDATE_ALL);
        sync();
    }

    public boolean isStructureValid() {
        if (level == null) {
            return false;
        }
        BlockState above = level.getBlockState(worldPosition.above());
        if (!above.canBeReplaced() && !above.is(Blocks.FIRE)) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = worldPosition.relative(direction);
            if (!validStructureBlock(adjacent, direction.getOpposite())) {
                return false;
            }
        }
        return validStructureBlock(worldPosition.below(), Direction.UP);
    }

    private boolean validStructureBlock(BlockPos pos, Direction face) {
        BlockState state = level.getBlockState(pos);
        return state.is(PrimitiveTags.PIT_KILN_STRUCTURE_BLOCKS)
                || state.is(PrimitiveTags.PIT_KILN_REFRACTORY_BLOCKS)
                || (state.isFaceSturdy(level, pos, face) && !state.isFlammable(level, pos, face));
    }

    private void complete() {
        if (level == null || activeRecipe == null) {
            failAll();
            return;
        }
        int refractory = countRefractoryBlocks();
        float chance = activeRecipe.failureChance() * (1.0F - refractory / 5.0F);
        int count = items.get(INPUT_SLOT).getCount();
        items.set(INPUT_SLOT, ItemStack.EMPTY);
        for (int i = 0; i < count; i++) {
            if (level.random.nextFloat() < chance) {
                insertOutput(randomFailure(activeRecipe.failureResults()));
            } else {
                insertOutput(activeRecipe.result());
            }
        }
        insertOutput(new ItemStack(PrimitiveMaterialsFeature.PIT_ASH.get(), level.random.nextInt(3) + 1));
        clearFireAbove();
        level.setBlock(worldPosition, getBlockState().setValue(PitKilnBlock.STAGE, PitKilnStage.COMPLETE), Block.UPDATE_ALL);
        sync();
    }

    private void failAll() {
        if (level == null) {
            return;
        }
        resolveRecipe();
        int count = items.get(INPUT_SLOT).getCount();
        items.set(INPUT_SLOT, ItemStack.EMPTY);
        for (int i = 0; i < count; i++) {
            insertOutput(activeRecipe == null
                    ? new ItemStack(PrimitiveMaterialsFeature.PIT_ASH.get())
                    : randomFailure(activeRecipe.failureResults()));
        }
        clearFireAbove();
        level.setBlock(worldPosition, getBlockState().setValue(PitKilnBlock.STAGE, PitKilnStage.COMPLETE), Block.UPDATE_ALL);
        sync();
    }

    private ItemStack randomFailure(List<ItemStack> failures) {
        if (failures.isEmpty() || level == null) {
            return new ItemStack(PrimitiveMaterialsFeature.PIT_ASH.get());
        }
        return failures.get(level.random.nextInt(failures.size())).copyWithCount(1);
    }

    private void ensureFireAbove() {
        if (level == null) {
            return;
        }
        BlockPos above = worldPosition.above();
        BlockState state = level.getBlockState(above);
        if (!state.is(Blocks.FIRE) && (state.isAir() || state.canBeReplaced())) {
            level.setBlock(above, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private void clearFireAbove() {
        if (level != null && level.getBlockState(worldPosition.above()).is(Blocks.FIRE)) {
            level.removeBlock(worldPosition.above(), false);
        }
    }

    public int invalidStructureTicks() {
        return structureTracker.invalidTicks();
    }

    public int maximumInvalidStructureTicks() {
        return structureTracker.maximumInvalidTicks();
    }

    public int rainTicks() {
        return rainTicks;
    }

    public void removeFireAbove() {
        clearFireAbove();
    }

    private int countRefractoryBlocks() {
        int count = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(worldPosition.relative(direction)).is(PrimitiveTags.PIT_KILN_REFRACTORY_BLOCKS)) {
                count++;
            }
        }
        if (level.getBlockState(worldPosition.below()).is(PrimitiveTags.PIT_KILN_REFRACTORY_BLOCKS)) {
            count++;
        }
        return count;
    }

    private void insertOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (int slot = FIRST_OUTPUT_SLOT; slot <= LAST_OUTPUT_SLOT; slot++) {
            ItemStack current = items.get(slot);
            if (!current.isEmpty()
                    && ItemStack.isSameItemSameComponents(current, stack)
                    && current.getCount() + stack.getCount() <= current.getMaxStackSize()) {
                current.grow(stack.getCount());
                return;
            }
        }
        for (int slot = FIRST_OUTPUT_SLOT; slot <= LAST_OUTPUT_SLOT; slot++) {
            if (items.get(slot).isEmpty()) {
                items.set(slot, stack.copy());
                return;
            }
        }
        if (level != null) {
            Block.popResource(level, worldPosition.above(), stack);
        }
    }

    public void giveOutputs(Player player) {
        if (level == null) {
            return;
        }
        boolean extracted = false;
        for (int slot = FIRST_OUTPUT_SLOT; slot <= LAST_OUTPUT_SLOT; slot++) {
            ItemStack stack = items.get(slot);
            extracted |= !stack.isEmpty();
            if (!stack.isEmpty() && !player.addItem(stack)) {
                Block.popResource(level, worldPosition.above(), stack);
            }
            items.set(slot, ItemStack.EMPTY);
        }
        if (extracted) {
            ItemStackInteraction.playExtractionSound(level, worldPosition);
        }
        activeRecipe = null;
        elapsedTicks = 0;
        totalTicks = 0;
        level.setBlock(worldPosition, getBlockState().setValue(PitKilnBlock.STAGE, PitKilnStage.EMPTY), Block.UPDATE_ALL);
        sync();
    }

    public SimpleContainer drops() {
        return new SimpleContainer(items.toArray(ItemStack[]::new));
    }

    private Optional<RecipeHolder<PitKilnRecipe>> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(PitKilnFeature.RECIPE_TYPE.get(), new SingleRecipeInput(stack), level);
    }

    private void resolveRecipe() {
        activeRecipe = findRecipe(items.get(INPUT_SLOT)).map(RecipeHolder::value).orElse(null);
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        elapsedTicks = tag.getInt("ElapsedTicks");
        totalTicks = tag.getInt("TotalTicks");
        rainTicks = tag.getInt("RainTicks");
        structureTracker.load(tag);
        resolveRecipe();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
        tag.putInt("ElapsedTicks", elapsedTicks);
        tag.putInt("TotalTicks", totalTicks);
        tag.putInt("RainTicks", rainTicks);
        structureTracker.save(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
