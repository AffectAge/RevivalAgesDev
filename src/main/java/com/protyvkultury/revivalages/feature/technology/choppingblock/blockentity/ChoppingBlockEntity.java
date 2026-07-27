package com.protyvkultury.revivalages.feature.technology.choppingblock.blockentity;

import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
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
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class ChoppingBlockEntity extends BlockEntity {

    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
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

    public ItemStack output() {
        return output;
    }

    public int sawdust() {
        return sawdust;
    }

    public void removeSawdust() {
        setSawdust(sawdust - 1);
    }

    public double progress() {
        return requiredChops <= 0 ? 0.0D : Math.min(1.0D, chops / (double) requiredChops);
    }

    public ItemStack recipeOutput() {
        return output.isEmpty()
                ? activeRecipe == null ? ItemStack.EMPTY : activeRecipe.result()
                : output.copy();
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
        chops = 0;
        resolveRecipe();
        sync();
    }

    public ItemStack extract() {
        if (!output.isEmpty()) {
            output = FoodFreshnessApi.materialize(output);
            ItemStack result = output;
            output = ItemStack.EMPTY;
            sync();
            return result;
        }
        input = FoodFreshnessApi.materialize(input);
        ItemStack result = input;
        input = ItemStack.EMPTY;
        chops = 0;
        requiredChops = 0;
        activeRecipe = null;
        sync();
        return result;
    }

    public void chop(Player player, ItemStack axe, InteractionHand hand, BlockHitResult hit) {
        ItemStack materialized = FoodFreshnessApi.materialize(input);
        if (materialized != input) {
            input = materialized;
            resolveRecipe();
            sync();
        }
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
            setSawdust(sawdust + 1);
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
                    hit.getLocation().x,
                    hit.getLocation().y,
                    hit.getLocation().z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.02D
            );
            server.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.8D,
                    worldPosition.getZ() + 0.5D,
                    2,
                    0.25D,
                    0.1D,
                    0.25D,
                    0.02D
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
            output = activeRecipe.result();
            output.setCount(activeRecipe.quantityForTier(tier, defaultQuantity(tier)));
            FoodFreshnessApi.copyOldest(output, java.util.List.of(input.copy()));
            input = ItemStack.EMPTY;
            chops = 0;
            requiredChops = 0;
            activeRecipe = null;
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

    private void setSawdust(int value) {
        int clamped = Math.clamp(value, 0, 5);
        if (sawdust == clamped) {
            return;
        }
        sawdust = clamped;
        setChanged();
        if (level != null && !level.isClientSide
                && getBlockState().hasProperty(ChoppingBlock.SAWDUST)
                && getBlockState().getValue(ChoppingBlock.SAWDUST) != clamped) {
            level.setBlock(
                    worldPosition,
                    getBlockState().setValue(ChoppingBlock.SAWDUST, clamped),
                    Block.UPDATE_ALL
            );
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        input = ItemStack.parseOptional(registries, tag.getCompound("Input"));
        output = ItemStack.parseOptional(registries, tag.getCompound("Output"));
        chops = tag.getInt("Chops");
        requiredChops = tag.getInt("RequiredChops");
        sawdust = Math.clamp(tag.getInt("Sawdust"), 0, 5);
        durabilityUntilDamage = tag.getInt("DurabilityUntilDamage");
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
        tag.putInt("Chops", chops);
        tag.putInt("RequiredChops", requiredChops);
        tag.putInt("Sawdust", sawdust);
        tag.putInt("DurabilityUntilDamage", durabilityUntilDamage);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        resolveRecipe();
        if (level != null && !level.isClientSide
                && getBlockState().hasProperty(ChoppingBlock.SAWDUST)
                && getBlockState().getValue(ChoppingBlock.SAWDUST) != sawdust) {
            level.setBlock(
                    worldPosition,
                    getBlockState().setValue(ChoppingBlock.SAWDUST, sawdust),
                    Block.UPDATE_ALL
            );
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

    private final class Handler implements IItemHandler {

        @Override
        public int getSlots() {
            return 2;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return switch (slot) {
                case 0 -> input.copy();
                case 1 -> output.copy();
                default -> ItemStack.EMPTY;
            };
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
            ItemStack stored = slot == 0 ? input : slot == 1 ? output : ItemStack.EMPTY;
            if (amount <= 0 || stored.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack result = stored.copyWithCount(Math.min(amount, stored.getCount()));
            if (!simulate) {
                if (slot == 0) {
                    extract();
                } else {
                    output.shrink(result.getCount());
                    if (output.isEmpty()) {
                        output = ItemStack.EMPTY;
                    }
                    sync();
                }
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? 1 : 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && findRecipe(stack).isPresent();
        }
    }
}
