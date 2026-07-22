package com.protyvkultury.revivalages.feature.technology.anvil.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.protyvkultury.revivalages.feature.technology.anvil.blockentity.AnvilBlockEntity;
import com.protyvkultury.revivalages.feature.technology.primitive.client.PrimitiveRenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;

public final class AnvilRenderer implements BlockEntityRenderer<AnvilBlockEntity> {

    private final ItemRenderer items;

    public AnvilRenderer(BlockEntityRendererProvider.Context context) {
        items = context.getItemRenderer();
    }

    @Override
    public void render(
            AnvilBlockEntity anvil,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay
    ) {
        if (anvil.input().isEmpty()) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5D, 0.42D, 0.5D);
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.scale(0.65F, 0.65F, 0.65F);
        PrimitiveRenderHelper.renderItem(items, anvil, anvil.input(), pose, buffers, light, overlay, 0);
        pose.popPose();
    }
}
