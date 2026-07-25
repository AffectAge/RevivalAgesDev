package com.protyvkultury.revivalages.feature.technology.knapping.recipe;

public final class KnappingPatternMatcher {

    private KnappingPatternMatcher() {
    }

    public static boolean matches(
            int grid,
            int width,
            int height,
            int patternCells,
            boolean defaultOn
    ) {
        for (int xOffset = 0; xOffset <= 5 - width; xOffset++) {
            for (int yOffset = 0; yOffset <= 5 - height; yOffset++) {
                if (matchesAt(grid, width, height, patternCells, defaultOn, xOffset, yOffset, false)
                        || matchesAt(grid, width, height, patternCells, defaultOn, xOffset, yOffset, true)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchesAt(
            int grid,
            int width,
            int height,
            int patternCells,
            boolean defaultOn,
            int xOffset,
            int yOffset,
            boolean mirrored
    ) {
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                boolean expected = defaultOn;
                int localX = x - xOffset;
                int localY = y - yOffset;
                if (localX >= 0 && localX < width && localY >= 0 && localY < height) {
                    int patternX = mirrored ? width - localX - 1 : localX;
                    expected = (patternCells & (1 << (localY * width + patternX))) != 0;
                }
                boolean actual = (grid & (1 << (y * 5 + x))) != 0;
                if (actual != expected) {
                    return false;
                }
            }
        }
        return true;
    }
}
