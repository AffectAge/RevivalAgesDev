package com.protyvkultury.revivalages.feature.technology.barrel.blockentity;

import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import com.protyvkultury.revivalages.feature.technology.barrel.block.BarrelBlock;
import com.protyvkultury.revivalages.feature.technology.barrel.recipe.BarrelRecipe;
import com.protyvkultury.revivalages.feature.technology.barrel.recipe.BarrelRecipeInput;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class BarrelBlockEntity extends BlockEntity {

    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final FluidTank tank = new FluidTank(PrimitiveTechnologyConfig.BARREL_CAPACITY.get()) {
        @Override
        protected void onContentsChanged() {
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
    private int rainFillTicks;
    private int rainConversionTicks;
    private BarrelRecipe activeRecipe;

    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(BarrelFeature.BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BarrelBlockEntity barrel) {
        if (!state.getValue(BarrelBlock.SEALED)) {
            barrel.collectRain(level, pos);
            return;
        }
        barrel.resolveRecipe();
        if (barrel.activeRecipe == null) {
            barrel.elapsedTicks = 0;
            barrel.totalTicks = 0;
            return;
        }
        barrel.totalTicks = Math.max(1, (int) Math.round(barrel.activeRecipe.processingTime()
                * PrimitiveTechnologyConfig.BARREL_DURATION_MULTIPLIER.get()));
        barrel.elapsedTicks++;
        if (barrel.elapsedTicks >= barrel.totalTicks) {
            barrel.completeRecipe();
        } else {
            barrel.setChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BarrelBlockEntity barrel) {
        if (state.getValue(BarrelBlock.SEALED)
                && barrel.activeRecipe != null
                && PrimitiveTechnologyConfig.PROGRESS_PARTICLES.get()
                && level.getGameTime() % 40L == 0L) {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D,
                    0.0D, 0.02D, 0.0D);
        }
    }

    public FluidTank fluidTank() {
        return tank;
    }

    public ItemStack item(int slot) {
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }

    public int slotFromHit(double x, double z) {
        return (z >= 0.5D ? 2 : 0) + (x >= 0.5D ? 1 : 0);
    }

    public boolean canInsert(int slot, ItemStack stack) {
        if (slot < 0 || slot >= 4 || !items.get(slot).isEmpty() || stack.isEmpty() || tank.isEmpty()) {
            return false;
        }
        return level != null && level.getRecipeManager().getAllRecipesFor(BarrelFeature.RECIPE_TYPE.get()).stream()
                .map(net.minecraft.world.item.crafting.RecipeHolder::value)
                .anyMatch(recipe -> FluidStack.isSameFluidSameComponents(recipe.inputFluid(), tank.getFluid())
                        && recipe.acceptsItem(stack));
    }

    public void insert(int slot, ItemStack source, boolean infinite) {
        if (!canInsert(slot, source)) {
            return;
        }
        items.set(slot, source.copyWithCount(1));
        if (!infinite) {
            source.shrink(1);
        }
        resolveRecipe();
        sync();
    }

    public ItemStack extract(int slot) {
        if (slot < 0 || slot >= 4) {
            return ItemStack.EMPTY;
        }
        ItemStack result = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        elapsedTicks = 0;
        totalTicks = 0;
        resolveRecipe();
        sync();
        return result;
    }

    public boolean seal() {
        if (level == null || getBlockState().getValue(BarrelBlock.SEALED)) {
            return false;
        }
        level.setBlock(worldPosition, getBlockState().setValue(BarrelBlock.SEALED, true), Block.UPDATE_ALL);
        resolveRecipe();
        sync();
        return true;
    }

    public void unseal(Player player) {
        if (level == null || !getBlockState().getValue(BarrelBlock.SEALED)) {
            return;
        }
        ItemStack lid = new ItemStack(BarrelFeature.BARREL_LID.get());
        if (!player.addItem(lid)) {
            Block.popResource(level, worldPosition.above(), lid);
        }
        elapsedTicks = 0;
        totalTicks = 0;
        level.setBlock(worldPosition, getBlockState().setValue(BarrelBlock.SEALED, false), Block.UPDATE_ALL);
        sync();
    }

    public double progress() {
        return totalTicks <= 0 ? 0.0D : Math.min(1.0D, elapsedTicks / (double) totalTicks);
    }

    public ItemStack[] itemsForView() {
        return items.stream().map(ItemStack::copy).toArray(ItemStack[]::new);
    }

    public FluidStack recipeOutput() {
        return activeRecipe == null ? FluidStack.EMPTY : activeRecipe.resultFluid();
    }

    public IItemHandler itemHandler(@Nullable Direction side) {
        return new BarrelItemHandler(side);
    }

    private void collectRain(Level level, BlockPos pos) {
        if (!level.isRainingAt(pos.above())) {
            rainFillTicks = 0;
            rainConversionTicks = 0;
            return;
        }
        int conversion = PrimitiveTechnologyConfig.BARREL_RAIN_CONVERSION_INTERVAL.get();
        if (conversion > 0 && !tank.isEmpty() && !tank.getFluid().is(Fluids.WATER)) {
            rainConversionTicks++;
        } else {
            rainConversionTicks = 0;
        }
        if (conversion > 0 && rainConversionTicks >= conversion) {
            tank.setFluid(new FluidStack(Fluids.WATER, tank.getFluidAmount()));
            rainConversionTicks = 0;
        }
        int fill = PrimitiveTechnologyConfig.BARREL_RAIN_FILL_INTERVAL.get();
        if (fill > 0) {
            rainFillTicks++;
        } else {
            rainFillTicks = 0;
        }
        if (fill > 0 && rainFillTicks >= fill) {
            tank.fill(new FluidStack(Fluids.WATER, 5), IFluidHandler.FluidAction.EXECUTE);
            rainFillTicks = 0;
        }
    }

    private void resolveRecipe() {
        if (level == null) {
            activeRecipe = null;
            return;
        }
        BarrelRecipeInput input = new BarrelRecipeInput(items, tank.getFluid());
        activeRecipe = level.getRecipeManager().getRecipeFor(BarrelFeature.RECIPE_TYPE.get(), input, level)
                .map(net.minecraft.world.item.crafting.RecipeHolder::value)
                .orElse(null);
    }

    private void completeRecipe() {
        if (activeRecipe == null) {
            return;
        }
        for (int slot = 0; slot < 4; slot++) {
            items.set(slot, ItemStack.EMPTY);
        }
        tank.setFluid(activeRecipe.resultFluid());
        elapsedTicks = 0;
        totalTicks = 0;
        activeRecipe = null;
        sync();
    }

    private void breakForHotFluid(FluidStack resource) {
        if (level == null || level.isClientSide) {
            return;
        }
        dropContents();
        BlockState fluidState = resource.getFluid().defaultFluidState().createLegacyBlock();
        level.setBlock(worldPosition, fluidState.isAir() ? Blocks.AIR.defaultBlockState() : fluidState, Block.UPDATE_ALL);
        level.playSound(null, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int slot = 0; slot < items.size(); slot++) {
            Block.popResource(level, worldPosition, items.get(slot));
            items.set(slot, ItemStack.EMPTY);
        }
        if (getBlockState().getValue(BarrelBlock.SEALED)) {
            Block.popResource(level, worldPosition, new ItemStack(BarrelFeature.BARREL_LID.get()));
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
        elapsedTicks = tag.getInt("ElapsedTicks");
        totalTicks = tag.getInt("TotalTicks");
        rainFillTicks = tag.getInt("RainFillTicks");
        rainConversionTicks = tag.getInt("RainConversionTicks");
        resolveRecipe();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("ElapsedTicks", elapsedTicks);
        tag.putInt("TotalTicks", totalTicks);
        tag.putInt("RainFillTicks", rainFillTicks);
        tag.putInt("RainConversionTicks", rainConversionTicks);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        if (!tank.isEmpty()) {
            builder.set(PrimitiveMaterialsFeature.STORED_FLUID.get(), tank.getFluid().copy());
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        input.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
        tank.setFluid(input.getOrDefault(PrimitiveMaterialsFeature.STORED_FLUID.get(), FluidStack.EMPTY).copy());
        elapsedTicks = 0;
        totalTicks = 0;
        resolveRecipe();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("Items");
        tag.remove("Tank");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private final class BarrelItemHandler implements IItemHandler {

        private final Direction side;

        private BarrelItemHandler(@Nullable Direction side) {
            this.side = side == null ? Direction.UP : side;
        }

        @Override
        public int getSlots() {
            return 4;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return item(slot).copy();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (getBlockState().getValue(BarrelBlock.SEALED) || side != Direction.UP || !canInsert(slot, stack)) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            if (!simulate) {
                ItemStack one = stack.copyWithCount(1);
                insert(slot, one, true);
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (getBlockState().getValue(BarrelBlock.SEALED) || side != Direction.UP || amount <= 0 || item(slot).isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack result = item(slot).copyWithCount(1);
            if (!simulate) {
                extract(slot);
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return side == Direction.UP && canInsert(slot, stack);
        }
    }
}
