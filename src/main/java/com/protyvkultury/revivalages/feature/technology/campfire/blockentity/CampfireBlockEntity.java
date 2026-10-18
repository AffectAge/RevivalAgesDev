package com.protyvkultury.revivalages.feature.technology.campfire.blockentity;

import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
import com.protyvkultury.revivalages.core.particle.ProgressParticleHelper;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.block.CampfireBlock;
import com.protyvkultury.revivalages.feature.technology.campfire.recipe.CampfireRecipeResolver;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
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

    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private boolean hasTinder;
    private boolean dead;
    private boolean lit;
    private boolean completed;
    private boolean burned;
    private int ash;
    private int burnTime;
    private int rainTicks;
    private int burnOutputTicks;
    private double progress;
    private int totalTime;
    private ResourceLocation recipeId;
    private ItemStack recipeOutput = ItemStack.EMPTY;
    private ItemStack cookingInput = ItemStack.EMPTY;
    private long clientSnapshotTime = -1L;
    private int clientFuelTicks;
    private double clientProgressSpeed;
    private int clientBurnedFoodTicks;

    public CampfireBlockEntity(BlockPos pos, BlockState state) {
        super(CampfireFeature.BLOCK_ENTITY.get(), pos, state);
        lit = state.getValue(CampfireBlock.LIT);
        ash = state.getValue(CampfireBlock.ASH);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        if (!ContentAvailability.isEnabled(ContentKey.CAMPFIRE)) {
            return;
        }
        ItemStack cooking = campfire.items.get(COOKING_SLOT);
        ItemStack materialized = FoodFreshnessApi.materialize(cooking);
        if (materialized != cooking) {
            campfire.items.set(COOKING_SLOT, materialized);
            campfire.completed = false;
            campfire.burned = false;
            campfire.progress = 0.0D;
            campfire.cookingInput = materialized.copyWithCount(1);
            campfire.resolveRecipe();
            campfire.sync();
        }
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
        campfire.setChanged();
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (level.random.nextDouble() < PrimitiveTechnologyConfig.CAMPFIRE_FLOOR_IGNITION_CHANCE.get()
                && belowState.isFlammable(level, below, Direction.UP)) {
            level.setBlock(below, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        if (campfire.completed) {
            if (campfire.burned) {
                return;
            }
            campfire.burnOutputTicks++;
            if (campfire.burnOutputTicks >= PrimitiveTechnologyConfig.CAMPFIRE_BURNED_FOOD_TICKS.get()) {
                campfire.items.set(COOKING_SLOT, new ItemStack(PrimitiveMaterialsFeature.BURNED_FOOD.get()));
                campfire.burnOutputTicks = 0;
                campfire.burned = true;
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
        double fuelSpeed = campfire.cookingSpeed();
        campfire.progress += fuelSpeed;
        if (campfire.progress >= campfire.totalTime) {
            ItemStack input = campfire.items.get(COOKING_SLOT).copy();
            ItemStack output = campfire.recipeOutput.copy();
            if (campfire.recipeId != null) {
                FoodFreshnessApi.transformOutput(output, List.of(input), campfire.recipeId);
            } else {
                FoodFreshnessApi.copyOldest(output, List.of(input));
            }
            campfire.items.set(COOKING_SLOT, output);
            campfire.completed = true;
            campfire.burned = false;
            campfire.progress = campfire.totalTime;
            campfire.burnOutputTicks = 0;
            campfire.sync();
        } else {
            campfire.setChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        if (!ContentAvailability.isEnabled(ContentKey.CAMPFIRE) || !state.getValue(CampfireBlock.LIT)) {
            return;
        }
        if (level.random.nextInt(10) == 0) {
            level.playLocalSound(pos, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS,
                    1.0F, 1.0F, false);
        }
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 4.0D / 16.0D + level.random.nextDouble() * 2.0D / 16.0D;
        double centerZ = pos.getZ() + 0.5D;
        for (int index = 0; index < 4; index++) {
            double x = centerX + (level.random.nextDouble() * 2.0D - 1.0D) * 0.2D;
            double z = centerZ + (level.random.nextDouble() * 2.0D - 1.0D) * 0.2D;
            level.addParticle(ParticleTypes.FLAME, x, centerY, z, 0.0D, 0.0D, 0.0D);
        }
        if (PrimitiveTechnologyConfig.PROGRESS_PARTICLES.get()
                && campfire.isProcessing()
                && level.getGameTime() % ProgressParticleHelper.INTERVAL == 0L) {
            ProgressParticleHelper.spawn(level, centerX, pos.getY() + 0.55D, centerZ, 0.2D, 0.1D, 0.2D);
        }
        if (campfire.burned) {
            for (int index = 0; index < 8; index++) {
                level.addParticle(
                        ParticleTypes.LARGE_SMOKE,
                        centerX + (level.random.nextDouble() * 2.0D - 1.0D) * 0.2D,
                        centerY,
                        centerZ + (level.random.nextDouble() * 2.0D - 1.0D) * 0.2D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }
    }

    public void setHasTinder(boolean value) {
        hasTinder = value;
        dead = false;
        sync();
    }

    public boolean canIgnite() {
        return ContentAvailability.isEnabled(ContentKey.CAMPFIRE)
                && hasTinder && !dead && !lit && fuelLevel() > 0 && ash < 8;
    }

    public void ignite() {
        if (!canIgnite()) {
            return;
        }
        lit = true;
        updateState();
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
        for (int slot = LAST_LOG_SLOT; slot >= FIRST_LOG_SLOT; slot--) {
            if (!items.get(slot).isEmpty()) {
                items.set(slot, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
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

    public int activeFuelLevel() {
        return Math.min(8, fuelLevel() + (burnTime > 0 ? 1 : 0));
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
        cookingInput = source.copyWithCount(1);
        if (!infinite) {
            source.shrink(1);
        }
        completed = false;
        burned = false;
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
        burned = false;
        cookingInput = ItemStack.EMPTY;
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

    public double progressAt(long gameTime) {
        if (totalTime <= 0) {
            return 0.0D;
        }
        double predicted = progress;
        if (level != null && level.isClientSide && clientSnapshotTime >= 0L && isProcessing()) {
            predicted += Math.max(0L, gameTime - clientSnapshotTime) * clientProgressSpeed;
        }
        return Math.clamp(predicted / totalTime, 0.0D, 1.0D);
    }

    public double burnProgressAt(long gameTime) {
        if (!completed) {
            return 0.0D;
        }
        if (burned) {
            return 1.0D;
        }
        int duration = level != null && level.isClientSide && clientBurnedFoodTicks > 0
                ? clientBurnedFoodTicks
                : PrimitiveTechnologyConfig.CAMPFIRE_BURNED_FOOD_TICKS.get();
        long elapsed = level != null && level.isClientSide && clientSnapshotTime >= 0L && lit
                ? Math.max(0L, gameTime - clientSnapshotTime)
                : 0L;
        return Math.clamp((burnOutputTicks + elapsed) / (double) Math.max(1, duration), 0.0D, 1.0D);
    }

    public int remainingFuelTicksAt(long gameTime) {
        int ticks = level != null && level.isClientSide && clientSnapshotTime >= 0L
                ? clientFuelTicks
                : remainingFuelTicks();
        if (level != null && level.isClientSide && clientSnapshotTime >= 0L && lit) {
            ticks -= (int) Math.min(Integer.MAX_VALUE, Math.max(0L, gameTime - clientSnapshotTime));
        }
        return Math.max(0, ticks);
    }

    public boolean isProcessing() {
        return lit && !completed && !recipeOutput.isEmpty() && ash < 8;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isBurned() {
        return burned;
    }

    public ItemStack cookingInput() {
        return cookingInput.copy();
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

    private int remainingFuelTicks() {
        long total = (long) burnTime
                + (long) fuelLevel() * PrimitiveTechnologyConfig.CAMPFIRE_BURN_TICKS_PER_LOG.get();
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, total));
    }

    private double cookingSpeed() {
        return Math.min(2.0D, activeFuelLevel()
                / (double) PrimitiveTechnologyConfig.CAMPFIRE_FULL_SPEED_FUEL_LEVEL.get());
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
        double fuelRatio = activeFuelLevel() / 8.0D;
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
        burned = tag.contains("Burned") ? tag.getBoolean("Burned")
                : completed && items.get(COOKING_SLOT).is(PrimitiveMaterialsFeature.BURNED_FOOD.get());
        ash = tag.getInt("Ash");
        burnTime = tag.getInt("BurnTime");
        rainTicks = tag.getInt("RainTicks");
        burnOutputTicks = tag.getInt("BurnOutputTicks");
        progress = tag.getDouble("Progress");
        totalTime = tag.getInt("TotalTime");
        String id = tag.getString("Recipe");
        recipeId = id.isEmpty() ? null : ResourceLocation.tryParse(id);
        recipeOutput = ItemStack.parseOptional(registries, tag.getCompound("RecipeOutput"));
        cookingInput = ItemStack.parseOptional(registries, tag.getCompound("CookingInput"));
        if (cookingInput.isEmpty() && !completed && !items.get(COOKING_SLOT).isEmpty()) {
            cookingInput = items.get(COOKING_SLOT).copyWithCount(1);
        }
        clientSnapshotTime = tag.contains("SnapshotGameTime") ? tag.getLong("SnapshotGameTime") : -1L;
        clientFuelTicks = tag.getInt("FuelTicksSnapshot");
        clientProgressSpeed = tag.getDouble("ProgressSpeedSnapshot");
        clientBurnedFoodTicks = tag.getInt("BurnedFoodTicksSnapshot");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
        tag.putBoolean("HasTinder", hasTinder);
        tag.putBoolean("Dead", dead);
        tag.putBoolean("Lit", lit);
        tag.putBoolean("Completed", completed);
        tag.putBoolean("Burned", burned);
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
        if (!cookingInput.isEmpty()) {
            tag.put("CookingInput", cookingInput.save(registries));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = saveWithoutMetadata(registries);
        if (level != null) {
            tag.putLong("SnapshotGameTime", level.getGameTime());
            tag.putInt("FuelTicksSnapshot", remainingFuelTicks());
            tag.putDouble("ProgressSpeedSnapshot", cookingSpeed());
            tag.putInt("BurnedFoodTicksSnapshot", PrimitiveTechnologyConfig.CAMPFIRE_BURNED_FOOD_TICKS.get());
        }
        return tag;
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
