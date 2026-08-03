package com.protyvkultury.revivalages.core.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.ClientHooks;

/** Shared rendering contract for physical slots and their interaction previews. */
public final class InteractionPreviewRenderer {

    private static final float PREVIEW_ALPHA = 0.55F;

    private InteractionPreviewRenderer() {
    }

    public static void renderItemPreview(
            ItemRenderer renderer,
            BlockEntity blockEntity,
            ItemStack stack,
            int seed,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay
    ) {
        if (stack.isEmpty()) {
            return;
        }
        BakedModel model = renderer.getModel(
                stack,
                blockEntity.getLevel(),
                Minecraft.getInstance().player,
                blockEntity.getBlockPos().hashCode() + seed
        );
        if (model.isCustomRenderer()) {
            return;
        }
        poseStack.pushPose();
        model = ClientHooks.handleCameraTransforms(
                poseStack,
                model,
                ItemDisplayContext.NONE,
                false
        );
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        for (BakedModel pass : model.getRenderPasses(stack, true)) {
            VertexConsumer consumer = new AlphaVertexConsumer(
                    buffers.getBuffer(RenderType.itemEntityTranslucentCull(InventoryMenu.BLOCK_ATLAS)),
                    PREVIEW_ALPHA
            );
            renderer.renderModelLists(pass, stack, light, overlay, poseStack, consumer);
        }
        poseStack.popPose();
    }

    public static void renderCount(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light
    ) {
        renderCount(stack, poseStack, buffers, light, 0.55D, 0.55D, 0.02D);
    }

    /** Renders a physical-stack count at an explicit position in the caller's interaction space. */
    public static void renderCount(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            double x,
            double y,
            double z
    ) {
        renderCount(stack, poseStack, buffers, light, x, y, z, 0.04F);
    }

    /** Renders a physical-stack count with an explicit position and text scale. */
    public static void renderCount(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            double x,
            double y,
            double z,
            float scale
    ) {
        renderCount(stack, poseStack, buffers, light, x, y, z, scale, false);
    }

    /** Renders a physical-stack count centered horizontally on its explicit anchor. */
    public static void renderCenteredCount(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            double x,
            double y,
            double z,
            float scale
    ) {
        renderCount(stack, poseStack, buffers, light, x, y, z, scale, true);
    }

    private static void renderCount(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            double x,
            double y,
            double z,
            float scale,
            boolean centered
    ) {
        if (stack.getCount() <= 1) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        String text = Integer.toString(stack.getCount());
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scale, -scale, scale);
        font.drawInBatch(
                text,
                centered ? -font.width(text) / 2.0F : -font.width(text),
                0.0F,
                0xFFFFFFFF,
                true,
                poseStack.last().pose(),
                buffers,
                Font.DisplayMode.NORMAL,
                0,
                light
        );
        poseStack.popPose();
    }

    private record AlphaVertexConsumer(VertexConsumer delegate, float alpha) implements VertexConsumer {

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(red, green, blue, Math.round(alpha * this.alpha));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            delegate.setNormal(normalX, normalY, normalZ);
            return this;
        }
    }
}
