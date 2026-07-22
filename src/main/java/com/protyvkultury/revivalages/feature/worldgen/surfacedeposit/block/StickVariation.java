package com.protyvkultury.revivalages.feature.worldgen.surfacedeposit.block;

import net.minecraft.util.StringRepresentable;

public enum StickVariation implements StringRepresentable {
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large");

    private final String serializedName;

    StickVariation(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
