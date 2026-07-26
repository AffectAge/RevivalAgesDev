package com.protyvkultury.revivalages.api.diet;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum DietDetector implements StringRepresentable {
    ANY("any"),
    AVERAGE("average"),
    ALL("all"),
    CUMULATIVE("cumulative");

    public static final Codec<DietDetector> CODEC = StringRepresentable.fromEnum(DietDetector::values);

    private final String serializedName;

    DietDetector(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
