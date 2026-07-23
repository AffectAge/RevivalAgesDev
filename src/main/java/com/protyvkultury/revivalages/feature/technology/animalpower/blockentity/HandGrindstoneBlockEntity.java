package com.protyvkultury.revivalages.feature.technology.animalpower.blockentity;

import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerConfig;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingMachine;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingChance;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingRecipe;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class HandGrindstoneBlockEntity extends BlockEntity {

    private final net.minecraft.core.NonNullList<ItemStack> items =
            net.minecraft.core.NonNullList.withSize(3, ItemStack.EMPTY);
    private int workPoints;
    private int rotationTicks;
    private int rotationDuration;

    public HandGrindstoneBlockEntity(BlockPos pos, BlockState state) {
        super(AnimalPowerFeature.HAND_GRINDSTONE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            HandGrindstoneBlockEntity grindstone
    ) {
        if (grindstone.rotationTicks <= 0) {
            return;
        }
        grindstone.rotationTicks--;
        if (grindstone.rotationTicks == 0) {
            grindstone.workPoints += AnimalPowerConfig.HAND_GRINDSTONE_POINTS_PER_ROTATION.get();
            grindstone.tryComplete();
        }
        grindstone.sync();
    }

    public ItemStack item(int slot) {
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }

    public boolean canInsert(ItemStack stack) {
        return items.getFirst().isEmpty() && findRecipe(stack).filter(this::outputsFit).isPresent();
    }

    public void insert(ItemStack source, boolean infinite) {
        Optional<GrindingRecipe> recipe = findRecipe(source);
        if (!items.getFirst().isEmpty() || recipe.isEmpty() || !outputsFit(recipe.get())) {
            return;
        }
        int count = recipe.get().inputCount();
        if (source.getCount() < count) {
            return;
        }
        items.set(0, source.copyWithCount(count));
        if (!infinite) {
            source.shrink(count);
        }
        workPoints = 0;
        sync();
    }

    public ItemStack extract(int slot) {
        if (slot < 0 || slot >= items.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        if (slot == 0) {
            workPoints = 0;
        }
        sync();
        return result;
    }

    public boolean turn(Player player) {
        Optional<GrindingRecipe> recipe = findRecipe(items.getFirst());
        if (rotationTicks > 0 || recipe.isEmpty() || !outputsFit(recipe.get())) {
            return false;
        }
        rotationDuration = AnimalPowerConfig.HAND_GRINDSTONE_ROTATION_TICKS.get();
        rotationTicks = rotationDuration;
        player.causeFoodExhaustion(AnimalPowerConfig.HAND_GRINDSTONE_EXHAUSTION.get().floatValue());
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.65F, 1.0F);
        }
        sync();
        return true;
    }

    public double progress() {
        return findRecipe(items.getFirst())
                .map(recipe -> Math.min(1.0D, workPoints / (double) recipe.workPoints()))
                .orElse(0.0D);
    }

    public float rotation(float partialTick) {
        if (rotationDuration <= 0 || rotationTicks <= 0) {
            return 0.0F;
        }
        return (rotationDuration - rotationTicks + partialTick) / rotationDuration;
    }

    public ItemStack recipeOutput() {
        return findRecipe(items.getFirst()).map(GrindingRecipe::result).orElse(ItemStack.EMPTY);
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int slot = 0; slot < items.size(); slot++) {
            Block.popResource(level, worldPosition, items.get(slot));
            items.set(slot, ItemStack.EMPTY);
        }
    }

    private Optional<GrindingRecipe> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager()
                .getRecipeFor(AnimalPowerFeature.GRINDING_TYPE.get(), new SingleRecipeInput(stack), level)
                .map(RecipeHolder::value)
                .filter(recipe -> recipe.supports(GrindingMachine.HAND));
    }

    private void tryComplete() {
        Optional<GrindingRecipe> resolved = findRecipe(items.getFirst());
        if (resolved.isEmpty() || level == null) {
            return;
        }
        GrindingRecipe recipe = resolved.get();
        if (workPoints < recipe.workPoints() || !outputsFit(recipe)) {
            return;
        }
        items.getFirst().shrink(recipe.inputCount());
        if (items.getFirst().isEmpty()) {
            items.set(0, ItemStack.EMPTY);
        }
        merge(1, recipe.result());
        if (!recipe.secondaryResult().isEmpty()
                && GrindingChance.shouldProduce(recipe.secondaryChance(), level.random.nextDouble())) {
            merge(2, recipe.secondaryResult());
        }
        workPoints = 0;
        level.playSound(null, worldPosition, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, 0.8F);
    }

    private static boolean canMerge(ItemStack stored, ItemStack addition) {
        return addition.isEmpty() || stored.isEmpty()
                || ItemStack.isSameItemSameComponents(stored, addition)
                && stored.getCount() + addition.getCount() <= stored.getMaxStackSize();
    }

    private boolean outputsFit(GrindingRecipe recipe) {
        return canMerge(items.get(1), recipe.result())
                && canMerge(items.get(2), recipe.secondaryResult());
    }

    private void merge(int slot, ItemStack addition) {
        if (addition.isEmpty()) {
            return;
        }
        if (items.get(slot).isEmpty()) {
            items.set(slot, addition.copy());
        } else {
            items.get(slot).grow(addition.getCount());
        }
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
        ContainerHelper.loadAllItems(tag, items, registries);
        workPoints = Math.max(0, tag.getInt("WorkPoints"));
        rotationTicks = Math.max(0, tag.getInt("RotationTicks"));
        rotationDuration = Math.max(0, tag.getInt("RotationDuration"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
        tag.putInt("WorkPoints", workPoints);
        tag.putInt("RotationTicks", rotationTicks);
        tag.putInt("RotationDuration", rotationDuration);
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
