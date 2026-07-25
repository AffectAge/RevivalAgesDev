package com.protyvkultury.revivalages.feature.technology.knapping.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;

public record KnappingPattern(int width, int height, int cells, boolean defaultOn) {

    public static final Codec<KnappingPattern> CODEC = Codec.STRING.listOf().comapFlatMap(
            KnappingPattern::decode,
            KnappingPattern::encode
    );

    public KnappingPattern {
        if (width < 1 || width > 5 || height < 1 || height > 5) {
            throw new IllegalArgumentException("Knapping patterns must be between 1x1 and 5x5");
        }
    }

    public boolean matches(int grid) {
        return KnappingPatternMatcher.matches(grid, width, height, cells, defaultOn);
    }

    public boolean on(int x, int y) {
        return (cells & (1 << (y * width + x))) != 0;
    }

    private static DataResult<KnappingPattern> decode(List<String> rows) {
        if (rows.isEmpty() || rows.size() > 5) {
            return DataResult.error(() -> "Knapping pattern must contain one to five rows");
        }
        int width = rows.getFirst().length();
        if (width < 1 || width > 5 || rows.stream().anyMatch(row -> row.length() != width)) {
            return DataResult.error(() -> "Knapping pattern rows must have one consistent width between one and five");
        }
        int cells = 0;
        for (int y = 0; y < rows.size(); y++) {
            String row = rows.get(y);
            for (int x = 0; x < width; x++) {
                char value = row.charAt(x);
                if (value != ' ') {
                    cells |= 1 << (y * width + x);
                }
            }
        }
        return DataResult.success(new KnappingPattern(width, rows.size(), cells, true));
    }

    private static List<String> encode(KnappingPattern pattern) {
        java.util.ArrayList<String> rows = new java.util.ArrayList<>(pattern.height);
        for (int y = 0; y < pattern.height; y++) {
            StringBuilder row = new StringBuilder(pattern.width);
            for (int x = 0; x < pattern.width; x++) {
                row.append(pattern.on(x, y) ? 'X' : ' ');
            }
            rows.add(row.toString());
        }
        return List.copyOf(rows);
    }

    public KnappingPattern withDefaultOn(boolean value) {
        return new KnappingPattern(width, height, cells, value);
    }
}
