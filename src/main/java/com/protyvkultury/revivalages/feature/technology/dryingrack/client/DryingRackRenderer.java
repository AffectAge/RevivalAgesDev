package com.protyvkultury.revivalages.feature.technology.dryingrack.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.protyvkultury.revivalages.feature.technology.dryingrack.block.CrudeDryingRackBlock;
import com.protyvkultury.revivalages.feature.technology.dryingrack.blockentity.DryingRackBlockEntity;
import com.protyvkultury.revivalages.feature.technology.primitive.client.PrimitiveRenderHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class DryingRackRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {

    private final ItemRenderer itemRenderer;

    public DryingRackRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            DryingRackBlockEntity rack,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        if (rack.getSlotCount() == 1) {
            renderCrude(rack, poseStack, bufferSource, packedLight, packedOverlay);
        } else {
            renderNormal(rack, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private void renderNormal(
            DryingRackBlockEntity rack,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        Direction facing = rack.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        poseStack.pushPose();
        PrimitiveRenderHelper.rotateInteractionSpace(poseStack, facing);
        for (int slot = 0; slot < rack.getSlotCount(); slot++) {
            ItemStack stack = rack.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            double x = (slot & 1) == 0 ? 0.3125D : 0.6875D;
            double z = (slot & 2) == 0 ? 0.3125D : 0.6875D;
            poseStack.pushPose();
            poseStack.translate(x, 0.78125D, z);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.scale(0.25F, 0.25F, 0.25F);
            renderStack(rack, stack, slot, poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void renderCrude(
            DryingRackBlockEntity rack,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        ItemStack stack = rack.getItem(0);
        if (stack.isEmpty()) {
            return;
        }
        BlockState state = rack.getBlockState();
        Direction facing = state.getValue(CrudeDryingRackBlock.FACING);
        poseStack.pushPose();
        PrimitiveRenderHelper.rotateInteractionSpace(poseStack, facing);
        poseStack.translate(0.5D, 0.5D, 0.15D);
        poseStack.scale(0.75F, 0.75F, 0.75F);
        renderStack(rack, stack, 0, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private void renderStack(
            DryingRackBlockEntity rack,
            ItemStack stack,
            int seed,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.NONE,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                rack.getLevel(),
                rack.getBlockPos().hashCode() + seed
        );
    }

}
