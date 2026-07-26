package com.protyvkultury.revivalages.feature.inventory.itemsize;

import com.protyvkultury.revivalages.api.size.ItemSizeDataMaps;
import com.protyvkultury.revivalages.api.size.Size;
import com.protyvkultury.revivalages.api.size.SizeApi;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.content.ContentPolicy;
import com.protyvkultury.revivalages.feature.inventory.itemsize.client.ItemSizeClientEvents;
import com.protyvkultury.revivalages.feature.inventory.itemsize.network.ItemSizeSettingsPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class ItemSizeFeature implements FeatureModule {

    @Override
    public ContentPolicy contentPolicy() {
        return ContentPolicy.gameplay("item_size")
                .define(ContentKey.ITEM_SIZE, ItemSizeConfig::configuredEnabled)
                .build();
    }

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(this::registerDataMaps);
        modBus.addListener(this::registerPayloads);
        modBus.addListener(this::onConfigLoading);
        modBus.addListener(this::onConfigReloading);
        modContainer.registerConfig(
                ModConfig.Type.SERVER,
                ItemSizeConfig.SPEC,
                "revivalages-item-size-server.toml"
        );
        NeoForge.EVENT_BUS.addListener(this::onTooltip);
        NeoForge.EVENT_BUS.addListener(this::onItemStacked);
        NeoForge.EVENT_BUS.addListener(this::onDatapackSync);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ItemSizeClientEvents.register();
        }
    }

    private void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(ItemSizeDataMaps.ITEM_SIZE);
        event.register(ItemSizeDataMaps.ITEM_CONTAINER);
        event.register(ItemSizeDataMaps.BLOCK_CONTAINER);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                ItemSizeSettingsPayload.TYPE,
                ItemSizeSettingsPayload.STREAM_CODEC,
                ItemSizeSettingsPayload::handle
        );
    }

    private void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !ItemSizeSettings.enabled()) {
            return;
        }
        Size size = SizeApi.getSize(stack);
        event.getToolTip().add(Component.translatable(
                "tooltip.revivalages.item_size",
                Component.translatable("size.revivalages." + size.getSerializedName())
        ).withStyle(ChatFormatting.GRAY));
    }

    private void onItemStacked(ItemStackedOnOtherEvent event) {
        if (!ItemSizeSettings.enabled()) {
            return;
        }
        ItemStack carried = event.getCarriedItem();
        ItemStack stackedOn = event.getStackedOnItem();
        if (stackedOn.is(Items.BUNDLE) && !carried.isEmpty() && !SizeApi.canInsert(stackedOn, carried)) {
            event.setCanceled(true);
            return;
        }
        if (carried.is(Items.BUNDLE)
                && event.getClickAction() == ClickAction.SECONDARY
                && !stackedOn.isEmpty()
                && !SizeApi.canInsert(carried, stackedOn)) {
            event.setCanceled(true);
        }
    }

    private void onDatapackSync(OnDatapackSyncEvent event) {
        ItemSizeSettings.Snapshot serverSnapshot = ItemSizeSettings.refreshLocal();
        ItemSizeSettingsPayload payload = new ItemSizeSettingsPayload(serverSnapshot);
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }

    private void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ItemSizeConfig.SPEC) {
            ItemSizeSettings.refreshLocal();
        }
    }

    private void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != ItemSizeConfig.SPEC) {
            return;
        }
        ItemSizeSettings.Snapshot serverSnapshot = ItemSizeSettings.refreshLocal();
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            PacketDistributor.sendToAllPlayers(new ItemSizeSettingsPayload(serverSnapshot));
        }
    }
}
