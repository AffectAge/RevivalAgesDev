package com.protyvkultury.revivalages.feature.technology.constructionframe.blockentity;

import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature;
import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameConfig;
import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameFeature;
import com.protyvkultury.revivalages.feature.technology.constructionframe.recipe.FrameAssemblyInput;
import com.protyvkultury.revivalages.feature.technology.constructionframe.recipe.FrameAssemblyRecipe;
import com.protyvkultury.revivalages.feature.technology.constructionframe.recipe.FrameGridPosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class ConstructionFrameBlockEntity extends BlockEntity {

    private static final String ITEMS_TAG = "Items";
    private static final String STATES_TAG = "BlockStates";
    private final NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private final NonNullList<BlockState> displayStates =
            NonNullList.withSize(27, Blocks.AIR.defaultBlockState());
    private boolean assembling;

    public ConstructionFrameBlockEntity(BlockPos pos, BlockState state) {
        super(ConstructionFrameFeature.BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStack item(int slot) {
        return items.get(slot);
    }

    public BlockState displayState(int slot) {
        return displayStates.get(slot);
    }

    public int occupancyMask() {
        int mask = 0;
        for (int slot = 0; slot < items.size(); slot++) {
            if (!items.get(slot).isEmpty()) {
                mask |= 1 << slot;
            }
        }
        return mask;
    }

    public int occupiedCells() {
        return Integer.bitCount(occupancyMask());
    }

    public boolean isAssembling() {
        return assembling;
    }

    public boolean insert(int slot, ItemStack source, boolean infinite) {
        if (slot < 0 || slot >= 27 || source.isEmpty() || !items.get(slot).isEmpty()) {
            return false;
        }
        items.set(slot, source.copyWithCount(1));
        displayStates.set(slot, displayStateFor(source));
        if (!infinite) {
            source.shrink(1);
        }
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.5F, 1.0F);
        }
        sync();
        return true;
    }

    public ItemStack extract(int slot) {
        if (slot < 0 || slot >= 27) {
            return ItemStack.EMPTY;
        }
        ItemStack result = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        displayStates.set(slot, Blocks.AIR.defaultBlockState());
        sync();
        return result;
    }

    public boolean tryAssemble(Player player, InteractionHand hand, ItemStack tool, BlockHitResult originalHit) {
        if (!(level instanceof ServerLevel server) || tool.isEmpty()) {
            return false;
        }
        FrameAssemblyInput input = new FrameAssemblyInput(items);
        Optional<RecipeHolder<FrameAssemblyRecipe>> match = server.getRecipeManager()
                .getAllRecipesFor(ConstructionFrameFeature.RECIPE_TYPE.get())
                .stream()
                .filter(holder -> holder.value().matches(input, server))
                .filter(holder -> holder.value().matchesTool(tool))
                .findFirst();
        if (match.isEmpty()) {
            return false;
        }
        FrameAssemblyRecipe recipe = match.get().value();
        ItemStack result = recipe.assemble(input, server.registryAccess());
        applyWoodVariant(recipe, result);
        if (!(result.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        return placeResultTransaction(server, player, hand, tool, result, blockItem, originalHit);
    }

    public Optional<FrameAssemblyRecipe> matchingRecipe() {
        if (level == null) {
            return Optional.empty();
        }
        FrameAssemblyInput input = new FrameAssemblyInput(items);
        return level.getRecipeManager()
                .getAllRecipesFor(ConstructionFrameFeature.RECIPE_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .filter(recipe -> recipe.matches(input, level))
                .findFirst();
    }

    public boolean isPlacementBlocked(FrameAssemblyRecipe recipe) {
        if (level == null || !(recipe.result().getItem() instanceof BlockItem blockItem)) {
            return true;
        }
        BlockState resultState = blockItem.getBlock().defaultBlockState();
        if (!resultState.canSurvive(level, worldPosition)
                || !level.isUnobstructed(resultState, worldPosition, CollisionContext.empty())) {
            return true;
        }
        return resultState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && (!level.isInWorldBounds(worldPosition.above())
                || !level.getBlockState(worldPosition.above()).canBeReplaced());
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        Containers.dropContents(level, worldPosition, new SimpleContainer(items.toArray(ItemStack[]::new)));
        items.replaceAll(ignored -> ItemStack.EMPTY);
        displayStates.replaceAll(ignored -> Blocks.AIR.defaultBlockState());
    }

    private boolean placeResultTransaction(
            ServerLevel server,
            Player player,
            InteractionHand hand,
            ItemStack tool,
            ItemStack result,
            BlockItem blockItem,
            BlockHitResult originalHit
    ) {
        List<ItemStack> snapshotItems = items.stream().map(ItemStack::copy).toList();
        List<BlockState> snapshotStates = List.copyOf(displayStates);
        BlockState frameState = getBlockState();
        BlockHitResult placementHit = new BlockHitResult(
                Vec3.atCenterOf(worldPosition.below()),
                net.minecraft.core.Direction.UP,
                worldPosition.below(),
                false
        );
        assembling = true;
        server.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        InteractionResult placed = blockItem.place(new BlockPlaceContext(player, hand, result, placementHit));
        if (!placed.consumesAction()) {
            server.setBlock(worldPosition, frameState, Block.UPDATE_ALL);
            if (server.getBlockEntity(worldPosition) instanceof ConstructionFrameBlockEntity restored) {
                restored.restore(snapshotItems, snapshotStates);
            }
            return false;
        }
        int durability = ConstructionFrameConfig.toolDurabilityCost();
        if (durability > 0 && !player.hasInfiniteMaterials()) {
            tool.hurtAndBreak(durability, player, LivingEntity.getSlotForHand(hand));
        }
        BlockState placedState = server.getBlockState(worldPosition);
        server.playSound(null, worldPosition, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        server.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, placedState),
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D,
                200,
                0.3D,
                0.3D,
                0.3D,
                0.15D
        );
        return true;
    }

    private void applyWoodVariant(FrameAssemblyRecipe recipe, ItemStack result) {
        recipe.woodVariantSource().ifPresent(source -> {
            ItemStack sourceStack = items.get(source.index());
            if (!sourceStack.isEmpty()) {
                result.set(
                        AnimalPowerFeature.WOOD_VARIANT.get(),
                        BuiltInRegistries.ITEM.getKey(sourceStack.getItem())
                );
            }
        });
    }

    private static BlockState displayStateFor(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState();
        }
        return Blocks.AIR.defaultBlockState();
    }

    private void restore(List<ItemStack> restoredItems, List<BlockState> restoredStates) {
        for (int slot = 0; slot < 27; slot++) {
            items.set(slot, restoredItems.get(slot).copy());
            displayStates.set(slot, restoredStates.get(slot));
        }
        assembling = false;
        sync();
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
        ListTag itemTags = tag.getList(ITEMS_TAG, Tag.TAG_COMPOUND);
        ListTag stateTags = tag.getList(STATES_TAG, Tag.TAG_COMPOUND);
        for (int slot = 0; slot < 27; slot++) {
            items.set(slot, slot < itemTags.size()
                    ? ItemStack.parseOptional(registries, itemTags.getCompound(slot))
                    : ItemStack.EMPTY);
            displayStates.set(slot, slot < stateTags.size()
                    ? NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), stateTags.getCompound(slot))
                    : displayStateFor(items.get(slot)));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag itemTags = new ListTag();
        ListTag stateTags = new ListTag();
        for (int slot = 0; slot < 27; slot++) {
            itemTags.add(items.get(slot).saveOptional(registries));
            stateTags.add(NbtUtils.writeBlockState(displayStates.get(slot)));
        }
        tag.put(ITEMS_TAG, itemTags);
        tag.put(STATES_TAG, stateTags);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
