package com.protyvkultury.revivalages.feature.technology.stonemachine.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

final class StoneMachineRecipeLayoutTest {

    @Test
    void ovenAndCrucibleUseCenteredThreePartFlows() {
        assertCenteredThreePartFlow(StoneMachineRecipeLayout.OVEN);
        assertCenteredThreePartFlow(StoneMachineRecipeLayout.CRUCIBLE);
    }

    @Test
    void kilnExtendsTheSameSpacingToItsPossibleOutput() {
        StoneMachineRecipeLayout layout = StoneMachineRecipeLayout.KILN;

        assertFlowSpacing(layout);
        StoneMachineRecipeLayout.Position secondaryOutput = layout.secondaryOutput();
        assertNotNull(secondaryOutput);
        assertEquals(
                8,
                secondaryOutput.x() - (layout.output().x() + 16)
        );
        assertEquals(layout.output().y(), secondaryOutput.y());
        assertEquals(
                layout.input().x(),
                layout.backgroundWidth() - (secondaryOutput.x() + 16)
        );
    }

    @Test
    void guideAtlasesMatchTheSharedRecipeLayout() {
        assertAtlasBounds("stone_oven", StoneMachineRecipeLayout.OVEN);
        assertAtlasBounds("stone_kiln", StoneMachineRecipeLayout.KILN);
        assertAtlasBounds("stone_crucible", StoneMachineRecipeLayout.CRUCIBLE);
    }

    private static void assertCenteredThreePartFlow(StoneMachineRecipeLayout layout) {
        assertFlowSpacing(layout);
        assertEquals(
                layout.input().x(),
                layout.backgroundWidth() - (layout.output().x() + 16)
        );
    }

    private static void assertFlowSpacing(StoneMachineRecipeLayout layout) {
        assertEquals(8, layout.progressArrow().x() - (layout.input().x() + 16));
        assertEquals(8, layout.output().x() - (layout.progressArrow().x() + 24));
        assertEquals(layout.input().y(), layout.progressArrow().y());
        assertEquals(layout.input().y(), layout.output().y());
        assertEquals(layout.input().x() + 8, layout.flame().x() + 7);
        assertEquals(layout.input().y() + 23, layout.flame().y());
        assertTrue(layout.flame().y() + 14 <= layout.backgroundHeight());
    }

    private static void assertAtlasBounds(String name, StoneMachineRecipeLayout layout) {
        BufferedImage image = readImage("assets/revivalages/textures/gui/" + name + ".png");
        assertTrue(image.getWidth() >= layout.backgroundWidth() + 24);
        assertTrue(image.getHeight() >= layout.backgroundHeight());
    }

    private static BufferedImage readImage(String path) {
        try (InputStream input = StoneMachineRecipeLayoutTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, path);
            return ImageIO.read(input);
        } catch (IOException exception) {
            throw new AssertionError("Unable to read " + path, exception);
        }
    }
}
