package com.protyvkultury.revivalages.feature.worldgen.surfacedeposit.block;

import net.minecraft.util.StringRepresentable;

public enum RockVariation implements StringRepresentable {
    TINY("tiny"),
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large");

    private final String serializedName;

    RockVariation(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
