package com.protyvkultury.revivalages.feature.technology.dryingrack.blockentity;

import com.protyvkultury.revivalages.feature.technology.dryingrack.DryingRackFeature;
import com.protyvkultury.revivalages.feature.technology.dryingrack.block.CrudeDryingRackBlock;
import com.protyvkultury.revivalages.feature.technology.dryingrack.config.DryingRackClientConfig;
import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.DryingEnvironmentBase;
import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.DryingEnvironmentCalculator;
import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.DryingEnvironmentModifier;
import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.DryingEnvironmentModifierType;
import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.DryingEnvironmentSnapshot;
import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.DryingRackSeasonService;
import com.protyvkultury.revivalages.feature.technology.dryingrack.recipe.DryingRecipe;
import com.protyvkultury.revivalages.feature.technology.dryingrack.recipe.DryingRecipeResolver;
import com.protyvkultury.revivalages.feature.technology.dryingrack.view.DryingRackView;
import com.protyvkultury.revivalages.feature.technology.dryingrack.view.DryingSlotView;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class DryingRackBlockEntity extends BlockEntity {

    private static final int ENVIRONMENT_UPDATE_INTERVAL = 20;
    private static final int PARTICLE_INTERVAL = 40;
    private static final String CLIENT_ENVIRONMENT_TAG = "ClientEnvironment";

    private final NonNullList<ItemStack> items;
    private final int[] totalTimes;
    private final double[] remainingTimes;
    private final boolean[] completed;
    private final ResourceLocation[] recipeIds;
    private final DryingRecipe[] activeRecipes;
    private final boolean normalRack;

    private double speed;
    private DryingEnvironmentSnapshot environment = DryingEnvironmentSnapshot.EMPTY;

    private DryingRackBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state,
            int slotCount,
            boolean normalRack
    ) {
        super(type, pos, state);
        this.items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        this.totalTimes = new int[slotCount];
        this.remainingTimes = new double[slotCount];
        this.completed = new boolean[slotCount];
        this.recipeIds = new ResourceLocation[slotCount];
        this.activeRecipes = new DryingRecipe[slotCount];
        this.normalRack = normalRack;
    }

    public static DryingRackBlockEntity createCrude(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(
                DryingRackFeature.CRUDE_DRYING_RACK_BLOCK_ENTITY.get(),
                pos,
                state,
                1,
                false
        );
    }

    public static DryingRackBlockEntity createNormal(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(
                DryingRackFeature.DRYING_RACK_BLOCK_ENTITY.get(),
                pos,
                state,
                4,
                true
        );
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity rack) {
        if (level.getGameTime() % ENVIRONMENT_UPDATE_INTERVAL == 0L) {
            rack.environment = DryingEnvironmentCalculator.snapshot(
                    level,
                    pos,
                    rack.normalRack,
                    DryingRackSeasonService.provider()
            );
            rack.speed = rack.environment.speed();
            rack.refreshRecipes(level);
            rack.sync();
        }
        rack.advance(level);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity rack) {
        if (!DryingRackClientConfig.SHOW_PROGRESS_PARTICLES.get()
                || rack.speed <= 0.0D
                || !rack.hasProcessingInput()
                || level.getGameTime() % PARTICLE_INTERVAL != 0L) {
            return;
        }
        if (rack.normalRack) {
            rack.spawnProgressParticle(level, pos.getX() + 0.5D, pos.getY() + 0.75D, pos.getZ() + 0.5D,
                    0.5D, 0.15D, 0.5D);
        } else {
            rack.spawnCrudeProgressParticle(level, pos, state.getValue(CrudeDryingRackBlock.FACING));
        }
    }

    public boolean canInsert(int slot) {
        return validSlot(slot) && items.get(slot).isEmpty();
    }

    public void insert(int slot, ItemStack source, boolean infiniteMaterials) {
        if (!canInsert(slot) || source.isEmpty()) {
            return;
        }
        ItemStack inserted = source.copyWithCount(1);
        if (!infiniteMaterials) {
            source.shrink(1);
        }
        items.set(slot, inserted);
        completed[slot] = false;
        totalTimes[slot] = 0;
        remainingTimes[slot] = 0.0D;
        recipeIds[slot] = null;
        activeRecipes[slot] = null;
        if (level != null) {
            resolveRecipe(level, slot);
        }
        sync();
    }

    public ItemStack extract(int slot) {
        if (!validSlot(slot)) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        completed[slot] = false;
        totalTimes[slot] = 0;
        remainingTimes[slot] = 0.0D;
        recipeIds[slot] = null;
        activeRecipes[slot] = null;
        sync();
        return extracted;
    }

    public ItemStack getItem(int slot) {
        return validSlot(slot) ? items.get(slot) : ItemStack.EMPTY;
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public int getSlotCount() {
        return items.size();
    }

    public double getSpeed() {
        return speed;
    }

    public double getProgress(int slot) {
        if (!validSlot(slot) || totalTimes[slot] <= 0) {
            return 0.0D;
        }
        return 1.0D - remainingTimes[slot] / totalTimes[slot];
    }

    public DryingRackView view() {
        List<DryingSlotView> slots = new ArrayList<>(items.size());
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            DryingRecipe recipe = activeRecipes[slot];
            if (recipe == null && level != null && !stack.isEmpty() && !completed[slot]) {
                recipe = DryingRecipeResolver.find(level, stack, normalRack)
                        .map(RecipeHolder::value)
                        .orElse(null);
            }
            boolean processing = recipe != null && remainingTimes[slot] > 0.0D && !completed[slot];
            ItemStack output = recipe == null || level == null
                    ? ItemStack.EMPTY
                    : recipe.getResultItem(level.registryAccess());
            slots.add(new DryingSlotView(
                    stack,
                    output,
                    getProgress(slot),
                    processing,
                    completed[slot] && totalTimes[slot] > 0
            ));
        }
        return new DryingRackView(slots, environment);
    }

    public IItemHandler itemHandler(@Nullable Direction side) {
        return new AutomationHandler(side);
    }

    private void advance(Level level) {
        boolean changed = false;
        for (int slot = 0; slot < items.size(); slot++) {
            DryingRecipe recipe = activeRecipes[slot];
            if (recipe == null || completed[slot]) {
                continue;
            }
            remainingTimes[slot] = Math.max(0.0D, Math.min(totalTimes[slot], remainingTimes[slot] - speed));
            if (remainingTimes[slot] <= 0.0D) {
                items.set(slot, recipe.assemble(new SingleRecipeInput(items.get(slot)), level.registryAccess()));
                completed[slot] = true;
                recipeIds[slot] = null;
                activeRecipes[slot] = null;
                changed = true;
            }
        }
        if (changed) {
            sync();
        } else if (hasActiveRecipe()) {
            setChanged();
        }
    }

    private void refreshRecipes(Level level) {
        for (int slot = 0; slot < items.size(); slot++) {
            if (!items.get(slot).isEmpty() && !completed[slot]) {
                resolveRecipe(level, slot);
            }
        }
    }

    private void resolveRecipe(Level level, int slot) {
        Optional<RecipeHolder<DryingRecipe>> match = DryingRecipeResolver.find(level, items.get(slot), normalRack);
        if (match.isEmpty()) {
            completed[slot] = false;
            activeRecipes[slot] = null;
            recipeIds[slot] = null;
            totalTimes[slot] = 0;
            remainingTimes[slot] = 0.0D;
            return;
        }

        RecipeHolder<DryingRecipe> holder = match.get();
        DryingRecipe recipe = holder.value();
        if (!holder.id().equals(recipeIds[slot])) {
            recipeIds[slot] = holder.id();
            totalTimes[slot] = recipe.dryingTime();
            remainingTimes[slot] = recipe.dryingTime();
        } else if (totalTimes[slot] != recipe.dryingTime()) {
            double ratio = totalTimes[slot] <= 0 ? 1.0D : remainingTimes[slot] / totalTimes[slot];
            totalTimes[slot] = recipe.dryingTime();
            remainingTimes[slot] = ratio * recipe.dryingTime();
        }
        activeRecipes[slot] = recipe;
    }

    private boolean hasActiveRecipe() {
        for (DryingRecipe recipe : activeRecipes) {
            if (recipe != null) {
                return true;
            }
        }
        return false;
    }

    private boolean hasProcessingInput() {
        for (int slot = 0; slot < items.size(); slot++) {
            if (!items.get(slot).isEmpty()
                    && !completed[slot]
                    && totalTimes[slot] > 0
                    && remainingTimes[slot] > 0.0D) {
                return true;
            }
        }
        return false;
    }

    private void spawnCrudeProgressParticle(Level level, BlockPos pos, Direction facing) {
        switch (facing) {
            case NORTH -> spawnProgressParticle(
                    level, pos.getX() + 0.5D, pos.getY() + 0.4D, pos.getZ() + 0.25D,
                    0.125D, 0.125D, 0.0625D
            );
            case SOUTH -> spawnProgressParticle(
                    level, pos.getX() + 0.5D, pos.getY() + 0.4D, pos.getZ() + 0.75D,
                    0.125D, 0.125D, 0.0625D
            );
            case EAST -> spawnProgressParticle(
                    level, pos.getX() + 0.75D, pos.getY() + 0.4D, pos.getZ() + 0.5D,
                    0.0625D, 0.125D, 0.125D
            );
            case WEST -> spawnProgressParticle(
                    level, pos.getX() + 0.25D, pos.getY() + 0.4D, pos.getZ() + 0.5D,
                    0.0625D, 0.125D, 0.125D
            );
            default -> {
            }
        }
    }

    private void spawnProgressParticle(
            Level level,
            double x,
            double y,
            double z,
            double rangeX,
            double rangeY,
            double rangeZ
    ) {
        double velocityX = level.getRandom().nextGaussian() * 0.02D;
        double velocityY = level.getRandom().nextGaussian() * 0.02D;
        double velocityZ = level.getRandom().nextGaussian() * 0.02D;
        level.addParticle(
                ParticleTypes.HAPPY_VILLAGER,
                x + (level.getRandom().nextDouble() * 2.0D - 1.0D) * rangeX,
                y + (level.getRandom().nextDouble() * 2.0D - 1.0D) * rangeY,
                z + (level.getRandom().nextDouble() * 2.0D - 1.0D) * rangeZ,
                velocityX,
                velocityY,
                velocityZ
        );
    }

    private boolean validSlot(int slot) {
        return slot >= 0 && slot < items.size();
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
        readClientEnvironment(tag);
        for (int slot = 0; slot < items.size(); slot++) {
            totalTimes[slot] = tag.getInt("Total" + slot);
            remainingTimes[slot] = tag.getDouble("Remaining" + slot);
            completed[slot] = tag.getBoolean("Completed" + slot);
            String recipeId = tag.getString("Recipe" + slot);
            recipeIds[slot] = recipeId.isEmpty() ? null : ResourceLocation.tryParse(recipeId);
            activeRecipes[slot] = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
        for (int slot = 0; slot < items.size(); slot++) {
            tag.putInt("Total" + slot, totalTimes[slot]);
            tag.putDouble("Remaining" + slot, remainingTimes[slot]);
            tag.putBoolean("Completed" + slot, completed[slot]);
            if (recipeIds[slot] != null) {
                tag.putString("Recipe" + slot, recipeIds[slot].toString());
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = saveWithoutMetadata(registries);
        writeClientEnvironment(tag);
        return tag;
    }

    private void writeClientEnvironment(CompoundTag tag) {
        CompoundTag environmentTag = new CompoundTag();
        environmentTag.putString("Base", environment.base().name());
        environmentTag.putDouble("BaseSpeed", environment.baseSpeed());
        environmentTag.putDouble("RackMultiplier", environment.rackMultiplier());
        ListTag modifiersTag = new ListTag();
        for (DryingEnvironmentModifier modifier : environment.modifiers()) {
            CompoundTag modifierTag = new CompoundTag();
            modifierTag.putString("Type", modifier.type().name());
            modifierTag.putDouble("Amount", modifier.amount());
            modifiersTag.add(modifierTag);
        }
        environmentTag.put("Modifiers", modifiersTag);
        tag.put(CLIENT_ENVIRONMENT_TAG, environmentTag);
    }

    private void readClientEnvironment(CompoundTag tag) {
        if (!tag.contains(CLIENT_ENVIRONMENT_TAG, Tag.TAG_COMPOUND)) {
            environment = DryingEnvironmentSnapshot.EMPTY;
            speed = 0.0D;
            return;
        }
        CompoundTag environmentTag = tag.getCompound(CLIENT_ENVIRONMENT_TAG);
        DryingEnvironmentBase base;
        try {
            base = DryingEnvironmentBase.valueOf(environmentTag.getString("Base"));
        } catch (IllegalArgumentException exception) {
            base = DryingEnvironmentBase.DEFAULT;
        }
        List<DryingEnvironmentModifier> modifiers = new ArrayList<>();
        ListTag modifiersTag = environmentTag.getList("Modifiers", Tag.TAG_COMPOUND);
        for (Tag entry : modifiersTag) {
            if (!(entry instanceof CompoundTag modifierTag)) {
                continue;
            }
            try {
                DryingEnvironmentModifierType type = DryingEnvironmentModifierType.valueOf(
                        modifierTag.getString("Type")
                );
                modifiers.add(new DryingEnvironmentModifier(type, modifierTag.getDouble("Amount")));
            } catch (IllegalArgumentException ignored) {
                // Unknown future modifier types are ignored by older clients.
            }
        }
        environment = new DryingEnvironmentSnapshot(
                base,
                environmentTag.getDouble("BaseSpeed"),
                modifiers,
                environmentTag.getDouble("RackMultiplier")
        );
        speed = environment.speed();
    }

    private final class AutomationHandler implements IItemHandler {

        private final Direction side;

        private AutomationHandler(@Nullable Direction side) {
            this.side = side == null ? Direction.UP : side;
        }

        @Override
        public int getSlots() {
            return items.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return validSlot(slot) ? items.get(slot).copy() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (side == Direction.DOWN
                    || !validSlot(slot)
                    || !canInsert(slot)
                    || stack.isEmpty()
                    || level == null
                    || DryingRecipeResolver.find(level, stack, normalRack).isEmpty()) {
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
            if (side != Direction.DOWN
                    || !validSlot(slot)
                    || !completed[slot]
                    || amount <= 0
                    || items.get(slot).isEmpty()) {
                return ItemStack.EMPTY;
            }
            int extractedCount = Math.min(amount, items.get(slot).getCount());
            ItemStack extracted = items.get(slot).copyWithCount(extractedCount);
            if (!simulate) {
                items.get(slot).shrink(extractedCount);
                if (items.get(slot).isEmpty()) {
                    extract(slot);
                } else {
                    sync();
                }
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return side != Direction.DOWN
                    && validSlot(slot)
                    && level != null
                    && DryingRecipeResolver.find(level, stack, normalRack).isPresent();
        }
    }
}
