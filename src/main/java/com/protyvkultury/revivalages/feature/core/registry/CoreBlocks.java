package com.protyvkultury.revivalages.feature.core.registry;

import com.protyvkultury.revivalages.RevivalAges;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CoreBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RevivalAges.MOD_ID);

    private CoreBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
