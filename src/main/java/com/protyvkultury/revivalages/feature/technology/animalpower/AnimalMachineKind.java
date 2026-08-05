package com.protyvkultury.revivalages.feature.technology.animalpower;

import com.protyvkultury.revivalages.feature.content.ContentKey;

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

    /** Matches the vertical path plane used by the designated Horse Power machine. */
    public int workerPathYOffset() {
        return this == GRINDSTONE ? -1 : 0;
    }

    public ContentKey contentKey() {
        return switch (this) {
            case GRINDSTONE -> ContentKey.HORSE_GRINDSTONE;
            case CHOPPING_BLOCK -> ContentKey.HORSE_CHOPPING_BLOCK;
            case PRESS -> ContentKey.HORSE_PRESS;
        };
    }
}
