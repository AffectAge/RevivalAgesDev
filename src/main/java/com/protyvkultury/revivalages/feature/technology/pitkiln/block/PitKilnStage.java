package com.protyvkultury.revivalages.feature.technology.pitkiln.block;

import net.minecraft.util.StringRepresentable;

public enum PitKilnStage implements StringRepresentable {
    EMPTY("empty"),
    THATCH("thatch"),
    WOOD("wood"),
    ACTIVE("active"),
    COMPLETE("complete");

    private final String serializedName;

    PitKilnStage(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
