package com.protyvkultury.revivalages.feature.technology.primitive.view;

import java.util.List;

/**
 * Shared physical recipe-view layouts for the linear Revival Ages devices.
 *
 * <p>JEI and EMI consume these positions directly. The matching guide atlases use cyan for
 * item inputs, green for guaranteed results, purple for chance results, blue for fluid tanks,
 * yellow for the progress arrow, and red for a fire indicator.</p>
 */
public record PrimitiveRecipeLayout(
        String texture,
        int backgroundWidth,
        int backgroundHeight,
        List<Position> itemInputs,
        List<Position> itemOutputs,
        Position progressArrow,
        Position flame
) {
    public static final PrimitiveRecipeLayout CAMPFIRE = single("campfire", true);
    public static final PrimitiveRecipeLayout CHOPPING = single("chopping", false);
    public static final PrimitiveRecipeLayout PIT_KILN = chance("pit_kiln", true);
    public static final PrimitiveRecipeLayout PIT_BURN = chance("pit_burn", true);
    public static final PrimitiveRecipeLayout BARREL = new PrimitiveRecipeLayout(
            "barrel",
            160,
            64,
            List.of(new Position(16, 8), new Position(40, 8), new Position(16, 32), new Position(40, 32)),
            List.of(),
            new Position(88, 24),
            null
    );
    public static final PrimitiveRecipeLayout SOAKING_POT = new PrimitiveRecipeLayout(
            "soaking_pot",
            120,
            47,
            List.of(new Position(16, 5)),
            List.of(new Position(88, 16)),
            new Position(48, 16),
            null
    );
    public static final PrimitiveRecipeLayout TANNING_RACK = chance("tanning_rack", false);
    public static final PrimitiveRecipeLayout STONE_SAWMILL = new PrimitiveRecipeLayout(
            "stone_sawmill",
            112,
            55,
            List.of(new Position(16, 7), new Position(16, 32)),
            List.of(new Position(80, 7), new Position(80, 32)),
            new Position(44, 19),
            null
    );
    public static final PrimitiveRecipeLayout ANVIL = single("anvil", false);
    public static final PrimitiveRecipeLayout GRINDING = single("animal_power_grinding", false);
    public static final PrimitiveRecipeLayout PRESSING = new PrimitiveRecipeLayout(
            "animal_power_pressing",
            128,
            47,
            List.of(new Position(16, 16)),
            List.of(new Position(72, 16)),
            new Position(40, 16),
            null
    );
    public static final PrimitiveRecipeLayout DRYING = single("drying_rack", false);

    public PrimitiveRecipeLayout {
        itemInputs = List.copyOf(itemInputs);
        itemOutputs = List.copyOf(itemOutputs);
        if (backgroundWidth < 1 || backgroundHeight < 1) {
            throw new IllegalArgumentException("Recipe-view backgrounds must have positive dimensions");
        }
    }

    public boolean hasFlame() {
        return flame != null;
    }

    public int arrowSourceX() {
        return backgroundWidth;
    }

    public int arrowSourceY() {
        return 14;
    }

    public int flameSourceX() {
        return backgroundWidth;
    }

    public int flameSourceY() {
        return 0;
    }

    private static PrimitiveRecipeLayout single(String texture, boolean hasFlame) {
        return new PrimitiveRecipeLayout(
                texture,
                104,
                47,
                List.of(new Position(16, 10)),
                List.of(new Position(72, 10)),
                new Position(40, 10),
                hasFlame ? new Position(17, 33) : null
        );
    }

    private static PrimitiveRecipeLayout chance(String texture, boolean hasFlame) {
        return new PrimitiveRecipeLayout(
                texture,
                128,
                47,
                List.of(new Position(16, 10)),
                List.of(new Position(72, 10), new Position(96, 10)),
                new Position(40, 10),
                hasFlame ? new Position(17, 33) : null
        );
    }

    /** A top-left viewer coordinate expressed in GUI pixels. */
    public record Position(int x, int y) {
    }
}
