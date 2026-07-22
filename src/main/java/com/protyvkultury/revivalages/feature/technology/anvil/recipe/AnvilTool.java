package com.protyvkultury.revivalages.feature.technology.anvil.recipe;

import net.minecraft.util.StringRepresentable;

public enum AnvilTool implements StringRepresentable {
    HAMMER("hammer"),
    PICKAXE("pickaxe");

    private final String name;

    AnvilTool(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
