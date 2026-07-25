package com.protyvkultury.revivalages.feature.technology.knapping.client;

import com.protyvkultury.revivalages.feature.technology.knapping.KnappingFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class KnappingClientEvents {

    private KnappingClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(KnappingClientEvents::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(KnappingFeature.MENU.get(), KnappingScreen::new);
    }
}
