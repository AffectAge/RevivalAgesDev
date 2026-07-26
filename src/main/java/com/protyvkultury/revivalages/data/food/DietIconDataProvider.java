package com.protyvkultury.revivalages.data.food;

import com.google.common.hash.Hashing;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class DietIconDataProvider implements DataProvider {

    private final Path output;

    public DietIconDataProvider(PackOutput output) {
        this.output = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve("revivalages/textures/mob_effect/diet_toughness.png");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        try {
            byte[] png = createIcon();
            cache.writeIfNeeded(output, png, Hashing.sha256().hashBytes(png));
            return CompletableFuture.completedFuture(null);
        } catch (IOException exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    @Override
    public String getName() {
        return "Revival Ages Diet Toughness Icon";
    }

    private static byte[] createIcon() throws IOException {
        BufferedImage image = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
        int outline = 0xFF243626;
        int green = 0xFF68B96B;
        int light = 0xFFA7DB79;
        int grain = 0xFFE6C66D;
        for (int y = 3; y <= 13; y++) {
            int halfWidth = Math.max(2, 7 - Math.abs(8 - y) / 2);
            for (int x = 9 - halfWidth; x <= 8 + halfWidth; x++) {
                boolean edge = x == 9 - halfWidth || x == 8 + halfWidth || y == 3 || y == 13;
                image.setRGB(x, y, edge ? outline : green);
            }
        }
        for (int x = 6; x <= 11; x++) {
            image.setRGB(x, 7, light);
            image.setRGB(x, 10, grain);
        }
        image.setRGB(8, 5, 0xFFFFFFFF);
        image.setRGB(9, 5, 0xFFFFFFFF);
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", bytes);
            return bytes.toByteArray();
        }
    }
}
