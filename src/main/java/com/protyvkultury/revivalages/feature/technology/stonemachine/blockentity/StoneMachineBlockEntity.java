package com.protyvkultury.revivalages.feature.technology.stonemachine.blockentity;

import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineFeature;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineKind;
import com.protyvkultury.revivalages.feature.technology.stonemachine.block.StoneMachineBlock;
import com.protyvkultury.revivalages.feature.technology.stonemachine.recipe.StoneMachineProcess;
import com.protyvkultury.revivalages.feature.technology.stonemachine.recipe.StoneMachineRecipeResolver;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class StoneMachineBlockEntity extends BlockEntity {

    private static final int FUEL_SLOT = 0;
    private static final int INPUT_SLOT = 1;
    private static final int BLADE_SLOT = 2;
    private static final int FIRST_OUTPUT_SLOT = 3;
    private static final int LAST_OUTPUT_SLOT = 11;
    private static final int DORMANT_TICKS = 50;

    private final NonNullList<ItemStack> items = NonNullList.withSize(12, ItemStack.EMPTY);
    private final StoneMachineKind kind;
    private final FluidTank tank = new FluidTank(PrimitiveTechnologyConfig.STONE_CRUCIBLE_CAPACITY.get()) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };

    private int burnTime;
    private int burnTimeTotal;
    private int elapsedTicks;
    private int totalTicks;
    private int dormantTicks = DORMANT_TICKS;
    private int sawmillIdleSoundTicks;
    private float airflowBonus;
    private double syncedWoodChipChance;

    public StoneMachineBlockEntity(BlockPos pos, BlockState state) {
        super(StoneMachineFeature.BLOCK_ENTITY.get(), pos, state);
        kind = ((StoneMachineBlock) state.getBlock()).kind();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StoneMachineBlockEntity machine) {
        machine.decayAirflow();
        if (!machine.isLit()) {
            machine.sawmillIdleSoundTicks = 0;
            return;
        }

        Optional<StoneMachineProcess> resolved = machine.resolveProcess();
        boolean canWork = resolved.filter(machine::canAcceptResult).isPresent();
        if (!canWork) {
            machine.elapsedTicks = 0;
            machine.totalTicks = 0;
            if (!PrimitiveTechnologyConfig.STONE_MACHINE_KEEP_HEAT.get()) {
                machine.extinguish();
                return;
            }
            machine.dormantTicks--;
            if (machine.dormantTicks <= 0) {
                machine.extinguish();
                return;
            }
        } else {
            machine.dormantTicks = DORMANT_TICKS;
        }

        int work = machine.workPerTick();
        for (int index = 0; index < work; index++) {
            if (!machine.consumeBurnTick()) {
                machine.extinguish();
                return;
            }
        }
        machine.playSawmillIdleSound();
        if (!canWork) {
            machine.syncPeriodically();
            return;
        }

        StoneMachineProcess process = resolved.orElseThrow();
        if (machine.totalTicks != process.processingTime()) {
            machine.elapsedTicks = 0;
            machine.totalTicks = process.processingTime();
        }
        machine.elapsedTicks += work;
        if (machine.elapsedTicks >= machine.totalTicks) {
            machine.complete(process);
        } else {
            machine.syncPeriodically();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, StoneMachineBlockEntity machine) {
        if (!machine.isLit()) {
            return;
        }
        if (level.random.nextInt(10) == 0) {
            level.playLocalSound(pos, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
        if (level.getGameTime() % 10L == 0L) {
            Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
            double x = pos.getX() + 0.5D + facing.getStepX() * 0.32D;
            double y = pos.getY() + 0.35D;
            double z = pos.getZ() + 0.5D + facing.getStepZ() * 0.32D;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.02D, 0.0D);
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.01D, 0.0D);
            if (PrimitiveTechnologyConfig.PROGRESS_PARTICLES.get() && !machine.input().isEmpty()) {
                level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5D,
                        pos.getY() + 1.55D,
                        pos.getZ() + 0.5D,
                        0.0D,
                        0.02D,
                        0.0D);
            }
        }
    }

    public StoneMachineKind kind() {
        return kind;
    }

    public ItemStack fuel() {
        StoneMachineBlockEntity root = root();
        return root == this ? items.get(FUEL_SLOT) : root.fuel();
    }

    public ItemStack input() {
        StoneMachineBlockEntity root = root();
        return root == this ? items.get(INPUT_SLOT) : root.input();
    }

    public ItemStack blade() {
        StoneMachineBlockEntity root = root();
        return root == this ? items.get(BLADE_SLOT) : root.blade();
    }

    public ItemStack firstOutput() {
        StoneMachineBlockEntity root = root();
        if (root != this) {
            return root.firstOutput();
        }
        for (int slot = FIRST_OUTPUT_SLOT; slot <= LAST_OUTPUT_SLOT; slot++) {
            if (!items.get(slot).isEmpty()) {
                return items.get(slot);
            }
        }
        return ItemStack.EMPTY;
    }

    public List<ItemStack> outputsForView() {
        StoneMachineBlockEntity root = root();
        if (root != this) {
            return root.outputsForView();
        }
        return items.subList(FIRST_OUTPUT_SLOT, LAST_OUTPUT_SLOT + 1).stream()
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
    }

    public FluidTank fluidTank() {
        StoneMachineBlockEntity root = root();
        return root == this ? tank : root.fluidTank();
    }

    public boolean isLit() {
        return getBlockState().getValue(StoneMachineBlock.LIT);
    }

    public double progress() {
        StoneMachineBlockEntity root = root();
        return root == this
                ? totalTicks <= 0 ? 0.0D : Math.min(1.0D, elapsedTicks / (double) totalTicks)
                : root.progress();
    }

    public int burnTime() {
        StoneMachineBlockEntity root = root();
        return root == this ? burnTime : root.burnTime();
    }

    public int burnTimeTotal() {
        StoneMachineBlockEntity root = root();
        return root == this ? burnTimeTotal : root.burnTimeTotal();
    }

    public float airflowBonus() {
        StoneMachineBlockEntity root = root();
        return root == this ? airflowBonus : root.airflowBonus();
    }

    public int elapsedTicks() {
        StoneMachineBlockEntity root = root();
        return root == this ? elapsedTicks : root.elapsedTicks();
    }

    public int totalTicks() {
        StoneMachineBlockEntity root = root();
        if (root != this) {
            return root.totalTicks();
        }
        return totalTicks > 0
                ? totalTicks
                : resolveProcess().map(StoneMachineProcess::processingTime).orElse(0);
    }

    public float recipeFailureChance() {
        return resolveProcess().map(StoneMachineProcess::failureChance).orElse(0.0F);
    }

    public int recipeWoodChips() {
        return resolveProcess().map(StoneMachineProcess::woodChips).orElse(0);
    }

    public double woodChipChanceForView() {
        return level != null && !level.isClientSide
                ? PrimitiveTechnologyConfig.STONE_SAWMILL_WOOD_CHIP_CHANCE.get()
                : syncedWoodChipChance;
    }

    public ItemStack recipeItemResult() {
        return resolveProcess().map(StoneMachineProcess::itemResult).orElse(ItemStack.EMPTY);
    }

    public FluidStack recipeFluidResult() {
        return resolveProcess().map(StoneMachineProcess::fluidResult).orElse(FluidStack.EMPTY);
    }

    public boolean canInsertFuel(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getBurnTime(null) > 0
                && compatible(items.get(FUEL_SLOT), stack)
                && items.get(FUEL_SLOT).getCount() < PrimitiveTechnologyConfig.STONE_MACHINE_FUEL_LIMIT.get();
    }

    public void insertFuel(ItemStack source, boolean infinite) {
        insertOne(FUEL_SLOT, source, infinite);
    }

    public ItemStack extractFuel() {
        return takeAll(FUEL_SLOT);
    }

    public boolean canInsertBlade(ItemStack stack) {
        return kind == StoneMachineKind.SAWMILL
                && items.get(BLADE_SLOT).isEmpty()
                && (stack.is(StoneMachineFeature.STONE_SAW_BLADE.get())
                        || stack.is(StoneMachineFeature.FLINT_SAW_BLADE.get())
                        || stack.is(StoneMachineFeature.BONE_SAW_BLADE.get()));
    }

    public void insertBlade(ItemStack source, boolean infinite, Player player) {
        if (isLit() && PrimitiveTechnologyConfig.STONE_SAWMILL_BLADE_DAMAGE.get() > 0.0D) {
            player.hurt(level.damageSources().generic(),
                    PrimitiveTechnologyConfig.STONE_SAWMILL_BLADE_DAMAGE.get().floatValue());
        }
        insertOne(BLADE_SLOT, source, infinite);
    }

    public ItemStack extractBlade(Player player) {
        ItemStack result = takeAll(BLADE_SLOT);
        if (isLit() && level != null && PrimitiveTechnologyConfig.STONE_SAWMILL_BLADE_DAMAGE.get() > 0.0D) {
            player.hurt(level.damageSources().hotFloor(),
                    PrimitiveTechnologyConfig.STONE_SAWMILL_BLADE_DAMAGE.get().floatValue());
        }
        return result;
    }

    public boolean canInsertInput(ItemStack stack) {
        if (level == null || stack.isEmpty() || !compatible(items.get(INPUT_SLOT), stack)
                || items.get(INPUT_SLOT).getCount() >= PrimitiveTechnologyConfig.STONE_MACHINE_INPUT_LIMIT.get()) {
            return false;
        }
        ItemStack simulated = items.get(INPUT_SLOT).isEmpty() ? stack.copyWithCount(1) : items.get(INPUT_SLOT).copy();
        if (!items.get(INPUT_SLOT).isEmpty()) {
            simulated.grow(1);
        }
        Optional<StoneMachineProcess> process = StoneMachineRecipeResolver.find(level, kind, simulated, blade());
        return process.filter(candidate -> canAcceptResult(candidate, simulated.getCount())).isPresent();
    }

    public void insertInput(ItemStack source, boolean infinite) {
        insertOne(INPUT_SLOT, source, infinite);
        elapsedTicks = 0;
        totalTicks = resolveProcess().map(StoneMachineProcess::processingTime).orElse(0);
        sync();
    }

    public ItemStack extractInput() {
        elapsedTicks = 0;
        totalTicks = 0;
        return takeAll(INPUT_SLOT);
    }

    public boolean ignite() {
        if (isLit() || (burnTime <= 0 && items.get(FUEL_SLOT).isEmpty())) {
            return false;
        }
        dormantTicks = DORMANT_TICKS;
        setLit(true);
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return true;
    }

    public void extinguish() {
        if (!isLit()) {
            return;
        }
        setLit(false);
        elapsedTicks = 0;
        totalTicks = 0;
        dormantTicks = DORMANT_TICKS;
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        sync();
    }

    public void receiveAirflow(float airflow, boolean simulate) {
        if (!simulate && airflow > 0.0F) {
            airflowBonus += airflow * PrimitiveTechnologyConfig.STONE_MACHINE_AIRFLOW_MULTIPLIER.get().floatValue();
            sync();
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
        sync();
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                Block.popResource(level, worldPosition.above(), stack);
            }
        }
        items.clear();
    }

    public IItemHandler itemHandler(@Nullable Direction side) {
        StoneMachineBlockEntity root = root();
        if (root != this) {
            return root.itemHandler(side);
        }
        if (side == Direction.UP) {
            return new InputHandler();
        }
        if (side == Direction.DOWN && kind != StoneMachineKind.CRUCIBLE) {
            return new OutputHandler();
        }
        if (side != null && side.getAxis().isHorizontal()) {
            return new FuelHandler();
        }
        return null;
    }

    public IFluidHandler fluidHandler(@Nullable Direction side) {
        StoneMachineBlockEntity root = root();
        if (root != this) {
            return root.fluidHandler(side);
        }
        return kind == StoneMachineKind.CRUCIBLE && side != Direction.UP ? tank : null;
    }

    private StoneMachineBlockEntity root() {
        if (getBlockState().getValue(StoneMachineBlock.HALF) == DoubleBlockHalf.UPPER
                && level != null
                && level.getBlockEntity(worldPosition.below()) instanceof StoneMachineBlockEntity lower) {
            return lower;
        }
        return this;
    }

    private boolean consumeBurnTick() {
        if (burnTime <= 0 && !consumeFuel()) {
            return false;
        }
        burnTime--;
        return true;
    }

    private boolean consumeFuel() {
        ItemStack fuel = items.get(FUEL_SLOT);
        if (fuel.isEmpty()) {
            return false;
        }
        int value = fuel.getBurnTime(null);
        if (value <= 0) {
            return false;
        }
        burnTimeTotal = Math.max(1, (int) Math.round(value
                * PrimitiveTechnologyConfig.STONE_MACHINE_FUEL_MULTIPLIER.get()));
        burnTime = burnTimeTotal;
        ItemStack remainder = fuel.getCraftingRemainingItem();
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            items.set(FUEL_SLOT, remainder);
        }
        sync();
        return true;
    }

    private int workPerTick() {
        int work = 1 + (int) airflowBonus;
        float fraction = airflowBonus - (int) airflowBonus;
        if (level != null && fraction > 0.0F && level.random.nextFloat() < fraction) {
            work++;
        }
        return work;
    }

    private void decayAirflow() {
        if (airflowBonus <= 0.0F) {
            return;
        }
        airflowBonus -= airflowBonus * PrimitiveTechnologyConfig.STONE_MACHINE_AIRFLOW_DRAG.get().floatValue();
        if (airflowBonus < 0.0001F) {
            airflowBonus = 0.0F;
        }
    }

    private Optional<StoneMachineProcess> resolveProcess() {
        return level == null
                ? Optional.empty()
                : StoneMachineRecipeResolver.find(level, kind, input(), blade());
    }

    private boolean canAcceptResult(StoneMachineProcess process) {
        return canAcceptResult(process, input().getCount());
    }

    private boolean canAcceptResult(StoneMachineProcess process, int inputCount) {
        if (kind == StoneMachineKind.CRUCIBLE) {
            FluidStack result = process.fluidResult();
            result.setAmount(result.getAmount() * inputCount);
            return tank.fill(result, IFluidHandler.FluidAction.SIMULATE) == result.getAmount();
        }
        if (kind == StoneMachineKind.KILN) {
            return freeOutputCapacity() >= inputCount;
        }
        ItemStack result = process.itemResult();
        int multiplier = kind == StoneMachineKind.OVEN ? inputCount : 1;
        result.setCount(result.getCount() * multiplier);
        return canInsertOutput(result);
    }

    private int freeOutputCapacity() {
        int capacity = 0;
        for (int slot = FIRST_OUTPUT_SLOT; slot <= LAST_OUTPUT_SLOT; slot++) {
            ItemStack stack = items.get(slot);
            capacity += stack.isEmpty() ? 64 : stack.getMaxStackSize() - stack.getCount();
        }
        return capacity;
    }

    private boolean canInsertOutput(ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = FIRST_OUTPUT_SLOT; slot <= LAST_OUTPUT_SLOT; slot++) {
            ItemStack current = items.get(slot);
            if (current.isEmpty()) {
                remaining.shrink(Math.min(remaining.getCount(), remaining.getMaxStackSize()));
            } else if (ItemStack.isSameItemSameComponents(current, remaining)) {
                remaining.shrink(Math.min(remaining.getCount(), current.getMaxStackSize() - current.getCount()));
            }
            if (remaining.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void complete(StoneMachineProcess process) {
        int inputCount = input().getCount();
        switch (kind) {
            case SAWMILL -> completeSawmill(process);
            case OVEN -> {
                ItemStack result = process.itemResult();
                result.setCount(result.getCount() * inputCount);
                items.set(INPUT_SLOT, ItemStack.EMPTY);
                insertOutput(result);
            }
            case KILN -> {
                items.set(INPUT_SLOT, ItemStack.EMPTY);
                for (int index = 0; index < inputCount; index++) {
                    insertOutput(randomKilnResult(process));
                }
            }
            case CRUCIBLE -> {
                FluidStack result = process.fluidResult();
                result.setAmount(result.getAmount() * inputCount);
                items.set(INPUT_SLOT, ItemStack.EMPTY);
                tank.fill(result, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        elapsedTicks = 0;
        totalTicks = 0;
        sync();
    }

    private void completeSawmill(StoneMachineProcess process) {
        items.get(INPUT_SLOT).shrink(1);
        if (items.get(INPUT_SLOT).isEmpty()) {
            items.set(INPUT_SLOT, ItemStack.EMPTY);
        }
        insertOutput(process.itemResult());
        if (PrimitiveTechnologyConfig.STONE_SAWMILL_DAMAGE_BLADES.get()) {
            ItemStack sawBlade = blade();
            sawBlade.setDamageValue(sawBlade.getDamageValue() + 1);
            if (sawBlade.getDamageValue() >= sawBlade.getMaxDamage()) {
                items.set(BLADE_SLOT, ItemStack.EMPTY);
                level.playSound(null, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        if (level instanceof ServerLevel server) {
            for (int index = 0; index < process.woodChips(); index++) {
                if (level.random.nextDouble() <= PrimitiveTechnologyConfig.STONE_SAWMILL_WOOD_CHIP_CHANCE.get()) {
                    Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(level.random);
                    Block.popResource(server, worldPosition.relative(direction).above(),
                            new ItemStack(PrimitiveMaterialsFeature.WOOD_CHIPS.get()));
                }
            }
            server.sendParticles(ParticleTypes.POOF,
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 1.4D,
                    worldPosition.getZ() + 0.5D,
                    8,
                    0.25D,
                    0.1D,
                    0.25D,
                    0.02D);
        }
        playSawmillCompletionSound();
    }

    private void playSawmillIdleSound() {
        if (level == null
                || kind != StoneMachineKind.SAWMILL
                || blade().isEmpty()
                || !PrimitiveTechnologyConfig.STONE_SAWMILL_IDLE_SOUND_ENABLED.get()) {
            sawmillIdleSoundTicks = 0;
            return;
        }
        sawmillIdleSoundTicks--;
        if (sawmillIdleSoundTicks > 0) {
            return;
        }
        sawmillIdleSoundTicks = 40;
        level.playSound(
                null,
                worldPosition,
                StoneMachineFeature.SAWMILL_IDLE.get(),
                SoundSource.BLOCKS,
                PrimitiveTechnologyConfig.STONE_SAWMILL_IDLE_SOUND_VOLUME.get().floatValue(),
                1.0F);
    }

    private void playSawmillCompletionSound() {
        if (level == null || !PrimitiveTechnologyConfig.STONE_SAWMILL_COMPLETE_SOUND_ENABLED.get()) {
            return;
        }
        var sound = StoneMachineFeature.SAWMILL_ACTIVE.get();
        if (!input().isEmpty()) {
            double selection = level.random.nextDouble();
            if (selection < 0.49D) {
                sound = StoneMachineFeature.SAWMILL_ACTIVE_SHORT_A.get();
            } else if (selection < 0.98D) {
                sound = StoneMachineFeature.SAWMILL_ACTIVE_SHORT_B.get();
            } else {
                return;
            }
        }
        float pitch = 1.0F + (level.random.nextFloat() * 2.0F - 1.0F) * 0.05F;
        level.playSound(
                null,
                worldPosition,
                sound,
                SoundSource.BLOCKS,
                PrimitiveTechnologyConfig.STONE_SAWMILL_COMPLETE_SOUND_VOLUME.get().floatValue(),
                pitch);
    }

    private ItemStack randomKilnResult(StoneMachineProcess process) {
        if (level != null && level.random.nextFloat() < process.failureChance()) {
            List<ItemStack> failures = process.failureResults();
            if (!failures.isEmpty()) {
                return failures.get(level.random.nextInt(failures.size())).copyWithCount(1);
            }
            return new ItemStack(PrimitiveMaterialsFeature.PIT_ASH.get());
        }
        return process.itemResult().copyWithCount(1);
    }

    private void insertOutput(ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = FIRST_OUTPUT_SLOT; slot <= LAST_OUTPUT_SLOT && !remaining.isEmpty(); slot++) {
            ItemStack current = items.get(slot);
            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, remaining)) {
                int moved = Math.min(remaining.getCount(), current.getMaxStackSize() - current.getCount());
                current.grow(moved);
                remaining.shrink(moved);
            }
        }
        for (int slot = FIRST_OUTPUT_SLOT; slot <= LAST_OUTPUT_SLOT && !remaining.isEmpty(); slot++) {
            if (items.get(slot).isEmpty()) {
                int moved = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                items.set(slot, remaining.copyWithCount(moved));
                remaining.shrink(moved);
            }
        }
        if (!remaining.isEmpty() && level != null) {
            Block.popResource(level, worldPosition.above(), remaining);
        }
    }

    private void insertOne(int slot, ItemStack source, boolean infinite) {
        ItemStack current = items.get(slot);
        if (current.isEmpty()) {
            items.set(slot, source.copyWithCount(1));
        } else {
            current.grow(1);
        }
        if (!infinite) {
            source.shrink(1);
        }
        sync();
    }

    private ItemStack takeAll(int slot) {
        ItemStack result = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        sync();
        return result;
    }

    private static boolean compatible(ItemStack current, ItemStack offered) {
        return current.isEmpty() || ItemStack.isSameItemSameComponents(current, offered);
    }

    private void setLit(boolean lit) {
        if (level == null) {
            return;
        }
        BlockState lower = level.getBlockState(worldPosition);
        if (lower.is(getBlockState().getBlock())) {
            level.setBlock(worldPosition, lower.setValue(StoneMachineBlock.LIT, lit), Block.UPDATE_ALL);
        }
        BlockPos upperPos = worldPosition.above();
        BlockState upper = level.getBlockState(upperPos);
        if (upper.is(getBlockState().getBlock())) {
            level.setBlock(upperPos, upper.setValue(StoneMachineBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    private void syncPeriodically() {
        setChanged();
        if (level != null && level.getGameTime() % 10L == 0L) {
            sync();
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
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        tank.readFromNBT(registries, tag.getCompound("Tank"));
        burnTime = tag.getInt("BurnTime");
        burnTimeTotal = tag.getInt("BurnTimeTotal");
        elapsedTicks = tag.getInt("ElapsedTicks");
        totalTicks = tag.getInt("TotalTicks");
        dormantTicks = tag.contains("DormantTicks") ? tag.getInt("DormantTicks") : DORMANT_TICKS;
        airflowBonus = tag.getFloat("AirflowBonus");
        syncedWoodChipChance = tag.getDouble("WoodChipChance");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTimeTotal", burnTimeTotal);
        tag.putInt("ElapsedTicks", elapsedTicks);
        tag.putInt("TotalTicks", totalTicks);
        tag.putInt("DormantTicks", dormantTicks);
        tag.putFloat("AirflowBonus", airflowBonus);
        tag.putDouble("WoodChipChance", PrimitiveTechnologyConfig.STONE_SAWMILL_WOOD_CHIP_CHANCE.get());
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private final class FuelHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? fuel().copy() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || !canInsertFuel(stack)) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            if (!simulate) {
                insertFuel(stack.copyWithCount(1), true);
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || amount <= 0 || fuel().isEmpty()) {
                return ItemStack.EMPTY;
            }
            int extracted = Math.min(amount, fuel().getCount());
            ItemStack result = fuel().copyWithCount(extracted);
            if (!simulate) {
                fuel().shrink(extracted);
                if (fuel().isEmpty()) {
                    items.set(FUEL_SLOT, ItemStack.EMPTY);
                }
                sync();
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return PrimitiveTechnologyConfig.STONE_MACHINE_FUEL_LIMIT.get();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && stack.getBurnTime(null) > 0;
        }
    }

    private final class InputHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? input().copy() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || !canInsertInput(stack)) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            if (!simulate) {
                insertInput(stack.copyWithCount(1), true);
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || amount <= 0 || input().isEmpty()) {
                return ItemStack.EMPTY;
            }
            int extracted = Math.min(amount, input().getCount());
            ItemStack result = input().copyWithCount(extracted);
            if (!simulate) {
                input().shrink(extracted);
                if (input().isEmpty()) {
                    items.set(INPUT_SLOT, ItemStack.EMPTY);
                }
                elapsedTicks = 0;
                totalTicks = 0;
                sync();
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return PrimitiveTechnologyConfig.STONE_MACHINE_INPUT_LIMIT.get();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && canInsertInput(stack);
        }
    }

    private final class OutputHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return LAST_OUTPUT_SLOT - FIRST_OUTPUT_SLOT + 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return validOutputSlot(slot) ? items.get(FIRST_OUTPUT_SLOT + slot).copy() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!validOutputSlot(slot) || amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack current = items.get(FIRST_OUTPUT_SLOT + slot);
            if (current.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int extracted = Math.min(amount, current.getCount());
            ItemStack result = current.copyWithCount(extracted);
            if (!simulate) {
                current.shrink(extracted);
                if (current.isEmpty()) {
                    items.set(FIRST_OUTPUT_SLOT + slot, ItemStack.EMPTY);
                }
                sync();
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        private boolean validOutputSlot(int slot) {
            return slot >= 0 && slot < getSlots();
        }
    }
}
