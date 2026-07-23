package com.protyvkultury.revivalages.feature.technology.animalpower.blockentity;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalChoppingProfile;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalMachineKind;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerConfig;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalWorkArea;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalWorkerController;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingMachine;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingChance;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingRecipe;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.PressingRecipe;
import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingBlockFeature;
import com.protyvkultury.revivalages.feature.technology.choppingblock.recipe.ChoppingRecipe;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
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

public final class AnimalMachineBlockEntity extends BlockEntity {

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private final AnimalWorkerController worker = new AnimalWorkerController();
    private final FluidTank tank = new FluidTank(AnimalPowerConfig.PRESS_TANK_CAPACITY.get()) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };
    private AnimalMachineKind kind;
    private int workPoints;
    private int areaCheckTicks;
    private boolean workAreaValid;
    private ResourceLocation woodVariant = ResourceLocation.withDefaultNamespace("oak_log");

    public AnimalMachineBlockEntity(BlockPos pos, BlockState state) {
        super(AnimalPowerFeature.ANIMAL_MACHINE_BLOCK_ENTITY.get(), pos, state);
        kind = AnimalPowerFeature.kind(state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AnimalMachineBlockEntity machine) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        machine.kind = AnimalPowerFeature.kind(state);
        if (machine.kind == AnimalMachineKind.PRESS) {
            machine.tank.setCapacity(AnimalPowerConfig.PRESS_TANK_CAPACITY.get());
        }
        machine.areaCheckTicks--;
        if (machine.areaCheckTicks <= 0) {
            boolean previous = machine.workAreaValid;
            machine.workAreaValid = AnimalWorkArea.isValid(level, pos, machine.kind.tall());
            machine.areaCheckTicks = AnimalPowerConfig.WORK_AREA_CHECK_INTERVAL.get();
            if (previous != machine.workAreaValid) {
                machine.sync();
            }
        }
        boolean ready = machine.workAreaValid && machine.canProcess();
        if (machine.worker.tick(server, pos, ready)) {
            machine.workPoints++;
            machine.tryComplete();
            machine.sync();
        }
    }

    public AnimalMachineKind kind() {
        return kind;
    }

    public ItemStack item(int slot) {
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }

    public FluidTank fluidTank() {
        return tank;
    }

    public IFluidHandler fluidOutputHandler() {
        return new OutputFluidHandler();
    }

    public Optional<UUID> workerId() {
        return worker.workerId();
    }

    public int waypointIndex() {
        return worker.waypointIndex();
    }

    public boolean workAreaValid() {
        return workAreaValid;
    }

    public ResourceLocation woodVariant() {
        return woodVariant;
    }

    public void setWoodVariant(ResourceLocation woodVariant) {
        this.woodVariant = woodVariant;
        sync();
    }

    public boolean attachWorker(Player player) {
        if (!(level instanceof ServerLevel server)) {
            return false;
        }
        boolean attached = worker.attach(server, worldPosition, player);
        if (attached) {
            level.playSound(null, worldPosition, SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            sync();
        }
        return attached;
    }

    public boolean detachWorker() {
        if (!(level instanceof ServerLevel server) || worker.workerId().isEmpty()) {
            return false;
        }
        worker.detach(server, worldPosition, true);
        level.playSound(null, worldPosition, SoundEvents.LEASH_KNOT_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        sync();
        return true;
    }

    public boolean canInsert(ItemStack stack) {
        return items.getFirst().isEmpty() && findAnyRecipe(stack);
    }

    public void insert(ItemStack source, boolean infinite) {
        if (!canInsert(source)) {
            return;
        }
        int count = requiredInputCount(source);
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

    public double progress() {
        int required = requiredWorkPoints();
        return required <= 0 ? 0.0D : Math.min(1.0D, workPoints / (double) required);
    }

    public ItemStack recipeOutput() {
        return switch (kind) {
            case GRINDSTONE -> grindingRecipe().map(GrindingRecipe::result).orElse(ItemStack.EMPTY);
            case CHOPPING_BLOCK -> choppingRecipe().map(recipe -> {
                ItemStack output = recipe.result();
                output.setCount(recipe.quantityForTier(
                        AnimalPowerConfig.CHOPPING_TIER.get(),
                        AnimalChoppingProfile.quantityForTier(AnimalPowerConfig.CHOPPING_TIER.get())));
                return output;
            }).orElse(ItemStack.EMPTY);
            case PRESS -> pressingRecipe().map(PressingRecipe::itemResult).orElse(ItemStack.EMPTY);
        };
    }

    public FluidStack recipeFluidOutput() {
        return kind == AnimalMachineKind.PRESS
                ? pressingRecipe().map(PressingRecipe::fluidResult).orElse(FluidStack.EMPTY)
                : FluidStack.EMPTY;
    }

    public String blockingState() {
        if (!workAreaValid) {
            return "invalid_area";
        }
        if (worker.workerId().isEmpty()) {
            return "no_worker";
        }
        if (items.getFirst().isEmpty()) {
            return "no_input";
        }
        if (!findAnyRecipe(items.getFirst())) {
            return "no_recipe";
        }
        if (!outputsFit()) {
            return "output_blocked";
        }
        return "working";
    }

    public IItemHandler itemHandler(@Nullable Direction side) {
        return new MachineItemHandler(side);
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int slot = 0; slot < items.size(); slot++) {
            Block.popResource(level, worldPosition, items.get(slot));
            items.set(slot, ItemStack.EMPTY);
        }
        if (level instanceof ServerLevel server) {
            worker.detach(server, worldPosition, true);
        }
    }

    private boolean canProcess() {
        return findAnyRecipe(items.getFirst()) && outputsFit();
    }

    private boolean outputsFit() {
        ItemStack result = recipeOutput();
        if (!canMerge(items.get(1), result)) {
            return false;
        }
        if (kind == AnimalMachineKind.GRINDSTONE) {
            return grindingRecipe().map(recipe -> canMerge(items.get(2), recipe.secondaryResult())).orElse(false);
        }
        FluidStack fluid = recipeFluidOutput();
        if (fluid.isEmpty()) {
            return true;
        }
        return (tank.isEmpty() || FluidStack.isSameFluidSameComponents(tank.getFluid(), fluid))
                && tank.getFluidAmount() + fluid.getAmount() <= tank.getCapacity();
    }

    private int requiredWorkPoints() {
        return switch (kind) {
            case GRINDSTONE -> grindingRecipe().map(GrindingRecipe::workPoints).orElse(0);
            case CHOPPING_BLOCK -> choppingRecipe()
                    .map(recipe -> recipe.chopsForTier(
                            AnimalPowerConfig.CHOPPING_TIER.get(),
                            AnimalChoppingProfile.cyclesForTier(AnimalPowerConfig.CHOPPING_TIER.get()))
                            * AnimalPowerConfig.CHOPPING_POINTS_PER_CYCLE.get())
                    .orElse(0);
            case PRESS -> pressingRecipe().isPresent() ? AnimalPowerConfig.PRESS_POINTS.get() : 0;
        };
    }

    private void tryComplete() {
        int required = requiredWorkPoints();
        if (required <= 0 || workPoints < required || !outputsFit() || level == null) {
            return;
        }
        ItemStack itemOutput = recipeOutput();
        ItemStack secondaryOutput = ItemStack.EMPTY;
        double secondaryChance = 0.0D;
        if (kind == AnimalMachineKind.GRINDSTONE) {
            Optional<GrindingRecipe> grinding = grindingRecipe();
            if (grinding.isPresent()) {
                secondaryOutput = grinding.get().secondaryResult();
                secondaryChance = grinding.get().secondaryChance();
            }
        }
        FluidStack fluidOutput = recipeFluidOutput();
        int inputCount = requiredInputCount(items.getFirst());
        items.getFirst().shrink(inputCount);
        if (items.getFirst().isEmpty()) {
            items.set(0, ItemStack.EMPTY);
        }
        merge(1, itemOutput);
        if (!secondaryOutput.isEmpty()
                && GrindingChance.shouldProduce(secondaryChance, level.random.nextDouble())) {
            merge(2, secondaryOutput);
        }
        if (!fluidOutput.isEmpty()) {
            tank.fill(fluidOutput, IFluidHandler.FluidAction.EXECUTE);
        }
        workPoints = 0;
        level.playSound(
                null,
                worldPosition,
                kind == AnimalMachineKind.CHOPPING_BLOCK ? SoundEvents.WOOD_BREAK : SoundEvents.GRINDSTONE_USE,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
    }

    private int requiredInputCount(ItemStack stack) {
        return switch (kind) {
            case GRINDSTONE -> grindingRecipe(stack).map(GrindingRecipe::inputCount).orElse(1);
            case CHOPPING_BLOCK -> 1;
            case PRESS -> pressingRecipe(stack).map(PressingRecipe::inputCount).orElse(1);
        };
    }

    private boolean findAnyRecipe(ItemStack stack) {
        return switch (kind) {
            case GRINDSTONE -> grindingRecipe(stack).isPresent();
            case CHOPPING_BLOCK -> choppingRecipe(stack).isPresent();
            case PRESS -> pressingRecipe(stack).isPresent();
        };
    }

    private Optional<GrindingRecipe> grindingRecipe() {
        return grindingRecipe(items.getFirst());
    }

    private Optional<GrindingRecipe> grindingRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager()
                .getRecipeFor(AnimalPowerFeature.GRINDING_TYPE.get(), new SingleRecipeInput(stack), level)
                .map(RecipeHolder::value)
                .filter(recipe -> recipe.supports(GrindingMachine.ANIMAL));
    }

    private Optional<ChoppingRecipe> choppingRecipe() {
        return choppingRecipe(items.getFirst());
    }

    private Optional<ChoppingRecipe> choppingRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager()
                .getRecipeFor(ChoppingBlockFeature.RECIPE_TYPE.get(), new SingleRecipeInput(stack), level)
                .map(RecipeHolder::value);
    }

    private Optional<PressingRecipe> pressingRecipe() {
        return pressingRecipe(items.getFirst());
    }

    private Optional<PressingRecipe> pressingRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager()
                .getRecipeFor(AnimalPowerFeature.PRESSING_TYPE.get(), new SingleRecipeInput(stack), level)
                .map(RecipeHolder::value);
    }

    private static boolean canMerge(ItemStack stored, ItemStack addition) {
        return addition.isEmpty() || stored.isEmpty()
                || ItemStack.isSameItemSameComponents(stored, addition)
                && stored.getCount() + addition.getCount() <= stored.getMaxStackSize();
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
        tank.readFromNBT(registries, tag.getCompound("Tank"));
        worker.load(tag);
        workPoints = Math.max(0, tag.getInt("WorkPoints"));
        areaCheckTicks = Math.max(0, tag.getInt("AreaCheckTicks"));
        workAreaValid = tag.getBoolean("WorkAreaValid");
        ResourceLocation parsed = ResourceLocation.tryParse(tag.getString("WoodVariant"));
        woodVariant = parsed == null ? ResourceLocation.withDefaultNamespace("oak_log") : parsed;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        worker.save(tag);
        tag.putInt("WorkPoints", workPoints);
        tag.putInt("AreaCheckTicks", areaCheckTicks);
        tag.putBoolean("WorkAreaValid", workAreaValid);
        tag.putString("WoodVariant", woodVariant.toString());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (kind == AnimalMachineKind.CHOPPING_BLOCK) {
            builder.set(AnimalPowerFeature.WOOD_VARIANT.get(), woodVariant);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        woodVariant = input.getOrDefault(
                AnimalPowerFeature.WOOD_VARIANT.get(),
                ResourceLocation.withDefaultNamespace("oak_log")
        );
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("WoodVariant");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private final class MachineItemHandler implements IItemHandler {

        private final Direction side;

        private MachineItemHandler(@Nullable Direction side) {
            this.side = side == null ? Direction.UP : side;
        }

        @Override
        public int getSlots() {
            return items.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return item(slot).copy();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || side == Direction.DOWN || !canInsert(stack)) {
                return stack;
            }
            int count = requiredInputCount(stack);
            if (stack.getCount() < count) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(count);
            if (!simulate) {
                insert(stack.copyWithCount(count), true);
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot <= 0 || amount <= 0 || item(slot).isEmpty()) {
                return ItemStack.EMPTY;
            }
            int extracted = Math.min(amount, item(slot).getCount());
            ItemStack result = item(slot).copyWithCount(extracted);
            if (!simulate) {
                items.get(slot).shrink(extracted);
                if (items.get(slot).isEmpty()) {
                    items.set(slot, ItemStack.EMPTY);
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
            return slot == 0 && side != Direction.DOWN && canInsert(stack);
        }
    }

    private final class OutputFluidHandler implements IFluidHandler {

        @Override
        public int getTanks() {
            return tank.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tankIndex) {
            return tank.getFluidInTank(tankIndex);
        }

        @Override
        public int getTankCapacity(int tankIndex) {
            return tank.getTankCapacity(tankIndex);
        }

        @Override
        public boolean isFluidValid(int tankIndex, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return tank.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return tank.drain(maxDrain, action);
        }
    }
}
