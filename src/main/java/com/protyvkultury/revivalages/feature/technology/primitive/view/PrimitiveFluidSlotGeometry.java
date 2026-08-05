package com.protyvkultury.revivalages.feature.technology.primitive.view;

/**
 * Texture-defined inner bounds for a primitive recipe-view fluid fill.
 *
 * <p>JEI renders directly into these bounds. EMI's {@code TankWidget} reserves one pixel on
 * every side, so its adapter must use {@link #emiTankBounds()} instead. Keeping the visual
 * bounds here prevents the two optional viewers from drifting apart.</p>
 */
public record PrimitiveFluidSlotGeometry(int contentX, int contentY, int contentWidth, int contentHeight) {

    public static final int SIZE = 16;
    public static final PrimitiveFluidSlotGeometry BARREL_INPUT =
            new PrimitiveFluidSlotGeometry(65, 21, SIZE, SIZE);
    public static final PrimitiveFluidSlotGeometry BARREL_OUTPUT =
            new PrimitiveFluidSlotGeometry(121, 21, SIZE, SIZE);
    public static final PrimitiveFluidSlotGeometry SOAKING_POT_INPUT =
            new PrimitiveFluidSlotGeometry(17, 30, SIZE, SIZE);
    public static final PrimitiveFluidSlotGeometry STONE_CRUCIBLE_OUTPUT =
            new PrimitiveFluidSlotGeometry(73, 11, SIZE, SIZE);
    public static final PrimitiveFluidSlotGeometry PRESSING_OUTPUT =
            new PrimitiveFluidSlotGeometry(97, 16, SIZE, SIZE);

    public PrimitiveFluidSlotGeometry {
        if (contentX < 1 || contentY < 1) {
            throw new IllegalArgumentException("Fluid content bounds must allow EMI's one-pixel inset");
        }
        if (contentWidth != SIZE || contentHeight != SIZE) {
            throw new IllegalArgumentException("Recipe-view fluid fills must be 16 by 16 pixels");
        }
    }

    public EmiTankBounds emiTankBounds() {
        return new EmiTankBounds(contentX - 1, contentY - 1, contentWidth + 2, contentHeight + 2);
    }

    /** Outer bounds passed to EMI's one-pixel-inset {@code TankWidget}. */
    public record EmiTankBounds(int x, int y, int width, int height) {
    }
}
