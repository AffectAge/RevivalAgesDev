package com.protyvkultury.revivalages.feature.technology.campfire.blockentity;

import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.block.CampfireBlock;
import com.protyvkultury.revivalages.feature.technology.campfire.recipe.CampfireRecipeResolver;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class CampfireBlockEntity extends BlockEntity {

    private static final int COOKING_SLOT = 0;
    private static final int FIRST_LOG_SLOT = 1;
    private static final int LAST_LOG_SLOT = 8;
    private static final int PARTICLE_INTERVAL = 20;

    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private boolean hasTinder;
    private boolean dead;
    private boolean lit;
    private boolean completed;
    private int ash;
    private int burnTime;
    private int rainTicks;
    private int burnOutputTicks;
    private double progress;
    private int totalTime;
    private ResourceLocation recipeId;
    private ItemStack recipeOutput = ItemStack.EMPTY;

    public CampfireBlockEntity(BlockPos pos, BlockState state) {
        super(CampfireFeature.BLOCK_ENTITY.get(), pos, state);
        lit = state.getValue(CampfireBlock.LIT);
        ash = state.getValue(CampfireBlock.ASH);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        if (level.getGameTime() % 20L == 0L) {
            campfire.resolveRecipe();
            campfire.refreshLightLevel();
        }
        if (!campfire.lit) {
            return;
        }
        if (PrimitiveTechnologyConfig.CAMPFIRE_RAIN_EXTINGUISHES.get() && level.isRainingAt(pos.above())) {
            campfire.rainTicks++;
            if (campfire.rainTicks >= PrimitiveTechnologyConfig.CAMPFIRE_RAIN_EXTINGUISH_TICKS.get()) {
                campfire.extinguish();
                return;
            }
        } else {
            campfire.rainTicks = 0;
        }

        if (campfire.ash >= 8) {
            campfire.extinguish();
            return;
        }
        if (campfire.burnTime <= 0) {
            if (!campfire.consumeFuelLog()) {
                campfire.die();
                return;
            }
            campfire.burnTime = PrimitiveTechnologyConfig.CAMPFIRE_BURN_TICKS_PER_LOG.get();
            if (level.random.nextDouble() < PrimitiveTechnologyConfig.CAMPFIRE_ASH_CHANCE.get()) {
                campfire.ash = Math.min(8, campfire.ash + 1);
            }
            campfire.updateState();
            if (campfire.ash >= 8) {
                campfire.extinguish();
                return;
            }
        }
        campfire.burnTime--;
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (level.random.nextDouble() < PrimitiveTechnologyConfig.CAMPFIRE_FLOOR_IGNITION_CHANCE.get()
                && belowState.isFlammable(level, below, Direction.UP)) {
            level.setBlock(below, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        if (campfire.completed) {
            campfire.burnOutputTicks++;
            if (campfire.burnOutputTicks >= PrimitiveTechnologyConfig.CAMPFIRE_BURNED_FOOD_TICKS.get()) {
                campfire.items.set(COOKING_SLOT, new ItemStack(PrimitiveMaterialsFeature.BURNED_FOOD.get()));
                campfire.burnOutputTicks = 0;
                campfire.sync();
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.6F);
            } else {
                campfire.setChanged();
            }
            return;
        }
        if (campfire.recipeOutput.isEmpty() || campfire.items.get(COOKING_SLOT).isEmpty()) {
            return;
        }
        double fuelSpeed = Math.min(2.0D, campfire.fuelLevel()
                / (double) PrimitiveTechnologyConfig.CAMPFIRE_FULL_SPEED_FUEL_LEVEL.get());
        campfire.progress += fuelSpeed;
        if (campfire.progress >= campfire.totalTime) {
            campfire.items.set(COOKING_SLOT, campfire.recipeOutput.copy());
            campfire.completed = true;
            campfire.progress = campfire.totalTime;
            campfire.burnOutputTicks = 0;
            campfire.sync();
        } else {
            campfire.setChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        if (!state.getValue(CampfireBlock.LIT)) {
            return;
        }
        if (level.random.nextInt(10) == 0) {
            level.playLocalSound(pos, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS,
                    1.0F, 1.0F, false);
        }
        if (level.getGameTime() % PARTICLE_INTERVAL != 0L) {
            return;
        }
        level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                pos.getX() + 0.5D, pos.getY() + 0.45D, pos.getZ() + 0.5D,
                0.0D, 0.035D, 0.0D);
        if (PrimitiveTechnologyConfig.PROGRESS_PARTICLES.get() && campfire.isProcessing()) {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                    0.0D, 0.02D, 0.0D);
        }
        if (campfire.completed && campfire.burnOutputTicks > PrimitiveTechnologyConfig.CAMPFIRE_BURNED_FOOD_TICKS.get() / 2) {
            level.addParticle(ParticleTypes.LARGE_SMOKE,
                    pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                    0.0D, 0.03D, 0.0D);
        }
        if (level.random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.FLAME,
                    pos.getX() + 0.35D + level.random.nextDouble() * 0.3D,
                    pos.getY() + 0.45D,
                    pos.getZ() + 0.35D + level.random.nextDouble() * 0.3D,
                    0.0D, 0.01D, 0.0D);
        }
    }

    public void setHasTinder(boolean value) {
        hasTinder = value;
        dead = false;
        sync();
    }

    public boolean canIgnite() {
        return hasTinder && !dead && !lit && fuelLevel() > 0 && ash < 8;
    }

    public void ignite() {
        if (!canIgnite()) {
            return;
        }
        lit = true;
        burnTime = Math.max(1, burnTime);
        updateState();
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    public void extinguish() {
        lit = false;
        rainTicks = 0;
        updateState();
    }

    private void die() {
        lit = false;
        dead = true;
        hasTinder = false;
        ash = Math.max(1, ash);
        if (level != null && !level.isClientSide && !cookingStack().isEmpty()) {
            Block.popResource(level, worldPosition, extractCookingStack());
        }
        updateState();
    }

    public boolean canAddLog() {
        return !dead && fuelLevel() < 8;
    }

    public void addLog(ItemStack source, boolean infinite) {
        for (int slot = FIRST_LOG_SLOT; slot <= LAST_LOG_SLOT; slot++) {
            if (items.get(slot).isEmpty()) {
                items.set(slot, source.copyWithCount(1));
                if (!infinite) {
                    source.shrink(1);
                }
                updateState();
                return;
            }
        }
    }

    public boolean canRemoveLog() {
        return fuelLevel() > 0;
    }

    public ItemStack removeLog() {
        for (int slot = LAST_LOG_SLOT; slot >= FIRST_LOG_SLOT; slot--) {
            if (!items.get(slot).isEmpty()) {
                ItemStack result = items.get(slot);
                items.set(slot, ItemStack.EMPTY);
                updateState();
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean consumeFuelLog() {
        ItemStack consumed = removeLog();
        return !consumed.isEmpty();
    }

    public int fuelLevel() {
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

    public int ashLevel() {
        return ash;
    }

    public void removeAsh() {
        ash = Math.max(0, ash - 1);
        updateState();
    }

    public ItemStack cookingStack() {
        return items.get(COOKING_SLOT);
    }

    public boolean canCook(ItemStack stack) {
        return items.get(COOKING_SLOT).isEmpty()
                && level != null
                && CampfireRecipeResolver.find(level, stack).isPresent();
    }

    public void insertCookingStack(ItemStack source, boolean infinite) {
        if (!canCook(source)) {
            return;
        }
        items.set(COOKING_SLOT, source.copyWithCount(1));
        if (!infinite) {
            source.shrink(1);
        }
        completed = false;
        burnOutputTicks = 0;
        progress = 0.0D;
        recipeId = null;
        resolveRecipe();
        sync();
    }

    public ItemStack extractCookingStack() {
        ItemStack result = items.get(COOKING_SLOT);
        items.set(COOKING_SLOT, ItemStack.EMPTY);
        completed = false;
        burnOutputTicks = 0;
        progress = 0.0D;
        totalTime = 0;
        recipeId = null;
        recipeOutput = ItemStack.EMPTY;
        sync();
        return result;
    }

    public double progress() {
        return totalTime <= 0 ? 0.0D : Math.min(1.0D, progress / totalTime);
    }

    public boolean isProcessing() {
        return lit && !completed && !recipeOutput.isEmpty() && ash < 8;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isLit() {
        return lit;
    }

    public boolean isDead() {
        return dead;
    }

    public boolean hasTinder() {
        return hasTinder;
    }

    public int burnTime() {
        return burnTime;
    }

    public ItemStack recipeOutput() {
        return recipeOutput.copy();
    }

    public SimpleContainer drops() {
        ItemStack[] drops = new ItemStack[items.size() + 1];
        for (int index = 0; index < items.size(); index++) {
            drops[index] = items.get(index);
        }
        drops[items.size()] = ash > 0
                ? new ItemStack(PrimitiveMaterialsFeature.PIT_ASH.get(), ash)
                : ItemStack.EMPTY;
        return new SimpleContainer(drops);
    }

    public IItemHandler itemHandler(@Nullable Direction side) {
        return new CampfireItemHandler(side);
    }

    private void resolveRecipe() {
        if (level == null || completed || items.get(COOKING_SLOT).isEmpty()) {
            if (!completed) {
                recipeOutput = ItemStack.EMPTY;
                totalTime = 0;
            }
            return;
        }
        Optional<CampfireRecipeResolver.Match> match = CampfireRecipeResolver.find(level, items.get(COOKING_SLOT));
        if (match.isEmpty()) {
            recipeOutput = ItemStack.EMPTY;
            totalTime = 0;
            progress = 0.0D;
            recipeId = null;
            return;
        }
        CampfireRecipeResolver.Match recipe = match.get();
        if (!recipe.id().equals(recipeId)) {
            recipeId = recipe.id();
            progress = 0.0D;
        }
        recipeOutput = recipe.output();
        totalTime = recipe.cookingTime();
    }

    private void updateState() {
        setChanged();
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        BlockState updated = state
                .setValue(CampfireBlock.LIT, lit)
                .setValue(CampfireBlock.FUEL, fuelLevel())
                .setValue(CampfireBlock.ASH, ash)
                .setValue(CampfireBlock.LIGHT, configuredLightLevel());
        if (updated != state) {
            level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
        }
        sync();
    }

    private void refreshLightLevel() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        int light = configuredLightLevel();
        if (state.getValue(CampfireBlock.LIGHT) != light) {
            level.setBlock(
                    worldPosition,
                    state.setValue(CampfireBlock.LIGHT, light),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    private int configuredLightLevel() {
        if (!lit) {
            return 0;
        }
        int minimum = PrimitiveTechnologyConfig.CAMPFIRE_MINIMUM_LIGHT.get();
        int maximum = PrimitiveTechnologyConfig.CAMPFIRE_MAXIMUM_LIGHT.get();
        double fuelRatio = fuelLevel() / 8.0D;
        return Math.clamp((int) Math.round(minimum + (maximum - minimum) * fuelRatio), 0, 15);
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
        hasTinder = tag.getBoolean("HasTinder");
        dead = tag.getBoolean("Dead");
        lit = tag.getBoolean("Lit");
        completed = tag.getBoolean("Completed");
        ash = tag.getInt("Ash");
        burnTime = tag.getInt("BurnTime");
        rainTicks = tag.getInt("RainTicks");
        burnOutputTicks = tag.getInt("BurnOutputTicks");
        progress = tag.getDouble("Progress");
        totalTime = tag.getInt("TotalTime");
        String id = tag.getString("Recipe");
        recipeId = id.isEmpty() ? null : ResourceLocation.tryParse(id);
        recipeOutput = ItemStack.parseOptional(registries, tag.getCompound("RecipeOutput"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
        tag.putBoolean("HasTinder", hasTinder);
        tag.putBoolean("Dead", dead);
        tag.putBoolean("Lit", lit);
        tag.putBoolean("Completed", completed);
        tag.putInt("Ash", ash);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("RainTicks", rainTicks);
        tag.putInt("BurnOutputTicks", burnOutputTicks);
        tag.putDouble("Progress", progress);
        tag.putInt("TotalTime", totalTime);
        if (recipeId != null) {
            tag.putString("Recipe", recipeId.toString());
        }
        if (!recipeOutput.isEmpty()) {
            tag.put("RecipeOutput", recipeOutput.save(registries));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private final class CampfireItemHandler implements IItemHandler {

        private final Direction side;

        private CampfireItemHandler(@Nullable Direction side) {
            this.side = side == null ? Direction.UP : side;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? cookingStack().copy() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (side == Direction.DOWN || slot != 0 || !canCook(stack)) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            if (!simulate) {
                ItemStack one = stack.copyWithCount(1);
                insertCookingStack(one, true);
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (side != Direction.DOWN || slot != 0 || amount <= 0 || !completed) {
                return ItemStack.EMPTY;
            }
            ItemStack result = cookingStack().copyWithCount(1);
            if (!simulate) {
                extractCookingStack();
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && side != Direction.DOWN && canCook(stack);
        }
    }
}
