package com.protyvkultury.revivalages.feature.technology.primitive.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

final class PrimitiveRecipeLayoutTest {

    private static final int INPUT_COLOR = 0xFF26BDE2;
    private static final int OUTPUT_COLOR = 0xFF57D26F;
    private static final int CHANCE_COLOR = 0xFFAD6DE5;
    private static final int FLUID_COLOR = 0xFF39A9F0;
    private static final int ARROW_COLOR = 0xFFF3C623;
    private static final int FLAME_COLOR = 0xFFEF5350;

    @Test
    void standardFlowsUseEqualGapsAndMargins() {
        for (PrimitiveRecipeLayout layout : List.of(
                PrimitiveRecipeLayout.CAMPFIRE,
                PrimitiveRecipeLayout.CHOPPING,
                PrimitiveRecipeLayout.PIT_KILN,
                PrimitiveRecipeLayout.PIT_BURN,
                PrimitiveRecipeLayout.TANNING_RACK,
                PrimitiveRecipeLayout.ANVIL,
                PrimitiveRecipeLayout.GRINDING,
                PrimitiveRecipeLayout.DRYING)) {
            PrimitiveRecipeLayout.Position input = layout.itemInputs().getFirst();
            PrimitiveRecipeLayout.Position output = layout.itemOutputs().getFirst();
            assertEquals(8, layout.progressArrow().x() - (input.x() + 16));
            assertEquals(8, output.x() - (layout.progressArrow().x() + 24));
            assertEquals(input.y(), layout.progressArrow().y());
            assertEquals(input.y(), output.y());
        }
    }

    @Test
    void guideAtlasesCoverEverySharedLayout() {
        assertGuideAtlas(PrimitiveRecipeLayout.CAMPFIRE);
        assertGuideAtlas(PrimitiveRecipeLayout.CHOPPING);
        assertGuideAtlas(PrimitiveRecipeLayout.PIT_KILN);
        assertGuideAtlas(PrimitiveRecipeLayout.PIT_BURN);
        assertGuideAtlas(PrimitiveRecipeLayout.BARREL);
        assertGuideAtlas(PrimitiveRecipeLayout.SOAKING_POT);
        assertGuideAtlas(PrimitiveRecipeLayout.TANNING_RACK);
        assertGuideAtlas(PrimitiveRecipeLayout.STONE_SAWMILL);
        assertGuideAtlas(PrimitiveRecipeLayout.ANVIL);
        assertGuideAtlas(PrimitiveRecipeLayout.GRINDING);
        assertGuideAtlas(PrimitiveRecipeLayout.PRESSING);
        assertGuideAtlas(PrimitiveRecipeLayout.DRYING);
    }

    @Test
    void fluidGuidesMatchTheSharedTankBounds() {
        assertFluidGuide("barrel", PrimitiveFluidSlotGeometry.BARREL_INPUT);
        assertFluidGuide("barrel", PrimitiveFluidSlotGeometry.BARREL_OUTPUT);
        assertFluidGuide("soaking_pot", PrimitiveFluidSlotGeometry.SOAKING_POT_INPUT);
        assertFluidGuide("animal_power_pressing", PrimitiveFluidSlotGeometry.PRESSING_OUTPUT);
    }

    private static void assertGuideAtlas(PrimitiveRecipeLayout layout) {
        BufferedImage image = readImage("assets/revivalages/textures/gui/" + layout.texture() + ".png");
        for (PrimitiveRecipeLayout.Position input : layout.itemInputs()) {
            assertEquals(INPUT_COLOR, image.getRGB(input.x(), input.y()));
        }
        for (int index = 0; index < layout.itemOutputs().size(); index++) {
            PrimitiveRecipeLayout.Position output = layout.itemOutputs().get(index);
            assertEquals(index == 0 ? OUTPUT_COLOR : CHANCE_COLOR, image.getRGB(output.x(), output.y()));
        }
        assertEquals(ARROW_COLOR, image.getRGB(layout.progressArrow().x(), layout.progressArrow().y()));
        assertEquals(ARROW_COLOR, image.getRGB(layout.arrowSourceX(), layout.arrowSourceY()));
        if (layout.hasFlame()) {
            assertEquals(FLAME_COLOR, image.getRGB(layout.flame().x(), layout.flame().y()));
            assertEquals(FLAME_COLOR, image.getRGB(layout.flameSourceX(), layout.flameSourceY()));
        }
        assertTrue(image.getWidth() > layout.arrowSourceX() + 23);
        assertTrue(image.getHeight() > Math.max(layout.backgroundHeight(), layout.arrowSourceY() + 16));
    }

    private static void assertFluidGuide(String atlas, PrimitiveFluidSlotGeometry geometry) {
        BufferedImage image = readImage("assets/revivalages/textures/gui/" + atlas + ".png");
        PrimitiveFluidSlotGeometry.EmiTankBounds bounds = geometry.emiTankBounds();
        assertEquals(FLUID_COLOR, image.getRGB(bounds.x(), bounds.y()));
    }

    private static BufferedImage readImage(String path) {
        try (InputStream input = PrimitiveRecipeLayoutTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, path);
            return ImageIO.read(input);
        } catch (IOException exception) {
            throw new AssertionError("Unable to read " + path, exception);
        }
    }
}
