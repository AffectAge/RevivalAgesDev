package com.protyvkultury.revivalages.core.process;

/** Built-in reusable process-rule types. */
public enum ProcessRuleType {
    LIT_BLOCK_BELOW("lit_block_below", ProcessRuleKind.GATE, ProcessRulePolicy.PAUSE),
    OPEN_SKY("open_sky", ProcessRuleKind.GATE, ProcessRulePolicy.RESET_PROGRESS),
    WEATHER_EXPOSURE("weather_exposure", ProcessRuleKind.HAZARD, ProcessRulePolicy.PAUSE),
    DRYING_ENVIRONMENT("drying_environment", ProcessRuleKind.MODIFIER, ProcessRulePolicy.PAUSE),
    SEALED_MACHINE("sealed_machine", ProcessRuleKind.GATE, ProcessRulePolicy.PAUSE),
    INSTALLED_TOOL("installed_tool", ProcessRuleKind.GATE, ProcessRulePolicy.PAUSE),
    FUELLED_AND_LIT("fuelled_and_lit", ProcessRuleKind.GATE, ProcessRulePolicy.PAUSE),
    ATTACHED_WORKER("attached_worker", ProcessRuleKind.GATE, ProcessRulePolicy.PAUSE),
    VALID_WORK_AREA("valid_work_area", ProcessRuleKind.GATE, ProcessRulePolicy.PAUSE),
    VALID_STRUCTURE("valid_structure", ProcessRuleKind.GATE, ProcessRulePolicy.PAUSE),
    REQUIRED_MANUAL_TOOL("required_manual_tool", ProcessRuleKind.GATE, ProcessRulePolicy.PAUSE),
    RANDOM_OUTCOME("random_outcome", ProcessRuleKind.HAZARD, ProcessRulePolicy.PAUSE);

    private final String serializedName;
    private final ProcessRuleKind kind;
    private final ProcessRulePolicy defaultPolicy;

    ProcessRuleType(String serializedName, ProcessRuleKind kind, ProcessRulePolicy defaultPolicy) {
        this.serializedName = serializedName;
        this.kind = kind;
        this.defaultPolicy = defaultPolicy;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static ProcessRuleType bySerializedName(String value) {
        for (ProcessRuleType type : values()) {
            if (type.serializedName.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown process rule type: " + value);
    }

    public ProcessRuleKind kind() {
        return kind;
    }

    public ProcessRulePolicy defaultPolicy() {
        return defaultPolicy;
    }
}
