package com.protyvkultury.revivalages.feature.technology.animalpower.recipe;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum GrindingMachine implements StringRepresentable {
    HAND("hand"),
    ANIMAL("animal");

    public static final Codec<GrindingMachine> CODEC = StringRepresentable.fromEnum(GrindingMachine::values);

    private final String serializedName;

    GrindingMachine(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
