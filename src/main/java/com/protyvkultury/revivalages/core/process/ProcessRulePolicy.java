package com.protyvkultury.revivalages.core.process;

/** Defines the progress behaviour while a gate rule is not satisfied. */
public enum ProcessRulePolicy {
    PAUSE("pause"),
    RESET_PROGRESS("reset_progress");

    private final String serializedName;

    ProcessRulePolicy(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static ProcessRulePolicy bySerializedName(String value) {
        for (ProcessRulePolicy policy : values()) {
            if (policy.serializedName.equals(value)) {
                return policy;
            }
        }
        throw new IllegalArgumentException("Unknown process rule policy: " + value);
    }
}
