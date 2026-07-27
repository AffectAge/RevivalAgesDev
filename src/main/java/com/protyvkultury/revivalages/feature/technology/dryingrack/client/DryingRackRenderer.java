package com.protyvkultury.revivalages.feature.technology.dryingrack.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.ClientHooks;

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
                ? new AABB(0.0D, 11.0D / 16.0D, 11.0D / 16.0D, 1.0D, 1.0D, 1.0D)
                : normalSlotBounds(slot);
        if (DryingRackClientConfig.SHOW_INTERACTION_BOUNDS.get()) {
            LevelRenderer.renderLineBox(
                    poseStack,
                    bufferSource.getBuffer(RenderType.lines()),
                    bounds.inflate(0.002D),
                    0.1F,
                    1.0F,
                    0.1F,
                    0.9F);
        }
        ItemStack held = minecraft.player.getMainHandItem();
        if (!DryingRackClientConfig.SHOW_ITEM_PREVIEW.get()
                || held.isEmpty()
                || !rack.canInsert(slot)) {
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
        renderPreview(rack, held, slot, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static AABB normalSlotBounds(int slot) {
        double minX = (slot & 1) == 0 ? 0.0D : 0.5D;
        double minZ = (slot & 2) == 0 ? 0.0D : 0.5D;
        return new AABB(
                minX,
                11.0D / 16.0D,
                minZ,
                minX + 0.5D,
                12.0D / 16.0D,
                minZ + 0.5D);
    }

    private void renderPreview(
            DryingRackBlockEntity rack,
            ItemStack stack,
            int seed,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        BakedModel model = itemRenderer.getModel(
                stack,
                rack.getLevel(),
                Minecraft.getInstance().player,
                rack.getBlockPos().hashCode() + seed);
        if (model.isCustomRenderer()) {
            return;
        }
        poseStack.pushPose();
        model = ClientHooks.handleCameraTransforms(
                poseStack,
                model,
                ItemDisplayContext.NONE,
                false);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        for (BakedModel pass : model.getRenderPasses(stack, true)) {
            VertexConsumer consumer = new AlphaVertexConsumer(
                    bufferSource.getBuffer(RenderType.itemEntityTranslucentCull(InventoryMenu.BLOCK_ATLAS)),
                    0.2F);
            itemRenderer.renderModelLists(
                    pass,
                    stack,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    consumer);
        }
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
