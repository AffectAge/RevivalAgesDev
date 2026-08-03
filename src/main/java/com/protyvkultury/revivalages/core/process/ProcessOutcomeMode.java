package com.protyvkultury.revivalages.core.process;

/** Describes when a viewer-only chance outcome is rolled by the canonical recipe logic. */
public enum ProcessOutcomeMode {
    ADDITIONAL("additional"),
    PER_INPUT("per_input"),
    PER_STAGE("per_stage"),
    PER_ATTEMPT("per_attempt"),
    PER_ITEM("per_item");

    private final String serializedName;

    ProcessOutcomeMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }
}
