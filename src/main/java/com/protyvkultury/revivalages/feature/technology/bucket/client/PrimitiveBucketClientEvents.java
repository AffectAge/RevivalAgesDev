package com.protyvkultury.revivalages.feature.technology.bucket.client;

import com.protyvkultury.revivalages.feature.technology.bucket.PrimitiveBucketFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;

public final class PrimitiveBucketClientEvents {

    private PrimitiveBucketClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(PrimitiveBucketClientEvents::registerItemColors);
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new DynamicFluidContainerModel.Colors(),
                PrimitiveBucketFeature.WOODEN_BUCKET.get(), PrimitiveBucketFeature.CLAY_BUCKET.get());
    }
}
