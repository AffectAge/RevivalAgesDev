package com.protyvkultury.revivalages.feature.technology.constructionframe.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.protyvkultury.revivalages.feature.technology.constructionframe.blockentity.ConstructionFrameBlockEntity;
import com.protyvkultury.revivalages.feature.technology.constructionframe.recipe.FrameGridPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Renders the synchronized grid directly, without a mutable virtual Level. */
public final class ConstructionFrameRenderer implements BlockEntityRenderer<ConstructionFrameBlockEntity> {

    private final ItemRenderer itemRenderer;

    public ConstructionFrameRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            ConstructionFrameBlockEntity frame,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay
    ) {
        pose.pushPose();
        pose.scale(1.0F / 3.0F, 1.0F / 3.0F, 1.0F / 3.0F);
        for (int slot = 0; slot < 27; slot++) {
            ItemStack stack = frame.item(slot);
            if (stack.isEmpty()) {
                continue;
            }
            FrameGridPosition cell = FrameGridPosition.fromIndex(slot);
            pose.pushPose();
            pose.translate(cell.x(), cell.y(), cell.z());
            if (stack.getItem() instanceof BlockItem) {
                BlockState state = frame.displayState(slot);
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                        state,
                        pose,
                        buffers,
                        light,
                        overlay,
                        ModelData.EMPTY,
                        null
                );
            } else {
                pose.translate(0.5D, 0.5D, 0.5D);
                float angle = frame.getLevel() == null
                        ? 0.0F
                        : (frame.getLevel().getGameTime() + partialTick) % 360.0F;
                pose.mulPose(Axis.YP.rotationDegrees(angle));
                pose.scale(0.65F, 0.65F, 0.65F);
                itemRenderer.renderStatic(
                        stack,
                        ItemDisplayContext.FIXED,
                        light,
                        overlay,
                        pose,
                        buffers,
                        frame.getLevel(),
                        frame.getBlockPos().hashCode() + slot
                );
            }
            pose.popPose();
        }
        pose.popPose();
    }
}
