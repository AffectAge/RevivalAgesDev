package com.protyvkultury.revivalages.feature.inventory.carriedweight;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.weight.ItemWeightDataMaps;
import com.protyvkultury.revivalages.api.weight.RegisterCarriedWeightProvidersEvent;
import com.protyvkultury.revivalages.api.weight.WeightApi;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.content.ContentPolicy;
import com.protyvkultury.revivalages.feature.inventory.carriedweight.client.CarriedWeightClientConfig;
import com.protyvkultury.revivalages.feature.inventory.carriedweight.client.CarriedWeightClientEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class CarriedWeightFeature implements FeatureModule {

    private static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, RevivalAges.MOD_ID);
    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, RevivalAges.MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, RevivalAges.MOD_ID);
    private static final CarriedWeightService SERVICE = new CarriedWeightService();

    public static final DeferredHolder<Attribute, Attribute> CARRY_CAPACITY_BONUS =
            ATTRIBUTES.register(
                    "carry_capacity_bonus",
                    () -> new RangedAttribute(
                            "attribute.name.revivalages.carry_capacity_bonus",
                            0.0D,
                            0.0D,
                            1_000_000_000.0D
                    ).setSyncable(true)
            );
    public static final DeferredHolder<MobEffect, MobEffect> OVERLOADED =
            EFFECTS.register("overloaded", OverloadedMobEffect::new);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CarriedWeightState>> STATE =
            ATTACHMENTS.register(
                    "carried_weight_state",
                    () -> AttachmentType.builder(() -> CarriedWeightState.EMPTY).build()
            );

    @Override
    public ContentPolicy contentPolicy() {
        return ContentPolicy.gameplay("carried_weight")
                .define(ContentKey.CARRIED_WEIGHT, CarriedWeightConfig::configuredEnabled)
                .build();
    }

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        ATTRIBUTES.register(modBus);
        EFFECTS.register(modBus);
        ATTACHMENTS.register(modBus);
        modBus.addListener(this::registerDataMaps);
        modBus.addListener(this::registerPayloads);
        modBus.addListener(this::addPlayerAttribute);
        modBus.addListener(this::onCommonSetup);
        modBus.addListener(this::onConfigLoading);
        modBus.addListener(this::onConfigReloading);
        modContainer.registerConfig(
                ModConfig.Type.SERVER,
                CarriedWeightConfig.SPEC,
                "revivalages-carried-weight-server.toml"
        );
        modContainer.registerConfig(
                ModConfig.Type.CLIENT,
                CarriedWeightClientConfig.SPEC,
                "revivalages-carried-weight-client.toml"
        );
        NeoForge.EVENT_BUS.addListener(this::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerJump);
        NeoForge.EVENT_BUS.addListener(this::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CarriedWeightClientEvents.register(modBus);
        }
        WeightApi.installRuntime(SERVICE);
    }

    public static CarriedWeightState state(Player player) {
        return player.getData(STATE);
    }

    static void setState(Player player, CarriedWeightState state) {
        player.setData(STATE, state);
    }

    static double capacityBonus(Player player) {
        return player.getAttributeValue(CARRY_CAPACITY_BONUS);
    }

    static CarriedWeightService service() {
        return SERVICE;
    }

    static void updatePlayerNow(ServerPlayer player) {
        updatePlayer(player, true);
    }

    static void reloadSettings(MinecraftServer server) {
        CarriedWeightSettings.Snapshot settings = CarriedWeightSettings.refreshLocal();
        PacketDistributor.sendToAllPlayers(new CarriedWeightSettingsPayload(settings));
        server.getPlayerList().getPlayers().forEach(player -> updatePlayer(player, true));
    }

    private void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(ItemWeightDataMaps.ITEM_WEIGHT);
        event.register(ItemWeightDataMaps.POCKETS);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(
                        CarriedWeightSettingsPayload.TYPE,
                        CarriedWeightSettingsPayload.STREAM_CODEC,
                        CarriedWeightSettingsPayload::handle
                )
                .playToClient(
                        CarriedWeightStatePayload.TYPE,
                        CarriedWeightStatePayload.STREAM_CODEC,
                        CarriedWeightStatePayload::handle
                );
    }

    private void addPlayerAttribute(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, CARRY_CAPACITY_BONUS, 0.0D);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NeoForge.EVENT_BUS.post(new RegisterCarriedWeightProvidersEvent(SERVICE));
            SERVICE.freeze();
            CarriedWeightSettings.refreshLocal();
        });
    }

    private void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        CarriedWeightSettings.Snapshot settings = CarriedWeightSettings.snapshot();
        if (player.tickCount % settings.updateIntervalTicks() != 0) {
            return;
        }
        updatePlayer(player, false);
    }

    private void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            CarriedWeightPenaltyService.onJump(player);
        }
    }

    private void onDatapackSync(OnDatapackSyncEvent event) {
        CarriedWeightSettings.Snapshot settings = CarriedWeightSettings.refreshLocal();
        CarriedWeightSettingsPayload settingsPayload = new CarriedWeightSettingsPayload(settings);
        event.getRelevantPlayers().forEach(player -> {
            PacketDistributor.sendToPlayer(player, settingsPayload);
            updatePlayer(player, true);
        });
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CarriedWeightCommands.register(event.getDispatcher());
    }

    private void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == CarriedWeightConfig.SPEC) {
            CarriedWeightSettings.refreshLocal();
        }
    }

    private void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != CarriedWeightConfig.SPEC) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            CarriedWeightSettings.refreshLocal();
            return;
        }
        reloadSettings(server);
    }

    private static void updatePlayer(ServerPlayer player, boolean forceSync) {
        CarriedWeightState previous = state(player);
        CarriedWeightState updated;
        if (!CarriedWeightSettings.enabled()) {
            updated = CarriedWeightState.EMPTY;
            CarriedWeightPenaltyService.clear(player);
        } else {
            double current = SERVICE.getCarriedWeight(player).weight();
            double capacity = SERVICE.getCapacity(player);
            boolean overloaded = !player.isCreative()
                    && !player.isSpectator()
                    && current >= capacity;
            updated = new CarriedWeightState(current, capacity, overloaded);
            CarriedWeightPenaltyService.apply(player, updated);
        }
        if (forceSync || !updated.equals(previous)) {
            setState(player, updated);
            PacketDistributor.sendToPlayer(player, new CarriedWeightStatePayload(updated));
        }
    }
}
