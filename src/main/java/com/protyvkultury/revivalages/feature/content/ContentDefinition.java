package com.protyvkultury.revivalages.feature.content;

import java.util.Objects;

/** A permanently available content key owned by a feature. */
public record ContentDefinition(ContentKey key) {

    public ContentDefinition {
        Objects.requireNonNull(key, "key");
    }
}
