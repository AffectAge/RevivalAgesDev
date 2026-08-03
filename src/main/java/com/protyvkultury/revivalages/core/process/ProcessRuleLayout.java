package com.protyvkultury.revivalages.core.process;

/** Pure layout math shared by recipe viewers; each row is centered independently. */
public record ProcessRuleLayout(int availableWidth, int columns, int rows, int ruleCount) {

    public static final int STEP = 18;

    public static ProcessRuleLayout of(int availableWidth, int ruleCount) {
        int columns = Math.max(1, (availableWidth + 2) / STEP);
        int normalizedRuleCount = Math.max(0, ruleCount);
        return new ProcessRuleLayout(availableWidth, columns, Math.ceilDiv(normalizedRuleCount, columns), normalizedRuleCount);
    }

    public int x(int index) {
        int row = index / columns;
        int countInRow = row == rows - 1 ? ruleCount - row * columns : columns;
        return Math.max(0, (availableWidth - countInRow * STEP + 2) / 2) + (index % columns) * STEP;
    }

    public int y(int baseY, int index) {
        return baseY + (index / columns) * STEP;
    }

    public int height() {
        return rows * STEP;
    }
}
