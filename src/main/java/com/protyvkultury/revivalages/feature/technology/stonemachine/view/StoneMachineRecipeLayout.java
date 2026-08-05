package com.protyvkultury.revivalages.feature.technology.stonemachine.view;

/**
 * Shared physical layout for stone-machine recipe viewers.
 *
 * <p>JEI and EMI consume these positions directly so their input, progress, output, and fire
 * indicators stay aligned with the same hand-authored backgrounds.</p>
 */
public record StoneMachineRecipeLayout(
        int backgroundWidth,
        int backgroundHeight,
        Position input,
        Position progressArrow,
        Position output,
        Position secondaryOutput,
        Position flame
) {
    public static final StoneMachineRecipeLayout OVEN = new StoneMachineRecipeLayout(
            104,
            47,
            new Position(16, 10),
            new Position(40, 10),
            new Position(72, 10),
            null,
            new Position(17, 33)
    );
    public static final StoneMachineRecipeLayout KILN = new StoneMachineRecipeLayout(
            128,
            47,
            new Position(16, 10),
            new Position(40, 10),
            new Position(72, 10),
            new Position(96, 10),
            new Position(17, 33)
    );
    public static final StoneMachineRecipeLayout CRUCIBLE = new StoneMachineRecipeLayout(
            104,
            47,
            new Position(16, 10),
            new Position(40, 10),
            new Position(72, 10),
            null,
            new Position(17, 33)
    );

    public boolean hasSecondaryOutput() {
        return secondaryOutput != null;
    }

    /** A top-left viewer coordinate expressed in GUI pixels. */
    public record Position(int x, int y) {
    }
}
