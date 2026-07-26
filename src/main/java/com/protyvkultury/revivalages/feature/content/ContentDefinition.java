package com.protyvkultury.revivalages.feature.content;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/** A content key and the feature-owned configuration value which controls it. */
public record ContentDefinition(ContentKey key, BooleanSupplier configuredEnabled) {

    public ContentDefinition {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(configuredEnabled, "configuredEnabled");
    }
}
