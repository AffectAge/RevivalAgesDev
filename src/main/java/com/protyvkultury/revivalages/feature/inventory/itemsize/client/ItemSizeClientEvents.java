package com.protyvkultury.revivalages.feature.inventory.itemsize.client;

import com.protyvkultury.revivalages.feature.inventory.itemsize.ItemSizeSettings;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ItemSizeClientEvents {

    private ItemSizeClientEvents() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ItemSizeClientEvents::onLoggingOut);
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ItemSizeSettings.clearRemote();
    }
}
