package com.protyvkultury.revivalages.feature.world.structuralintegrity;

public record SupportRange(int up, int down, int horizontal) {

    public SupportRange {
        if (up < 0 || down < 0 || horizontal < 0) {
            throw new IllegalArgumentException("Support ranges cannot be negative");
        }
    }

    public boolean contains(int horizontalX, int vertical, int horizontalZ) {
        return Math.abs(horizontalX) <= horizontal
                && -down <= vertical
                && vertical <= up
                && Math.abs(horizontalZ) <= horizontal;
    }
}
