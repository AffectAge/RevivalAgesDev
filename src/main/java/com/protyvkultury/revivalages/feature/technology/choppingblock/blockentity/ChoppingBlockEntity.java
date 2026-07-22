package com.protyvkultury.revivalages.feature.technology.choppingblock.blockentity;

import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingBlockFeature;
import com.protyvkultury.revivalages.feature.technology.choppingblock.block.ChoppingBlock;
import com.protyvkultury.revivalages.feature.technology.choppingblock.recipe.ChoppingRecipe;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class ChoppingBlockEntity extends BlockEntity {

    private ItemStack input = ItemStack.EMPTY;
    private int chops;
    private int requiredChops;
    private int sawdust;
    private int durabilityUntilDamage = PrimitiveTechnologyConfig.CHOPPING_CHOPS_PER_DAMAGE.get();
    private ChoppingRecipe activeRecipe;

    public ChoppingBlockEntity(BlockPos pos, BlockState state) {
        super(ChoppingBlockFeature.BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStack input() {
        return input;
    }

    public int sawdust() {
        return sawdust;
    }

    public void removeSawdust() {
        sawdust = Math.max(0, sawdust - 1);
        sync();
    }

    public double progress() {
        return requiredChops <= 0 ? 0.0D : Math.min(1.0D, chops / (double) requiredChops);
    }

    public ItemStack recipeOutput() {
        return activeRecipe == null ? ItemStack.EMPTY : activeRecipe.result();
    }

    public boolean canInsert(ItemStack stack) {
        return input.isEmpty() && findRecipe(stack).isPresent();
    }

    public void insert(ItemStack source, boolean infinite) {
        if (!canInsert(source)) {
            return;
        }
        input = source.copyWithCount(1);
        if (!infinite) {
            source.shrink(1);
        }
        chops = 0;
        resolveRecipe();
        sync();
    }

    public ItemStack extract() {
        ItemStack result = input;
        input = ItemStack.EMPTY;
        chops = 0;
        requiredChops = 0;
        activeRecipe = null;
        sync();
        return result;
    }

    public void chop(Player player, ItemStack axe, InteractionHand hand) {
        if (level == null || level.isClientSide || activeRecipe == null) {
            resolveRecipe();
        }
        if (level == null || activeRecipe == null || input.isEmpty()) {
            return;
        }
        int tier = toolTier(axe);
        requiredChops = activeRecipe.chopsForTier(tier, defaultChops(tier));
        chops++;
        player.causeFoodExhaustion(PrimitiveTechnologyConfig.CHOPPING_EXHAUSTION_PER_CHOP.get().floatValue());
        axe.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        durabilityUntilDamage--;
        if (level.random.nextDouble() < PrimitiveTechnologyConfig.CHOPPING_WOOD_CHIPS_CHANCE.get() * 2.0D) {
            sawdust = Math.min(5, sawdust + 1);
        }
        if (level.random.nextDouble() < PrimitiveTechnologyConfig.CHOPPING_WOOD_CHIPS_CHANCE.get() * 0.5D) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(level.random);
            BlockPos chipPos = worldPosition.relative(direction).above();
            Block.popResource(level, chipPos, new ItemStack(
                    com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature.WOOD_CHIPS.get()));
        }
        if (level instanceof ServerLevel server) {
            BlockState particleState = Block.byItem(input.getItem()).defaultBlockState();
            if (particleState.isAir()) {
                particleState = getBlockState();
            }
            server.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, particleState),
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.8D,
                    worldPosition.getZ() + 0.5D,
                    8,
                    0.25D,
                    0.1D,
                    0.25D,
                    0.05D
            );
        }
        level.playSound(null, worldPosition, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.75F,
                (float) (1.0D + level.random.nextGaussian() * 0.4D));

        if (PrimitiveTechnologyConfig.CHOPPING_USES_DURABILITY.get() && durabilityUntilDamage <= 0) {
            durabilityUntilDamage = PrimitiveTechnologyConfig.CHOPPING_CHOPS_PER_DAMAGE.get();
            int damage = getBlockState().getValue(ChoppingBlock.DAMAGE);
            if (damage >= 5) {
                level.destroyBlock(worldPosition, false);
                return;
            }
            level.setBlock(worldPosition, getBlockState().setValue(ChoppingBlock.DAMAGE, damage + 1), Block.UPDATE_ALL);
        }

        if (chops >= requiredChops) {
            player.causeFoodExhaustion(PrimitiveTechnologyConfig.CHOPPING_EXHAUSTION_PER_CRAFT.get().floatValue());
            ItemStack output = activeRecipe.result();
            output.setCount(activeRecipe.quantityForTier(tier, defaultQuantity(tier)));
            input = ItemStack.EMPTY;
            chops = 0;
            requiredChops = 0;
            activeRecipe = null;
            Block.popResource(level, worldPosition.above(), output);
            level.playSound(null, worldPosition, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        sync();
    }

    public IItemHandler itemHandler(@Nullable Direction side) {
        return new Handler();
    }

    private Optional<RecipeHolder<ChoppingRecipe>> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(
                ChoppingBlockFeature.RECIPE_TYPE.get(),
                new SingleRecipeInput(stack),
                level
        );
    }

    private void resolveRecipe() {
        activeRecipe = findRecipe(input).map(RecipeHolder::value).orElse(null);
        if (activeRecipe == null) {
            requiredChops = 0;
        }
    }

    private static int toolTier(ItemStack stack) {
        if (!(stack.getItem() instanceof TieredItem tiered)) {
            return 0;
        }
        float speed = tiered.getTier().getSpeed();
        if (speed <= 2.0F) {
            return 0;
        }
        if (speed <= 4.0F) {
            return 1;
        }
        if (speed <= 6.0F) {
            return 2;
        }
        return 3;
    }

    private static int defaultChops(int tier) {
        return switch (tier) {
            case 0 -> PrimitiveTechnologyConfig.CHOPPING_WOOD_CHOPS.get();
            case 1 -> PrimitiveTechnologyConfig.CHOPPING_STONE_CHOPS.get();
            case 2 -> PrimitiveTechnologyConfig.CHOPPING_IRON_CHOPS.get();
            default -> PrimitiveTechnologyConfig.CHOPPING_DIAMOND_CHOPS.get();
        };
    }

    private static int defaultQuantity(int tier) {
        return switch (tier) {
            case 0 -> PrimitiveTechnologyConfig.CHOPPING_WOOD_OUTPUT.get();
            case 1 -> PrimitiveTechnologyConfig.CHOPPING_STONE_OUTPUT.get();
            case 2 -> PrimitiveTechnologyConfig.CHOPPING_IRON_OUTPUT.get();
            default -> PrimitiveTechnologyConfig.CHOPPING_DIAMOND_OUTPUT.get();
        };
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
        chops = tag.getInt("Chops");
        requiredChops = tag.getInt("RequiredChops");
        sawdust = tag.getInt("Sawdust");
        durabilityUntilDamage = tag.getInt("DurabilityUntilDamage");
        resolveRecipe();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!input.isEmpty()) {
            tag.put("Input", input.save(registries));
        }
        tag.putInt("Chops", chops);
        tag.putInt("RequiredChops", requiredChops);
        tag.putInt("Sawdust", sawdust);
        tag.putInt("DurabilityUntilDamage", durabilityUntilDamage);
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

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? input.copy() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || !canInsert(stack)) {
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
            if (slot != 0 || amount <= 0 || input.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack result = input.copyWithCount(1);
            if (!simulate) {
                extract();
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && findRecipe(stack).isPresent();
        }
    }
}
