package com.protyvkultury.revivalages.feature.technology.knapping;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.content.ContentPolicy;
import com.protyvkultury.revivalages.feature.technology.knapping.client.KnappingClientEvents;
import com.protyvkultury.revivalages.feature.technology.knapping.menu.KnappingMenu;
import com.protyvkultury.revivalages.feature.technology.knapping.network.KnappingCellPayload;
import com.protyvkultury.revivalages.feature.technology.knapping.network.KnappingStatePayload;
import com.protyvkultury.revivalages.feature.technology.knapping.recipe.KnappingRecipe;
import com.protyvkultury.revivalages.feature.technology.knapping.recipe.KnappingRecipeSerializer;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class KnappingFeature implements FeatureModule {

    public static final ResourceKey<Registry<KnappingType>> KNAPPING_TYPES =
            ResourceKey.createRegistryKey(RevivalAges.id("knapping_type"));

    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, RevivalAges.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, RevivalAges.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, RevivalAges.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, RevivalAges.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<KnappingMenu>> MENU = MENUS.register(
            "knapping",
            () -> IMenuTypeExtension.create((containerId, inventory, buffer) ->
                    new KnappingMenu(
                            containerId,
                            inventory,
                            ResourceLocation.STREAM_CODEC.decode(buffer),
                            buffer.readUnsignedByte()
                    ))
    );
    public static final DeferredHolder<RecipeType<?>, RecipeType<KnappingRecipe>> RECIPE_TYPE =
            RECIPE_TYPES.register("knapping", recipeType());
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<KnappingRecipe>> RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("knapping", KnappingRecipeSerializer::new);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<KnappingEnabledCondition>>
            ENABLED_CONDITION = CONDITIONS.register("knapping_enabled", () -> KnappingEnabledCondition.CODEC);
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_CLICK =
            sound("knapping_stone");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLAY_CLICK =
            sound("knapping_clay");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEATHER_CLICK =
            sound("knapping_leather");

    @Override
    public ContentPolicy contentPolicy() {
        return ContentPolicy.gameplay("knapping")
                .define(ContentKey.KNAPPING, KnappingConfig::configuredEnabled)
                .build();
    }

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        MENUS.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        CONDITIONS.register(modBus);
        SOUNDS.register(modBus);
        modBus.addListener(this::registerDataPackRegistry);
        modBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(this::onUseItem);
        modContainer.registerConfig(ModConfig.Type.SERVER, KnappingConfig.SERVER_SPEC, "revivalages-knapping-server.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, KnappingConfig.CLIENT_SPEC, "revivalages-knapping-client.toml");
        if (FMLEnvironment.dist == Dist.CLIENT) {
            KnappingClientEvents.register(modBus);
        }
    }

    private void registerDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(KNAPPING_TYPES, KnappingType.CODEC, KnappingType.CODEC);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                KnappingCellPayload.TYPE,
                KnappingCellPayload.STREAM_CODEC,
                KnappingCellPayload::handle
        ).playToClient(
                KnappingStatePayload.TYPE,
                KnappingStatePayload.STREAM_CODEC,
                KnappingStatePayload::handle
        );
    }

    private void onUseItem(PlayerInteractEvent.RightClickItem event) {
        if (!KnappingConfig.enabled()
                || event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ResourceLocation typeId = findType(player);
        if (typeId == null) {
            return;
        }
        int hotbarIndex = player.getInventory().selected;
        player.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new KnappingMenu(containerId, inventory, typeId, hotbarIndex),
                        Component.translatable("container.revivalages.knapping")
                ),
                buffer -> {
                    ResourceLocation.STREAM_CODEC.encode(buffer, typeId);
                    buffer.writeByte(hotbarIndex);
                }
        );
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static ResourceLocation findType(ServerPlayer player) {
        return player.registryAccess()
                .registryOrThrow(KNAPPING_TYPES)
                .entrySet()
                .stream()
                .filter(entry -> player.getAbilities().instabuild
                        ? entry.getValue().input().ingredient().test(player.getMainHandItem())
                        : entry.getValue().input().test(player.getMainHandItem()))
                .min(Comparator.comparing(entry -> entry.getKey().location()))
                .map(Map.Entry::getKey)
                .map(ResourceKey::location)
                .orElse(null);
    }

    private static Supplier<RecipeType<KnappingRecipe>> recipeType() {
        return () -> new RecipeType<>() {
            @Override
            public String toString() {
                return RevivalAges.id("knapping").toString();
            }
        };
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(RevivalAges.id(name)));
    }
}
