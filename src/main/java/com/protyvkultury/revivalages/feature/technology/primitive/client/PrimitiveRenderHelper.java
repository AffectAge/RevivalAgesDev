package com.protyvkultury.revivalages.feature.technology.primitive.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public final class PrimitiveRenderHelper {

    private PrimitiveRenderHelper() {
    }

    /** Applies the horizontal interaction-space transform around the block center. */
    public static void rotateInteractionSpace(PoseStack poseStack, Direction facing) {
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(-0.5D, 0.0D, -0.5D);
    }

    public static void renderItem(
            ItemRenderer renderer,
            BlockEntity blockEntity,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay,
            int seed
    ) {
        if (stack.isEmpty()) {
            return;
        }
        renderer.renderStatic(
                stack,
                // The designated reference applies the machine transform directly
                // and renders the baked item model with TransformType.NONE. Using FIXED
                // here applies a second item-display transform, shrinking logs and turning
                // flat hides almost edge-on.
                ItemDisplayContext.NONE,
                light,
                overlay,
                poseStack,
                buffers,
                blockEntity.getLevel(),
                blockEntity.getBlockPos().hashCode() + seed
        );
    }

    public static void renderFluidSurface(
            FluidStack stack,
            float minX,
            float maxX,
            float y,
            float minZ,
            float maxZ,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay
    ) {
        if (stack.isEmpty()) {
            return;
        }
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(stack.getFluidType());
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(extensions.getStillTexture(stack));
        int color = extensions.getTintColor(stack);
        int alpha = color >>> 24;
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        VertexConsumer vertex = buffers.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
        var pose = poseStack.last().pose();
        vertex.addVertex(pose, minX, y, minZ).setColor(red, green, blue, alpha).setUv(sprite.getU0(), sprite.getV0()).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        vertex.addVertex(pose, minX, y, maxZ).setColor(red, green, blue, alpha).setUv(sprite.getU0(), sprite.getV1()).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        vertex.addVertex(pose, maxX, y, maxZ).setColor(red, green, blue, alpha).setUv(sprite.getU1(), sprite.getV1()).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        vertex.addVertex(pose, maxX, y, minZ).setColor(red, green, blue, alpha).setUv(sprite.getU1(), sprite.getV0()).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }
}
