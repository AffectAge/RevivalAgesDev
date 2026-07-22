package com.protyvkultury.revivalages.feature.technology.ignition.block;

import net.minecraft.util.StringRepresentable;

public enum WoodTorchState implements StringRepresentable {
    UNLIT("unlit"),
    LIT("lit"),
    DOUSED("doused");

    private final String name;

    WoodTorchState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
