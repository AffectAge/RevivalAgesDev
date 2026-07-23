package com.protyvkultury.revivalages.feature.technology.constructionframe.client;

import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameFeature;
import com.protyvkultury.revivalages.feature.technology.constructionframe.block.ConstructionFrameBlock;
import com.protyvkultury.revivalages.feature.technology.constructionframe.recipe.FrameGridPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ConstructionFrameClientEvents {

    private ConstructionFrameClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ConstructionFrameClientEvents::registerRenderers);
        NeoForge.EVENT_BUS.addListener(ConstructionFrameClientEvents::renderCellHighlight);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ConstructionFrameFeature.BLOCK_ENTITY.get(),
                ConstructionFrameRenderer::new
        );
    }

    private static void renderCellHighlight(RenderHighlightEvent.Block event) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockHitResult hit = event.getTarget();
        if (minecraft.level == null
                || minecraft.player == null
                || !minecraft.level.getBlockState(hit.getBlockPos()).is(ConstructionFrameFeature.CONSTRUCTION_FRAME)) {
            return;
        }
        boolean extracting = minecraft.player.getMainHandItem().isEmpty();
        FrameGridPosition cell = ConstructionFrameBlock.selectedCell(hit, extracting);
        if (cell == null) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        double x = hit.getBlockPos().getX() - camera.x + cell.x() / 3.0D;
        double y = hit.getBlockPos().getY() - camera.y + cell.y() / 3.0D;
        double z = hit.getBlockPos().getZ() - camera.z + cell.z() / 3.0D;
        double epsilon = 0.002D;
        LevelRenderer.renderLineBox(
                event.getPoseStack(),
                event.getMultiBufferSource().getBuffer(RenderType.lines()),
                x - epsilon,
                y - epsilon,
                z - epsilon,
                x + 1.0D / 3.0D + epsilon,
                y + 1.0D / 3.0D + epsilon,
                z + 1.0D / 3.0D + epsilon,
                0.1F,
                1.0F,
                0.1F,
                1.0F
        );
        event.setCanceled(true);
    }
}
