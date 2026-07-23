package com.protyvkultury.revivalages.feature.technology.pitburn.blockentity;

import com.protyvkultury.revivalages.core.machine.BurnableStructureTracker;
import com.protyvkultury.revivalages.feature.technology.pitburn.PitBurnFeature;
import com.protyvkultury.revivalages.feature.technology.pitburn.recipe.PitBurnRecipe;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Timed burn state shared by the active and ash pile blocks. */
public final class PitBurnBlockEntity extends BlockEntity {

    private final NonNullList<ItemStack> outputs = NonNullList.withSize(27, ItemStack.EMPTY);
    private final BurnableStructureTracker structureTracker = new BurnableStructureTracker(
            PrimitiveTechnologyConfig.PIT_BURN_STRUCTURE_CHECK_INTERVAL.get(),
            PrimitiveTechnologyConfig.PIT_BURN_INVALID_GRACE_TICKS.get());
    private ItemStack recipeInput = ItemStack.EMPTY;
    private PitBurnRecipe activeRecipe;
    private int elapsedTicks;
    private int totalTicks;
    private int completedStages;

    public PitBurnBlockEntity(BlockPos pos, BlockState state) {
        super(PitBurnFeature.BLOCK_ENTITY.get(), pos, state);
    }

    public void initialize(ItemStack input, PitBurnRecipe recipe) {
        recipeInput = input.copyWithCount(1);
        activeRecipe = recipe;
        elapsedTicks = 0;
        completedStages = 0;
        totalTicks = Math.max(1, (int) Math.round(recipe.burnTime()
                * PrimitiveTechnologyConfig.PIT_BURN_DURATION_MULTIPLIER.get()));
        sync();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PitBurnBlockEntity burn) {
        if (!state.is(PitBurnFeature.ACTIVE_PILE.get())) {
            return;
        }
        burn.resolveRecipe();
        if (burn.activeRecipe == null) {
            level.setBlock(pos, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        BurnableStructureTracker.Result structure = burn.structureTracker.tick(burn::isStructureValid);
        if (structure == BurnableStructureTracker.Result.FAILED) {
            level.setBlock(pos, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        if (structure == BurnableStructureTracker.Result.INVALID_GRACE) {
            burn.sync();
            return;
        }
        burn.elapsedTicks++;
        int expectedStages = Math.min(burn.activeRecipe.stages(),
                (int) ((long) burn.elapsedTicks * burn.activeRecipe.stages() / burn.totalTicks));
        while (burn.completedStages < expectedStages) {
            burn.produceStage();
            burn.completedStages++;
        }
        if (burn.elapsedTicks >= burn.totalTicks) {
            burn.complete();
        } else if (burn.elapsedTicks % 20 == 0) {
            burn.sync();
        } else {
            burn.setChanged();
        }
    }

    private void produceStage() {
        if (level == null || activeRecipe == null) {
            return;
        }
        ItemStack result = activeRecipe.result();
        if (level.random.nextFloat() < activeRecipe.failureChance()) {
            List<ItemStack> failures = activeRecipe.failureResults();
            result = failures.isEmpty()
                    ? new ItemStack(PrimitiveMaterialsFeature.PIT_ASH.get())
                    : failures.get(level.random.nextInt(failures.size())).copy();
        }
        insertOutput(result);
    }

    private void insertOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (int i = 0; i < outputs.size(); i++) {
            ItemStack existing = outputs.get(i);
            if (existing.isEmpty()) {
                outputs.set(i, stack.copy());
                return;
            }
            if (ItemStack.isSameItemSameComponents(existing, stack)
                    && existing.getCount() + stack.getCount() <= existing.getMaxStackSize()) {
                existing.grow(stack.getCount());
                return;
            }
        }
    }

    private void complete() {
        if (level == null) {
            return;
        }
        while (activeRecipe != null && completedStages < activeRecipe.stages()) {
            produceStage();
            completedStages++;
        }
        NonNullList<ItemStack> completed = NonNullList.withSize(outputs.size(), ItemStack.EMPTY);
        for (int i = 0; i < outputs.size(); i++) {
            completed.set(i, outputs.get(i).copy());
        }
        level.setBlock(worldPosition, PitBurnFeature.ASH_PILE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(worldPosition) instanceof PitBurnBlockEntity ash) {
            ash.setCompletedOutputs(completed);
        }
    }

    private void setCompletedOutputs(NonNullList<ItemStack> completed) {
        for (int i = 0; i < Math.min(outputs.size(), completed.size()); i++) {
            outputs.set(i, completed.get(i).copy());
        }
        recipeInput = ItemStack.EMPTY;
        activeRecipe = null;
        sync();
    }

    public boolean isStructureValid() {
        if (level == null) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            BlockPos adjacent = worldPosition.relative(direction);
            BlockState state = level.getBlockState(adjacent);
            if (state.is(PitBurnFeature.ACTIVE_PILE.get()) || state.is(PitBurnFeature.ASH_PILE.get())) {
                continue;
            }
            Direction face = direction.getOpposite();
            if (!state.isFaceSturdy(level, adjacent, face) || state.isFlammable(level, adjacent, face)) {
                return false;
            }
        }
        return true;
    }

    public void requireStructureValidation() {
        structureTracker.requireValidation();
    }

    public double progress() {
        return totalTicks <= 0 ? 0.0D : Math.clamp(elapsedTicks / (double) totalTicks, 0.0D, 1.0D);
    }

    public int completedStages() {
        return completedStages;
    }

    public int stages() {
        return activeRecipe == null ? 0 : activeRecipe.stages();
    }

    public int invalidStructureTicks() {
        return structureTracker.invalidTicks();
    }

    public int maximumInvalidStructureTicks() {
        return structureTracker.maximumInvalidTicks();
    }

    public ItemStack recipeOutput() {
        return activeRecipe == null ? ItemStack.EMPTY : activeRecipe.result();
    }

    public SimpleContainer drops() {
        return new SimpleContainer(outputs.stream().map(ItemStack::copy).toArray(ItemStack[]::new));
    }

    private void resolveRecipe() {
        if (activeRecipe != null || level == null || recipeInput.isEmpty()) {
            return;
        }
        activeRecipe = level.getRecipeManager()
                .getRecipeFor(PitBurnFeature.RECIPE_TYPE.get(), new SingleRecipeInput(recipeInput), level)
                .map(RecipeHolder::value)
                .orElse(null);
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
        outputs.clear();
        ContainerHelper.loadAllItems(tag, outputs, registries);
        recipeInput = ItemStack.parseOptional(registries, tag.getCompound("RecipeInput"));
        elapsedTicks = tag.getInt("ElapsedTicks");
        totalTicks = tag.getInt("TotalTicks");
        completedStages = tag.getInt("CompletedStages");
        structureTracker.load(tag);
        resolveRecipe();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, outputs, true, registries);
        if (!recipeInput.isEmpty()) {
            tag.put("RecipeInput", recipeInput.save(registries));
        }
        tag.putInt("ElapsedTicks", elapsedTicks);
        tag.putInt("TotalTicks", totalTicks);
        tag.putInt("CompletedStages", completedStages);
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
