package com.protyvkultury.revivalages.feature.technology.dryingrack.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.protyvkultury.revivalages.config.InteractionOutlineConfig;
import com.protyvkultury.revivalages.core.client.render.InteractionPreviewRenderer;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.technology.dryingrack.block.AbstractDryingRackBlock;
import com.protyvkultury.revivalages.feature.technology.dryingrack.block.CrudeDryingRackBlock;
import com.protyvkultury.revivalages.feature.technology.dryingrack.blockentity.DryingRackBlockEntity;
import com.protyvkultury.revivalages.feature.technology.dryingrack.config.DryingRackClientConfig;
import com.protyvkultury.revivalages.feature.technology.primitive.client.PrimitiveRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

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
            if (DryingRackClientConfig.SHOW_ITEM_COUNTS.get()) {
                InteractionPreviewRenderer.renderCount(stack, poseStack, bufferSource, packedLight);
            }
            poseStack.popPose();
        }
        renderInteractionFeedback(rack, poseStack, bufferSource, packedLight, packedOverlay);
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
        BlockState state = rack.getBlockState();
        Direction facing = state.getValue(CrudeDryingRackBlock.FACING);
        poseStack.pushPose();
        PrimitiveRenderHelper.rotateInteractionSpace(poseStack, facing);
        if (!stack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.5D, 0.85D);
            poseStack.scale(0.75F, 0.75F, 0.75F);
            renderStack(rack, stack, 0, poseStack, bufferSource, packedLight, packedOverlay);
            if (DryingRackClientConfig.SHOW_ITEM_COUNTS.get()) {
                InteractionPreviewRenderer.renderCount(stack, poseStack, bufferSource, packedLight);
            }
            poseStack.popPose();
        }
        renderInteractionFeedback(rack, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private void renderInteractionFeedback(
            DryingRackBlockEntity rack,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ContentAvailability.isEnabled(rack.contentKey())
                || minecraft.player == null
                || minecraft.hitResult == null
                || minecraft.hitResult.getType() != HitResult.Type.BLOCK
                || !(minecraft.hitResult instanceof BlockHitResult hit)
                || !hit.getBlockPos().equals(rack.getBlockPos())
                || !(rack.getBlockState().getBlock() instanceof AbstractDryingRackBlock block)
                || !block.allowsInteractionFace(rack.getBlockState(), hit.getDirection())) {
            return;
        }
        int slot = block.interactionSlot(rack.getBlockState(), hit);
        AABB bounds = rack.getSlotCount() == 1
                ? new AABB(0.1D, 0.1D, 0.81D, 0.9D, 0.9D, 0.89D)
                : normalSlotBounds(slot);
        if (DryingRackClientConfig.SHOW_INTERACTION_BOUNDS.get()) {
            int color = InteractionOutlineConfig.rgb();
            LevelRenderer.renderLineBox(
                    poseStack,
                    bufferSource.getBuffer(RenderType.lines()),
                    bounds.inflate(0.002D),
                    ((color >> 16) & 0xFF) / 255.0F,
                    ((color >> 8) & 0xFF) / 255.0F,
                    (color & 0xFF) / 255.0F,
                    0.9F);
        }
        ItemStack held = minecraft.player.getMainHandItem();
        ItemStack stored = rack.getItem(slot);
        ItemStack preview = ItemStack.EMPTY;
        if (!held.isEmpty() && rack.canInsert(slot)) {
            preview = held;
        } else if (!stored.isEmpty() && (held.isEmpty() || minecraft.player.isShiftKeyDown())) {
            preview = stored;
        }
        if (!DryingRackClientConfig.SHOW_ITEM_PREVIEW.get() || preview.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        if (rack.getSlotCount() == 1) {
            poseStack.translate(0.5D, 0.5D, 0.85D);
            poseStack.scale(0.75F, 0.75F, 0.75F);
        } else {
            double x = (slot & 1) == 0 ? 0.3125D : 0.6875D;
            double z = (slot & 2) == 0 ? 0.3125D : 0.6875D;
            poseStack.translate(x, 0.78125D, z);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.scale(0.25F, 0.25F, 0.25F);
        }
        InteractionPreviewRenderer.renderItemPreview(
                itemRenderer,
                rack,
                preview,
                slot,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }

    private static AABB normalSlotBounds(int slot) {
        double x = (slot & 1) == 0 ? 0.3125D : 0.6875D;
        double z = (slot & 2) == 0 ? 0.3125D : 0.6875D;
        return new AABB(
                x - 0.16D,
                0.75D,
                z - 0.16D,
                x + 0.16D,
                0.8125D,
                z + 0.16D);
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
