package com.protyvkultury.revivalages.feature.technology.ignition;

import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;

public final class WoodTorchSettings {

    private static volatile Snapshot client = new Snapshot(true, true);

    private WoodTorchSettings() {
    }

    public static Snapshot serverSnapshot() {
        return new Snapshot(
                PrimitiveTechnologyConfig.WOOD_TORCH_BURNS_UP.get(),
                PrimitiveTechnologyConfig.WOOD_TORCH_RAIN_EXTINGUISHES.get()
        );
    }

    public static Snapshot clientSnapshot() {
        return client;
    }

    public static void acceptRemote(Snapshot snapshot) {
        client = snapshot;
    }

    public record Snapshot(boolean burnsUp, boolean rainExtinguishes) {
    }
}
