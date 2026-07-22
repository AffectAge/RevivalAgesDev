package com.protyvkultury.revivalages.feature.technology.pitburn;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.pitburn.block.ActivePileBlock;
import com.protyvkultury.revivalages.feature.technology.pitburn.block.AshPileBlock;
import com.protyvkultury.revivalages.feature.technology.pitburn.block.LogPileBlock;
import com.protyvkultury.revivalages.feature.technology.pitburn.blockentity.PitBurnBlockEntity;
import com.protyvkultury.revivalages.feature.technology.pitburn.recipe.PitBurnRecipe;
import com.protyvkultury.revivalages.feature.technology.pitburn.recipe.PitBurnRecipeSerializer;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PitBurnFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, RevivalAges.MOD_ID);

    public static final DeferredBlock<LogPileBlock> LOG_PILE = BLOCKS.registerBlock(
            "log_pile", LogPileBlock::new,
            BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.WOOD).ignitedByLava());
    public static final DeferredItem<BlockItem> LOG_PILE_ITEM =
            ITEMS.registerSimpleBlockItem(LOG_PILE, new Item.Properties());
    public static final DeferredBlock<ActivePileBlock> ACTIVE_PILE = BLOCKS.registerBlock(
            "active_pile", ActivePileBlock::new,
            BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.STONE).lightLevel(state -> 7).noLootTable());
    public static final DeferredBlock<AshPileBlock> ASH_PILE = BLOCKS.registerBlock(
            "ash_pile", AshPileBlock::new,
            BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.GRAVEL).noLootTable());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PitBurnBlockEntity>> BLOCK_ENTITY =
            BLOCK_ENTITIES.register("pit_burn", () -> BlockEntityType.Builder.of(
                    PitBurnBlockEntity::new, ACTIVE_PILE.get(), ASH_PILE.get()).build(null));
    public static final DeferredHolder<RecipeType<?>, RecipeType<PitBurnRecipe>> RECIPE_TYPE =
            RECIPE_TYPES.register("pit_burn", simpleRecipeType());
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PitBurnRecipe>> RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("pit_burn", PitBurnRecipeSerializer::new);

    private static Supplier<RecipeType<PitBurnRecipe>> simpleRecipeType() {
        return () -> new RecipeType<>() {
            @Override
            public String toString() {
                return RevivalAges.id("pit_burn").toString();
            }
        };
    }

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
    }

    public static boolean canIgnite(Level level, BlockPos origin) {
        return collectConnected(level, origin).isPresent()
                && (level.isClientSide || findRecipe(level).isPresent());
    }

    public static boolean ignite(Level level, BlockPos origin) {
        Optional<RecipeHolder<PitBurnRecipe>> recipe = findRecipe(level);
        Optional<Set<BlockPos>> connected = collectConnected(level, origin);
        if (recipe.isEmpty() || connected.isEmpty()) {
            return false;
        }
        ItemStack input = new ItemStack(LOG_PILE_ITEM.get());
        for (BlockPos pos : connected.get()) {
            level.setBlock(pos, ACTIVE_PILE.get().defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof PitBurnBlockEntity burn) {
                burn.initialize(input, recipe.get().value());
            }
        }
        return true;
    }

    private static Optional<RecipeHolder<PitBurnRecipe>> findRecipe(Level level) {
        if (level.isClientSide) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(
                RECIPE_TYPE.get(), new SingleRecipeInput(new ItemStack(LOG_PILE_ITEM.get())), level);
    }

    private static Optional<Set<BlockPos>> collectConnected(Level level, BlockPos origin) {
        if (!level.getBlockState(origin).is(LOG_PILE.get())) {
            return Optional.empty();
        }
        int limit = PrimitiveTechnologyConfig.PIT_BURN_MAX_BLOCKS.get();
        Set<BlockPos> found = new LinkedHashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        pending.add(origin.immutable());
        while (!pending.isEmpty()) {
            BlockPos pos = pending.removeFirst();
            if (found.contains(pos) || !level.getBlockState(pos).is(LOG_PILE.get())) {
                continue;
            }
            found.add(pos.immutable());
            if (found.size() > limit) {
                return Optional.empty();
            }
            for (Direction direction : Direction.values()) {
                pending.addLast(pos.relative(direction));
            }
        }
        return Optional.of(found);
    }
}
