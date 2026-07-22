package com.protyvkultury.revivalages.feature.technology.ignition;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.technology.ignition.block.WoodTorchBlock;
import com.protyvkultury.revivalages.feature.technology.ignition.block.WoodTorchState;
import com.protyvkultury.revivalages.feature.technology.ignition.blockentity.WoodTorchBlockEntity;
import com.protyvkultury.revivalages.feature.technology.ignition.item.FlintAndTinderItem;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponentType;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IgnitionFeature implements FeatureModule {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, RevivalAges.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> IGNITER_USES =
            DATA_COMPONENTS.registerComponentType("igniter_uses", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredItem<FlintAndTinderItem> FLINT_AND_TINDER = ITEMS.registerItem(
            "flint_and_tinder", FlintAndTinderItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredBlock<WoodTorchBlock> WOOD_TORCH = BLOCKS.registerBlock(
            "wood_torch", WoodTorchBlock::new,
            BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> WOOD_TORCH_ITEM =
            ITEMS.registerSimpleBlockItem(WOOD_TORCH, new Item.Properties());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodTorchBlockEntity>> WOOD_TORCH_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("wood_torch", () -> BlockEntityType.Builder.of(
                    WoodTorchBlockEntity::new, WOOD_TORCH.get()).build(null));

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        DATA_COMPONENTS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
