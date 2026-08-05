package com.protyvkultury.revivalages.feature.technology.knapping;

/**
 * Shared coordinates for the item-backed Knapping menu and its client screen.
 *
 * <p>The menu slots and the screen background must use these values together;
 * otherwise slots can overlap the 5x5 material grid or be drawn outside the
 * background texture.</p>
 */
public final class KnappingLayout {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 186;
    public static final int CELL_SIZE = 16;
    public static final int GRID_X = 12;
    public static final int GRID_Y = 12;
    public static final int OUTPUT_X = 128;
    public static final int OUTPUT_Y = 46;
    public static final int PLAYER_INVENTORY_Y = 104;
    public static final int HOTBAR_Y = 162;
    public static final int PLAYER_INVENTORY_OFFSET_Y = 20;

    private KnappingLayout() {
    }
}
