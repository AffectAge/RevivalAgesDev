package com.protyvkultury.revivalages.feature.technology.animalpower.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.protyvkultury.revivalages.feature.technology.animalpower.blockentity.HandGrindstoneBlockEntity;
import com.protyvkultury.revivalages.feature.technology.primitive.client.PrimitiveRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;

public final class AnimalPowerRenderers {

    private AnimalPowerRenderers() {
    }

    public static final class HandGrindstone implements BlockEntityRenderer<HandGrindstoneBlockEntity> {

        private final ItemRenderer items;

        public HandGrindstone(BlockEntityRendererProvider.Context context) {
            items = context.getItemRenderer();
        }

        @Override
        public void render(
                HandGrindstoneBlockEntity grindstone,
                float partialTick,
                PoseStack pose,
                MultiBufferSource buffers,
                int light,
                int overlay
        ) {
            pose.pushPose();
            pose.translate(0.5D, 0.5D, 0.5D);
            pose.mulPose(Axis.YP.rotationDegrees(grindstone.rotation(partialTick) * 360.0F));
            pose.translate(-0.5D, -0.5D, -0.5D);
            renderPart("hand_grindstone_rotor", grindstone, pose, buffers, light, overlay);
            pose.popPose();

            ItemStack display = grindstone.item(0).isEmpty() ? grindstone.item(1) : grindstone.item(0);
            if (display.isEmpty()) {
                return;
            }
            pose.pushPose();
            pose.translate(0.5D, 0.88D, 0.5D);
            pose.mulPose(Axis.YP.rotationDegrees(grindstone.rotation(partialTick) * 360.0F));
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            pose.scale(0.45F, 0.45F, 0.45F);
            PrimitiveRenderHelper.renderItem(items, grindstone, display, pose, buffers, light, overlay, 0);
            pose.popPose();
        }
    }

    private static void renderPart(
            String modelName,
            net.minecraft.world.level.block.entity.BlockEntity blockEntity,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay
    ) {
        BakedModel model = Minecraft.getInstance()
                .getModelManager()
                .getModel(AnimalPowerClientEvents.standaloneModel(modelName));
        Minecraft.getInstance()
                .getBlockRenderer()
                .getModelRenderer()
                .renderModel(
                        pose.last(),
                        buffers.getBuffer(Sheets.cutoutBlockSheet()),
                        blockEntity.getBlockState(),
                        model,
                        1.0F,
                        1.0F,
                        1.0F,
                        light,
                        overlay,
                        ModelData.EMPTY,
                        Sheets.cutoutBlockSheet()
                );
    }
}
