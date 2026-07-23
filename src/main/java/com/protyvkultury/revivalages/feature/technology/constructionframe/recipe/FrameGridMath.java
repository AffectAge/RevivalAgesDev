package com.protyvkultury.revivalages.feature.technology.constructionframe.recipe;

/** Pure index and rotation rules for the 3x3x3 frame grid. */
public final class FrameGridMath {

    public static final int CELL_COUNT = 27;

    private FrameGridMath() {
    }

    public static int index(int x, int y, int z) {
        requireCoordinate(x);
        requireCoordinate(y);
        requireCoordinate(z);
        return y * 9 + z * 3 + x;
    }

    public static int[] position(int index) {
        if (index < 0 || index >= CELL_COUNT) {
            throw new IllegalArgumentException("Frame index must be within 0..26");
        }
        int y = index / 9;
        int remainder = index % 9;
        return new int[] {remainder % 3, y, remainder / 3};
    }

    public static int rotatedIndex(int x, int y, int z, int quarterTurns) {
        requireCoordinate(x);
        requireCoordinate(y);
        requireCoordinate(z);
        int rotatedX = x;
        int rotatedZ = z;
        for (int turn = 0; turn < Math.floorMod(quarterTurns, 4); turn++) {
            int previousX = rotatedX;
            rotatedX = 2 - rotatedZ;
            rotatedZ = previousX;
        }
        return index(rotatedX, y, rotatedZ);
    }

    private static void requireCoordinate(int coordinate) {
        if (coordinate < 0 || coordinate >= 3) {
            throw new IllegalArgumentException("Frame coordinate must be within 0..2");
        }
    }
}
