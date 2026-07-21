package com.protyvkultury.revivalages.feature.technology.tanningrack.blockentity;

import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.tanningrack.TanningRackFeature;
import com.protyvkultury.revivalages.feature.technology.tanningrack.recipe.TanningRackRecipe;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class TanningRackBlockEntity extends BlockEntity {

    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private int elapsedTicks;
    private int totalTicks;
    private int rainTicks;
    private TanningRackRecipe activeRecipe;

    public TanningRackBlockEntity(BlockPos pos, BlockState state) {
        super(TanningRackFeature.BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TanningRackBlockEntity rack) {
        rack.resolveRecipe();
        if (rack.activeRecipe == null || rack.input.isEmpty() || !rack.output.isEmpty()) {
            return;
        }
        if (!level.canSeeSky(pos)) {
            if (rack.elapsedTicks != 0 || rack.rainTicks != 0) {
                rack.elapsedTicks = 0;
                rack.rainTicks = 0;
                rack.sync();
            }
            return;
        }
        int rainLimit = PrimitiveTechnologyConfig.TANNING_RACK_RAIN_RUIN_TICKS.get();
        if (rainLimit >= 0 && level.isRainingAt(pos.above())) {
            if (rack.rainTicks < rainLimit) {
                rack.rainTicks++;
            }
            if (rack.rainTicks >= rainLimit && !rack.activeRecipe.rainFailure().isEmpty()) {
                rack.output = rack.activeRecipe.rainFailure();
                rack.input = ItemStack.EMPTY;
                rack.elapsedTicks = 0;
                rack.totalTicks = 0;
                rack.rainTicks = 0;
                rack.activeRecipe = null;
                rack.sync();
            } else {
                rack.setChanged();
            }
            return;
        }
        rack.rainTicks = 0;
        long time = level.getDayTime() % 24000L;
        if (time > 12000L) {
            return;
        }
        rack.totalTicks = Math.max(1, (int) Math.round(rack.activeRecipe.processingTime()
                * PrimitiveTechnologyConfig.TANNING_RACK_DURATION_MULTIPLIER.get()));
        rack.elapsedTicks++;
        if (rack.elapsedTicks >= rack.totalTicks) {
            rack.output = rack.activeRecipe.result();
            rack.input = ItemStack.EMPTY;
            rack.elapsedTicks = 0;
            rack.totalTicks = 0;
            rack.activeRecipe = null;
            rack.sync();
        } else {
            rack.setChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TanningRackBlockEntity rack) {
        long time = level.getDayTime() % 24000L;
        if (rack.activeRecipe != null
                && !rack.input.isEmpty()
                && level.canSeeSky(pos)
                && (PrimitiveTechnologyConfig.TANNING_RACK_RAIN_RUIN_TICKS.get() < 0
                || !level.isRainingAt(pos.above()))
                && time <= 12000L
                && PrimitiveTechnologyConfig.PROGRESS_PARTICLES.get()
                && level.getGameTime() % 40L == 0L) {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5D, pos.getY() + 0.85D, pos.getZ() + 0.5D,
                    0.0D, 0.02D, 0.0D);
        }
    }

    public ItemStack input() {
        return input;
    }

    public ItemStack output() {
        return output;
    }

    public ItemStack recipeOutput() {
        return activeRecipe == null ? ItemStack.EMPTY : activeRecipe.result();
    }

    public double progress() {
        return totalTicks <= 0 ? 0.0D : Math.min(1.0D, elapsedTicks / (double) totalTicks);
    }

    public int rainTicks() {
        return rainTicks;
    }

    public boolean canInsert(ItemStack stack) {
        return input.isEmpty() && output.isEmpty() && findRecipe(stack).isPresent();
    }

    public void insert(ItemStack source, boolean infinite) {
        if (!canInsert(source)) {
            return;
        }
        input = source.copyWithCount(1);
        if (!infinite) {
            source.shrink(1);
        }
        elapsedTicks = 0;
        totalTicks = 0;
        rainTicks = 0;
        resolveRecipe();
        sync();
    }

    public ItemStack extractInput() {
        ItemStack result = input;
        input = ItemStack.EMPTY;
        elapsedTicks = 0;
        totalTicks = 0;
        rainTicks = 0;
        activeRecipe = null;
        sync();
        return result;
    }

    public ItemStack extractOutput() {
        ItemStack result = output;
        output = ItemStack.EMPTY;
        resolveRecipe();
        sync();
        return result;
    }

    public void dropContents() {
        if (level != null && !level.isClientSide) {
            Block.popResource(level, worldPosition, input);
            Block.popResource(level, worldPosition, output);
        }
    }

    public IItemHandler itemHandler(@Nullable Direction side) {
        return new Handler(side);
    }

    private Optional<RecipeHolder<TanningRackRecipe>> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(TanningRackFeature.RECIPE_TYPE.get(), new SingleRecipeInput(stack), level);
    }

    private void resolveRecipe() {
        activeRecipe = findRecipe(input).map(RecipeHolder::value).orElse(null);
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
        elapsedTicks = tag.getInt("ElapsedTicks");
        totalTicks = tag.getInt("TotalTicks");
        rainTicks = tag.getInt("RainTicks");
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
        }
        tag.putInt("ElapsedTicks", elapsedTicks);
        tag.putInt("TotalTicks", totalTicks);
        tag.putInt("RainTicks", rainTicks);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private final class Handler implements IItemHandler {

        private final Direction side;

        private Handler(@Nullable Direction side) {
            this.side = side == null ? Direction.UP : side;
        }

        @Override
        public int getSlots() {
            return 2;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? input.copy() : slot == 1 ? output.copy() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || side == Direction.DOWN || !canInsert(stack)) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            if (!simulate) {
                ItemStack one = stack.copyWithCount(1);
                insert(one, true);
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 1 || side != Direction.DOWN || amount <= 0 || output.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack result = output.copyWithCount(1);
            if (!simulate) {
                extractOutput();
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && side != Direction.DOWN && findRecipe(stack).isPresent();
        }
    }
}
