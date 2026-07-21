package com.protyvkultury.revivalages.feature.technology.soakingpot.blockentity;

import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.block.CampfireBlock;
import com.protyvkultury.revivalages.feature.technology.soakingpot.SoakingPotFeature;
import com.protyvkultury.revivalages.feature.technology.soakingpot.recipe.SoakingPotRecipe;
import com.protyvkultury.revivalages.feature.technology.soakingpot.recipe.SoakingRecipeInput;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class SoakingPotBlockEntity extends BlockEntity {

    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private int outputCount;
    private final FluidTank tank = new FluidTank(PrimitiveTechnologyConfig.SOAKING_POT_CAPACITY.get()) {
        @Override
        protected void onContentsChanged() {
            resolveRecipe();
            ejectFluidOverfill();
            sync();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!resource.isEmpty()
                    && !PrimitiveTechnologyConfig.WOODEN_CONTAINERS_HOLD_HOT_FLUIDS.get()
                    && resource.getFluidType().getTemperature(resource) >= PrimitiveTechnologyConfig.HOT_FLUID_TEMPERATURE.get()) {
                if (action.execute()) {
                    breakForHotFluid(resource);
                }
                return 0;
            }
            return super.fill(resource, action);
        }
    };
    private int elapsedTicks;
    private int totalTicks;
    private SoakingPotRecipe activeRecipe;

    public SoakingPotBlockEntity(BlockPos pos, BlockState state) {
        super(SoakingPotFeature.BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SoakingPotBlockEntity pot) {
        pot.resolveRecipe();
        if (pot.activeRecipe == null || pot.input.isEmpty() || !pot.output.isEmpty()) {
            pot.elapsedTicks = 0;
            pot.totalTicks = 0;
            return;
        }
        int required = pot.activeRecipe.inputFluid().getAmount() * pot.input.getCount();
        if (pot.tank.getFluidAmount() < required) {
            return;
        }
        if (pot.activeRecipe.requiresCampfire()) {
            BlockState below = level.getBlockState(pos.below());
            if (!below.is(CampfireFeature.CAMPFIRE.get()) || !below.getValue(CampfireBlock.LIT)) {
                return;
            }
        }
        pot.totalTicks = Math.max(1, (int) Math.round(pot.activeRecipe.processingTime()
                * PrimitiveTechnologyConfig.SOAKING_POT_DURATION_MULTIPLIER.get()));
        pot.elapsedTicks++;
        if (pot.elapsedTicks >= pot.totalTicks) {
            pot.completeRecipe();
        } else {
            pot.setChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SoakingPotBlockEntity pot) {
        if (pot.activeRecipe != null
                && !pot.input.isEmpty()
                && (!pot.activeRecipe.requiresCampfire()
                || (level.getBlockState(pos.below()).is(CampfireFeature.CAMPFIRE.get())
                && level.getBlockState(pos.below()).getValue(CampfireBlock.LIT)))
                && PrimitiveTechnologyConfig.PROGRESS_PARTICLES.get()
                && level.getGameTime() % 40L == 0L) {
            double y = level.getBlockState(pos.below()).is(CampfireFeature.CAMPFIRE.get()) ? 0.5D : 0.75D;
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5D, pos.getY() + y, pos.getZ() + 0.5D,
                    0.0D, 0.02D, 0.0D);
        }
    }

    public ItemStack input() {
        return input;
    }

    public ItemStack output() {
        return output.isEmpty()
                ? ItemStack.EMPTY
                : output.copyWithCount(Math.min(output.getMaxStackSize(), outputCount));
    }

    public FluidTank fluidTank() {
        return tank;
    }

    public double progress() {
        return totalTicks <= 0 ? 0.0D : Math.min(1.0D, elapsedTicks / (double) totalTicks);
    }

    public ItemStack recipeOutput() {
        return activeRecipe == null ? ItemStack.EMPTY : activeRecipe.result();
    }

    public boolean requiresCampfire() {
        return activeRecipe != null && activeRecipe.requiresCampfire();
    }

    public boolean canInsert(ItemStack stack) {
        if (!output.isEmpty() || stack.isEmpty()) {
            return false;
        }
        Optional<SoakingPotRecipe> recipe = findRecipe(stack);
        if (recipe.isEmpty()) {
            return false;
        }
        if (!input.isEmpty() && !ItemStack.isSameItemSameComponents(input, stack)) {
            return false;
        }
        int maximum = Math.min(
                PrimitiveTechnologyConfig.SOAKING_POT_MAX_STACK_SIZE.get(),
                tank.getFluidAmount() / recipe.get().inputFluid().getAmount()
        );
        return input.getCount() < maximum;
    }

    public void insert(ItemStack source, boolean infinite, boolean fullStack) {
        Optional<SoakingPotRecipe> recipe = findRecipe(source);
        if (recipe.isEmpty() || !canInsert(source)) {
            return;
        }
        int perItem = recipe.get().inputFluid().getAmount();
        int maximum = Math.min(
                PrimitiveTechnologyConfig.SOAKING_POT_MAX_STACK_SIZE.get(),
                tank.getFluidAmount() / perItem
        );
        int remainingCapacity = maximum - input.getCount();
        int amount = fullStack
                ? Math.min(source.getCount(), remainingCapacity)
                : Math.min(1, remainingCapacity);
        if (amount <= 0) {
            return;
        }
        if (input.isEmpty()) {
            input = source.copyWithCount(amount);
        } else {
            input.grow(amount);
        }
        if (!infinite) {
            source.shrink(amount);
        }
        activeRecipe = recipe.get();
        elapsedTicks = 0;
        totalTicks = 0;
        sync();
    }

    public ItemStack extractInput() {
        ItemStack result = input;
        input = ItemStack.EMPTY;
        elapsedTicks = 0;
        totalTicks = 0;
        activeRecipe = null;
        sync();
        return result;
    }

    public ItemStack extractOutput() {
        if (output.isEmpty() || outputCount <= 0) {
            return ItemStack.EMPTY;
        }
        int count = Math.min(output.getMaxStackSize(), outputCount);
        ItemStack result = output.copyWithCount(count);
        outputCount -= count;
        if (outputCount <= 0) {
            output = ItemStack.EMPTY;
            outputCount = 0;
            resolveRecipe();
        }
        sync();
        return result;
    }

    public IItemHandler itemHandler(@Nullable Direction side) {
        return new PotItemHandler(side);
    }

    private Optional<SoakingPotRecipe> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty() || tank.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(
                SoakingPotFeature.RECIPE_TYPE.get(),
                new SoakingRecipeInput(stack, tank.getFluid()),
                level
        ).map(net.minecraft.world.item.crafting.RecipeHolder::value);
    }

    private void resolveRecipe() {
        activeRecipe = findRecipe(input).orElse(null);
    }

    private void completeRecipe() {
        if (activeRecipe == null) {
            return;
        }
        SoakingPotRecipe completedRecipe = activeRecipe;
        int count = input.getCount();
        input = ItemStack.EMPTY;
        tank.drain(completedRecipe.inputFluid().getAmount() * count, IFluidHandler.FluidAction.EXECUTE);
        ItemStack recipeOutput = completedRecipe.result();
        outputCount = recipeOutput.getCount() * count;
        output = recipeOutput.copyWithCount(1);
        elapsedTicks = 0;
        totalTicks = 0;
        activeRecipe = null;
        sync();
    }

    private void ejectFluidOverfill() {
        if (level == null || level.isClientSide || activeRecipe == null || input.isEmpty()) {
            return;
        }
        int maximum = tank.getFluidAmount() / activeRecipe.inputFluid().getAmount();
        if (input.getCount() <= maximum) {
            return;
        }
        int ejectedCount = input.getCount() - maximum;
        ItemStack ejected = input.copyWithCount(ejectedCount);
        input.shrink(ejectedCount);
        Block.popResource(level, worldPosition.above(), ejected);
        elapsedTicks = 0;
        if (input.isEmpty()) {
            activeRecipe = null;
            totalTicks = 0;
        }
    }

    private void breakForHotFluid(FluidStack resource) {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState fluidState = resource.getFluid().defaultFluidState().createLegacyBlock();
        level.setBlock(worldPosition, fluidState.isAir() ? Blocks.AIR.defaultBlockState() : fluidState, Block.UPDATE_ALL);
        level.playSound(null, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        Block.popResource(level, worldPosition, input);
        while (!output.isEmpty() && outputCount > 0) {
            int count = Math.min(output.getMaxStackSize(), outputCount);
            Block.popResource(level, worldPosition, output.copyWithCount(count));
            outputCount -= count;
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
        input = ItemStack.parseOptional(registries, tag.getCompound("Input"));
        output = ItemStack.parseOptional(registries, tag.getCompound("Output"));
        outputCount = tag.contains("OutputCount")
                ? Math.max(0, tag.getInt("OutputCount"))
                : output.getCount();
        if (!output.isEmpty()) {
            output.setCount(1);
        }
        tank.readFromNBT(registries, tag.getCompound("Tank"));
        elapsedTicks = tag.getInt("ElapsedTicks");
        totalTicks = tag.getInt("TotalTicks");
        resolveRecipe();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!input.isEmpty()) {
            tag.put("Input", input.save(registries));
        }
        if (!output.isEmpty()) {
            tag.put("Output", output.save(registries));
            tag.putInt("OutputCount", outputCount);
        }
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("ElapsedTicks", elapsedTicks);
        tag.putInt("TotalTicks", totalTicks);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private final class PotItemHandler implements IItemHandler {

        private final Direction side;

        private PotItemHandler(@Nullable Direction side) {
            this.side = side == null ? Direction.UP : side;
        }

        @Override
        public int getSlots() {
            return 2;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? input.copy() : slot == 1 ? output() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || side == Direction.DOWN || !canInsert(stack)) {
                return stack;
            }
            Optional<SoakingPotRecipe> recipe = findRecipe(stack);
            int amount = recipe.map(value -> Math.min(stack.getCount(), Math.min(
                    PrimitiveTechnologyConfig.SOAKING_POT_MAX_STACK_SIZE.get(),
                    tank.getFluidAmount() / value.inputFluid().getAmount()
            ) - input.getCount())).orElse(0);
            if (amount <= 0) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(amount);
            if (!simulate) {
                ItemStack toInsert = stack.copyWithCount(amount);
                insert(toInsert, true, true);
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 1 || side != Direction.DOWN || amount <= 0 || output.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int count = Math.min(amount, Math.min(output.getMaxStackSize(), outputCount));
            ItemStack result = output.copyWithCount(count);
            if (!simulate) {
                outputCount -= count;
                if (outputCount <= 0) {
                    output = ItemStack.EMPTY;
                    outputCount = 0;
                    resolveRecipe();
                }
                sync();
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? PrimitiveTechnologyConfig.SOAKING_POT_MAX_STACK_SIZE.get() : Integer.MAX_VALUE;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && side != Direction.DOWN && findRecipe(stack).isPresent();
        }
    }
}
