package com.protyvkultury.revivalages.feature.technology.animalpower;

public enum AnimalMachineKind {
    GRINDSTONE(false),
    CHOPPING_BLOCK(true),
    PRESS(true);

    private final boolean tall;

    AnimalMachineKind(boolean tall) {
        this.tall = tall;
    }

    public boolean tall() {
        return tall;
    }
}
