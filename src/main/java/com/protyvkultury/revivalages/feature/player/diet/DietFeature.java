package com.protyvkultury.revivalages.feature.player.diet;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.diet.DietApi;
import com.protyvkultury.revivalages.api.diet.DietDataMaps;
import com.protyvkultury.revivalages.api.diet.DietEffectRule;
import com.protyvkultury.revivalages.api.diet.DietGroup;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.content.ContentPolicy;
import com.protyvkultury.revivalages.feature.player.diet.client.DietClientConfig;
import com.protyvkultury.revivalages.feature.player.diet.client.DietClientEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public final class DietFeature implements FeatureModule {

    public static final ResourceKey<Registry<DietGroup>> DIET_GROUPS =
            ResourceKey.createRegistryKey(RevivalAges.id("diet_group"));
    public static final ResourceKey<Registry<DietEffectRule>> DIET_EFFECT_RULES =
            ResourceKey.createRegistryKey(RevivalAges.id("diet_effect_rule"));

    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, RevivalAges.MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, RevivalAges.MOD_ID);
    private static final DietService SERVICE = new DietService();

    public static final DeferredHolder<MobEffect, MobEffect> DIET_TOUGHNESS =
            EFFECTS.register("diet_toughness", DietToughnessEffect::new);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DietState>> STATE =
            ATTACHMENTS.register(
                    "diet_state",
                    () -> AttachmentType.builder(() -> DietState.EMPTY)
                            .serialize(DietState.CODEC)
                            .build()
            );

    @Override
    public ContentPolicy contentPolicy() {
        return ContentPolicy.gameplay("diet")
                .define(ContentKey.DIET, DietConfig::configuredEnabled)
                .build();
    }

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        EFFECTS.register(modBus);
        ATTACHMENTS.register(modBus);
        modBus.addListener(this::registerDataPackRegistries);
        modBus.addListener(this::registerDataMaps);
        modBus.addListener(this::registerPayloads);
        modBus.addListener(this::onConfigReload);
        modContainer.registerConfig(ModConfig.Type.SERVER, DietConfig.SPEC, "revivalages-diet-server.toml");
        modContainer.registerConfig(
                ModConfig.Type.CLIENT,
                DietClientConfig.SPEC,
                "revivalages-diet-client.toml"
        );
        NeoForge.EVENT_BUS.addListener(this::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(this::onFoodFinished);
        NeoForge.EVENT_BUS.addListener(this::onClone);
        NeoForge.EVENT_BUS.addListener(this::onLogin);
        NeoForge.EVENT_BUS.addListener(this::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            DietClientEvents.register(modBus);
        }
        DietApi.install(SERVICE);
    }

    public static DietState state(net.minecraft.world.entity.player.Player player) {
        return player.getData(STATE);
    }

    public static void setState(net.minecraft.world.entity.player.Player player, DietState state) {
        player.setData(STATE, state);
    }

    public static void recordCakeSlice(ServerPlayer player) {
        SERVICE.consume(player, new ItemStack(net.minecraft.world.item.Items.CAKE));
    }

    private void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(DIET_GROUPS, DietGroup.CODEC, DietGroup.CODEC);
        event.dataPackRegistry(DIET_EFFECT_RULES, DietEffectRule.CODEC, DietEffectRule.CODEC);
    }

    private void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(DietDataMaps.ITEM_DIET);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                DietStatePayload.TYPE,
                DietStatePayload.STREAM_CODEC,
                DietStatePayload::handle
        );
    }

    private void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SERVICE.tick(player);
        }
    }

    private void onFoodFinished(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack consumed = event.getItem().copyWithCount(1);
            SERVICE.consume(player, consumed);
        }
    }

    private void onClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        DietState copied = state(event.getOriginal());
        if (event.isWasDeath()) {
            copied = copied.penalized(DietConfig.DEATH_PENALTY.get(), DietConfig.DEATH_MINIMUM.get());
        }
        setState(player, copied);
        SERVICE.initializeGroups(player);
        SERVICE.sync(player);
    }

    private void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SERVICE.initializeGroups(player);
            SERVICE.sync(player);
        }
    }

    private void onDatapackSync(OnDatapackSyncEvent event) {
        event.getRelevantPlayers().forEach(player -> {
            SERVICE.initializeGroups(player);
            SERVICE.refreshEffects(player);
            SERVICE.sync(player);
        });
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        DietCommands.register(event.getDispatcher());
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != DietConfig.SPEC) {
            return;
        }
        net.minecraft.server.MinecraftServer server =
                net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(() -> server.getPlayerList().getPlayers().forEach(player -> {
                SERVICE.refreshEffects(player);
                SERVICE.sync(player);
            }));
        }
    }
}
