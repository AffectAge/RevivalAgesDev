package com.protyvkultury.revivalages.feature.technology.anvil.blockentity;

import com.protyvkultury.revivalages.feature.technology.anvil.AnvilFeature;
import com.protyvkultury.revivalages.feature.technology.anvil.AnvilTags;
import com.protyvkultury.revivalages.feature.technology.anvil.block.AnvilBlock;
import com.protyvkultury.revivalages.feature.technology.anvil.recipe.AnvilRecipe;
import com.protyvkultury.revivalages.feature.technology.anvil.recipe.AnvilTool;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;

public final class AnvilBlockEntity extends BlockEntity {

    private ItemStack input = ItemStack.EMPTY;
    private int hits;
    private int requiredHits;
    private int durabilityUntilDamage = PrimitiveTechnologyConfig.ANVIL_HITS_PER_DAMAGE_STAGE.get();
    private AnvilRecipe activeRecipe;
    private AnvilTool activeTool;

    public AnvilBlockEntity(BlockPos pos, BlockState state) {
        super(AnvilFeature.BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStack input() {
        return input;
    }

    public ItemStack recipeOutput() {
        resolveRecipe();
        return activeRecipe == null ? ItemStack.EMPTY : activeRecipe.result();
    }

    public double progress() {
        return requiredHits <= 0 ? 0.0D : Math.min(1.0D, hits / (double) requiredHits);
    }

    public int hits() {
        return hits;
    }

    public int requiredHits() {
        return requiredHits;
    }

    public AnvilTool activeTool() {
        return activeTool;
    }

    public boolean canInsert(ItemStack stack) {
        return input.isEmpty() && findAnyRecipe(stack).isPresent();
    }

    public void insert(ItemStack source, boolean infinite) {
        if (!canInsert(source)) {
            return;
        }
        input = source.copyWithCount(1);
        if (!infinite) {
            source.shrink(1);
        }
        hits = 0;
        requiredHits = 0;
        activeRecipe = null;
        activeTool = null;
        if (level != null) {
            level.playSound(
                    null,
                    worldPosition,
                    SoundEvents.WOOD_PLACE,
                    SoundSource.BLOCKS,
                    0.5F,
                    (float) (1.0D + level.random.nextGaussian() * 0.4D));
        }
        sync();
    }

    public ItemStack extract() {
        ItemStack result = input;
        input = ItemStack.EMPTY;
        hits = 0;
        requiredHits = 0;
        activeRecipe = null;
        activeTool = null;
        sync();
        return result;
    }

    public boolean isValidTool(ItemStack tool) {
        return tool.is(AnvilTags.HAMMERS) || tool.is(ItemTags.PICKAXES);
    }

    public void hit(Player player, ItemStack tool, InteractionHand hand, Vec3 hitPosition) {
        if (level == null || input.isEmpty()) {
            return;
        }
        if (player.getFoodData().getFoodLevel() < PrimitiveTechnologyConfig.ANVIL_MINIMUM_HUNGER.get()) {
            player.displayClientMessage(Component.translatable("message.revivalages.anvil.not_enough_hunger"), true);
            return;
        }
        AnvilTool type = tool.is(AnvilTags.HAMMERS) ? AnvilTool.HAMMER : AnvilTool.PICKAXE;
        Optional<RecipeHolder<AnvilRecipe>> found = findRecipe(input, type);
        if (found.isEmpty()) {
            return;
        }
        AnvilRecipe recipe = found.get().value();
        if (activeRecipe != recipe && activeTool != type) {
            activeRecipe = recipe;
            hits = 0;
            requiredHits = recipe.hits();
        } else {
            activeRecipe = recipe;
            requiredHits = Math.max(1, requiredHits);
        }
        activeTool = type;

        hits++;
        durabilityUntilDamage--;
        player.causeFoodExhaustion(PrimitiveTechnologyConfig.ANVIL_EXHAUSTION_PER_HIT.get().floatValue());
        if (PrimitiveTechnologyConfig.ANVIL_USE_TOOL_DURABILITY.get()) {
            tool.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }
        level.playSound(null, worldPosition, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.75F,
                (float) (1.0D + level.random.nextGaussian() * 0.4D));
        if (level instanceof ServerLevel server) {
            server.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, getBlockState()),
                    hitPosition.x,
                    hitPosition.y,
                    hitPosition.z,
                    8,
                    0.15D,
                    0.05D,
                    0.15D,
                    0.04D
            );
        }

        if (durabilityUntilDamage <= 0) {
            durabilityUntilDamage = PrimitiveTechnologyConfig.ANVIL_HITS_PER_DAMAGE_STAGE.get();
            int damage = getBlockState().getValue(AnvilBlock.DAMAGE);
            if (damage >= 3) {
                ItemStack preserved = input.copy();
                input = ItemStack.EMPTY;
                level.destroyBlock(worldPosition, false);
                Block.popResource(level, worldPosition.above(), preserved);
                return;
            }
            level.setBlock(worldPosition, getBlockState().setValue(AnvilBlock.DAMAGE, damage + 1), Block.UPDATE_ALL);
        }

        if (hits >= requiredHits) {
            ItemStack output = recipe.result();
            input = ItemStack.EMPTY;
            hits = 0;
            requiredHits = 0;
            activeRecipe = null;
            activeTool = null;
            player.causeFoodExhaustion(PrimitiveTechnologyConfig.ANVIL_EXHAUSTION_PER_CRAFT.get().floatValue());
            if (!player.addItem(output)) {
                Block.popResource(level, worldPosition.above(), output);
            }
            level.playSound(null, worldPosition, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F,
                    (float) (1.0D + level.random.nextGaussian() * 0.4D));
        }
        sync();
    }

    public void dropContents() {
        if (level != null && !input.isEmpty()) {
            Block.popResource(level, worldPosition.above(), input);
            input = ItemStack.EMPTY;
        }
    }

    public IItemHandler itemHandler() {
        return new Handler();
    }

    private Optional<RecipeHolder<AnvilRecipe>> findAnyRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipesFor(AnvilFeature.RECIPE_TYPE.get(), new SingleRecipeInput(stack), level)
                .stream()
                .min(Comparator.comparing(holder -> holder.id().toString()));
    }

    private Optional<RecipeHolder<AnvilRecipe>> findRecipe(ItemStack stack, AnvilTool tool) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipesFor(AnvilFeature.RECIPE_TYPE.get(), new SingleRecipeInput(stack), level)
                .stream()
                .filter(holder -> holder.value().tool() == tool)
                .min(Comparator.comparing(holder -> holder.id().toString()));
    }

    private void resolveRecipe() {
        if (activeTool == null || input.isEmpty() || level == null) {
            return;
        }
        activeRecipe = findRecipe(input, activeTool).map(RecipeHolder::value).orElse(null);
        if (activeRecipe == null) {
            hits = 0;
            requiredHits = 0;
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
        hits = tag.getInt("Hits");
        requiredHits = tag.getInt("RequiredHits");
        durabilityUntilDamage = tag.contains("DurabilityUntilDamage")
                ? tag.getInt("DurabilityUntilDamage")
                : PrimitiveTechnologyConfig.ANVIL_HITS_PER_DAMAGE_STAGE.get();
        String savedTool = tag.getString("Tool");
        activeTool = java.util.Arrays.stream(AnvilTool.values())
                .filter(tool -> tool.name().equals(savedTool))
                .findFirst()
                .orElse(null);
        activeRecipe = null;
        resolveRecipe();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!input.isEmpty()) {
            tag.put("Input", input.save(registries));
        }
        tag.putInt("Hits", hits);
        tag.putInt("RequiredHits", requiredHits);
        tag.putInt("DurabilityUntilDamage", durabilityUntilDamage);
        if (activeTool != null) {
            tag.putString("Tool", activeTool.name());
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        resolveRecipe();
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
                insert(stack.copyWithCount(1), true);
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || amount <= 0 || input.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack result = input.copy();
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
            return slot == 0 && findAnyRecipe(stack).isPresent();
        }
    }
}
