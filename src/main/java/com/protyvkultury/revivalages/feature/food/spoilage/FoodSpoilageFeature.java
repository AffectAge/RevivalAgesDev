package com.protyvkultury.revivalages.feature.food.spoilage;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
import com.protyvkultury.revivalages.api.food.FoodSpoilageDataMaps;
import com.protyvkultury.revivalages.api.food.FoodState;
import com.protyvkultury.revivalages.api.food.FoodTrait;
import com.protyvkultury.revivalages.api.food.FoodOutputPolicy;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.content.ContentPolicy;
import com.protyvkultury.revivalages.feature.food.spoilage.client.FoodSpoilageClientConfig;
import com.protyvkultury.revivalages.feature.food.spoilage.client.FoodSpoilageClientEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public final class FoodSpoilageFeature implements FeatureModule {

    public static final ResourceKey<Registry<FoodTrait>> FOOD_TRAITS =
            ResourceKey.createRegistryKey(RevivalAges.id("food_trait"));
    public static final ResourceKey<Registry<FoodOutputPolicy>> FOOD_OUTPUT_POLICIES =
            ResourceKey.createRegistryKey(RevivalAges.id("food_output_policy"));

    private static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, RevivalAges.MOD_ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FoodState>> FOOD_STATE =
            DATA_COMPONENTS.registerComponentType("food_state", builder -> builder
                    .persistent(FoodState.CODEC)
                    .networkSynchronized(FoodState.STREAM_CODEC)
                    .cacheEncoding());

    private static final FoodFreshnessService SERVICE = new FoodFreshnessService();

    public static boolean mayStack(ItemStack first, ItemStack second) {
        return SERVICE.mayMerge(first, second);
    }

    public static void inheritOldestAfterMerge(ItemStack target, ItemStack source) {
        SERVICE.inheritOldestAfterMerge(target, source);
    }

    @Override
    public ContentPolicy contentPolicy() {
        return ContentPolicy.gameplay("food_spoilage")
                .define(ContentKey.FOOD_SPOILAGE, FoodSpoilageConfig::configuredEnabled)
                .build();
    }

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        DATA_COMPONENTS.register(modBus);
        modBus.addListener(this::registerDataPackRegistries);
        modBus.addListener(this::registerDataMaps);
        modBus.addListener(this::registerPayloads);
        modBus.addListener(this::onConfigReload);
        modContainer.registerConfig(
                ModConfig.Type.SERVER,
                FoodSpoilageConfig.SPEC,
                "revivalages-food-spoilage-server.toml"
        );
        modContainer.registerConfig(
                ModConfig.Type.CLIENT,
                FoodSpoilageClientConfig.SPEC,
                "revivalages-food-spoilage-client.toml"
        );
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(this::onEntityTick);
        NeoForge.EVENT_BUS.addListener(this::onSleepFinished);
        NeoForge.EVENT_BUS.addListener(this::onUseStart);
        NeoForge.EVENT_BUS.addListener(this::onLogin);
        NeoForge.EVENT_BUS.addListener(this::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            FoodSpoilageClientEvents.register(modBus);
        }
        FoodFreshnessApi.install(SERVICE);
    }

    private void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(FOOD_TRAITS, FoodTrait.CODEC, FoodTrait.CODEC);
        event.dataPackRegistry(FOOD_OUTPUT_POLICIES, FoodOutputPolicy.CODEC, FoodOutputPolicy.CODEC);
    }

    private void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(FoodSpoilageDataMaps.ITEM_SPOILAGE);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                FoodClockPayload.TYPE,
                FoodClockPayload.STREAM_CODEC,
                FoodClockPayload::handle
        );
    }

    private void onServerTick(ServerTickEvent.Post event) {
        if (!FoodSpoilageConfig.configuredEnabled() || event.getServer().overworld() == null) {
            return;
        }
        SpoilageClockData data = SpoilageClockData.get(event.getServer().overworld());
        data.advance(1L);
        if (data.ticks() % 200L == 0L) {
            event.getServer().getPlayerList().getPlayers().forEach(this::sync);
        }
    }

    private void onSleepFinished(SleepFinishedTimeEvent event) {
        if (!FoodSpoilageConfig.configuredEnabled()
                || !value(FoodSpoilageConfig.AGE_THROUGH_SLEEP)
                || !(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)
                || !level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            return;
        }
        long skipped = Math.max(0L, event.getNewTime() - level.getDayTime());
        SpoilageClockData.get(level).advance(skipped);
    }

    private void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !FoodSpoilageConfig.configuredEnabled()
                || player.tickCount % integer(FoodSpoilageConfig.MATERIALIZATION_CADENCE) != 0) {
            return;
        }
        sweep(player.getInventory());
        AbstractContainerMenu menu = player.containerMenu;
        for (Slot slot : menu.slots) {
            ItemStack before = slot.getItem();
            ItemStack after = SERVICE.materialize(before);
            if (after != before) {
                slot.set(after);
            }
        }
        ItemStack carried = menu.getCarried();
        ItemStack replaced = SERVICE.materialize(carried);
        if (replaced != carried) {
            menu.setCarried(replaced);
        }
        menu.broadcastChanges();
    }

    private void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity item)
                || item.level().isClientSide()
                || !FoodSpoilageConfig.configuredEnabled()
                || item.tickCount % integer(FoodSpoilageConfig.MATERIALIZATION_CADENCE) != 0) {
            return;
        }
        ItemStack before = item.getItem();
        ItemStack after = SERVICE.materialize(before);
        if (after != before) {
            item.setItem(after);
        }
    }

    private void onUseStart(LivingEntityUseItemEvent.Start event) {
        if (!event.getEntity().level().isClientSide()
                && FoodSpoilageConfig.configuredEnabled()
                && FoodFreshnessApi.profile(event.getItem()).isPresent()) {
            SERVICE.initialize(event.getItem());
            if (SERVICE.expired(event.getItem())) {
                event.setCanceled(true);
            }
        }
    }

    private void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    private void onDatapackSync(OnDatapackSyncEvent event) {
        event.getRelevantPlayers().forEach(this::sync);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        FoodSpoilageCommands.register(event.getDispatcher());
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != FoodSpoilageConfig.SPEC) {
            return;
        }
        net.minecraft.server.MinecraftServer server =
                net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(() -> server.getPlayerList().getPlayers().forEach(this::sync));
        }
    }

    private void sweep(Container container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack before = container.getItem(slot);
            ItemStack after = SERVICE.materialize(before);
            if (after != before) {
                container.setItem(slot, after);
            }
        }
        container.setChanged();
    }

    private void sync(ServerPlayer player) {
        long ticks = SpoilageClockData.get(player.server.overworld()).ticks();
        PacketDistributor.sendToPlayer(player, new FoodClockPayload(
                ticks,
                FoodSpoilageConfig.configuredEnabled(),
                longValue(FoodSpoilageConfig.BASE_LIFETIME_TICKS),
                doubleValue(FoodSpoilageConfig.GLOBAL_DECAY_MULTIPLIER)
        ));
    }

    private static boolean value(net.neoforged.neoforge.common.ModConfigSpec.BooleanValue config) {
        return FoodSpoilageConfig.SPEC.isLoaded() ? config.get() : config.getDefault();
    }

    private static int integer(net.neoforged.neoforge.common.ModConfigSpec.IntValue config) {
        return FoodSpoilageConfig.SPEC.isLoaded() ? config.get() : config.getDefault();
    }

    private static long longValue(net.neoforged.neoforge.common.ModConfigSpec.LongValue config) {
        return FoodSpoilageConfig.SPEC.isLoaded() ? config.get() : config.getDefault();
    }

    private static double doubleValue(net.neoforged.neoforge.common.ModConfigSpec.DoubleValue config) {
        return FoodSpoilageConfig.SPEC.isLoaded() ? config.get() : config.getDefault();
    }
}
