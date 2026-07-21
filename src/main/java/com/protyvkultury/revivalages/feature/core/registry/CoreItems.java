package com.protyvkultury.revivalages.feature.core.registry;

import com.protyvkultury.revivalages.RevivalAges;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CoreItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RevivalAges.MOD_ID);

    private CoreItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
