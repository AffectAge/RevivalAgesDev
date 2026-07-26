package com.protyvkultury.revivalages.feature.technology.stonemachine;

import com.protyvkultury.revivalages.feature.content.ContentKey;

public enum StoneMachineKind {
    SAWMILL,
    OVEN,
    KILN,
    CRUCIBLE;

    public ContentKey contentKey() {
        return switch (this) {
            case SAWMILL -> ContentKey.STONE_SAWMILL;
            case OVEN -> ContentKey.STONE_OVEN;
            case KILN -> ContentKey.STONE_KILN;
            case CRUCIBLE -> ContentKey.STONE_CRUCIBLE;
        };
    }
}
