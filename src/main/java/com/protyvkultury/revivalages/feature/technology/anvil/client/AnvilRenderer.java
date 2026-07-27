package com.protyvkultury.revivalages.feature.technology.anvil.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.protyvkultury.revivalages.core.client.render.InteractionPreviewRenderer;
import com.protyvkultury.revivalages.feature.technology.anvil.blockentity.AnvilBlockEntity;
import com.protyvkultury.revivalages.feature.technology.primitive.client.PrimitiveRenderHelper;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

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
        ItemStack shown = anvil.input();
        if (!shown.isEmpty()) {
            renderAtSlot(anvil, shown, false, pose, buffers, light, overlay);
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (!PrimitiveTechnologyClientConfig.SHOW_INTERACTION_PREVIEWS.get()
                || minecraft.player == null
                || !(minecraft.hitResult instanceof BlockHitResult hit)
                || !hit.getBlockPos().equals(anvil.getBlockPos())) {
            return;
        }
        ItemStack held = minecraft.player.getMainHandItem();
        ItemStack preview = !held.isEmpty() && anvil.canInsert(held)
                ? held
                : !shown.isEmpty() && (held.isEmpty() || minecraft.player.isShiftKeyDown())
                        ? shown
                        : ItemStack.EMPTY;
        if (!preview.isEmpty()) {
            renderAtSlot(anvil, preview, true, pose, buffers, light, overlay);
        }
    }

    private void renderAtSlot(
            AnvilBlockEntity anvil,
            ItemStack stack,
            boolean preview,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay
    ) {
        pose.pushPose();
        pose.translate(0.5D, 0.42D, 0.5D);
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.scale(0.65F, 0.65F, 0.65F);
        if (preview) {
            InteractionPreviewRenderer.renderItemPreview(
                    items, anvil, stack, 0, pose, buffers, light, overlay
            );
        } else {
            PrimitiveRenderHelper.renderItem(items, anvil, stack, pose, buffers, light, overlay, 0);
            if (PrimitiveTechnologyClientConfig.SHOW_PHYSICAL_ITEM_COUNTS.get()) {
                InteractionPreviewRenderer.renderCount(stack, pose, buffers, light);
            }
        }
        pose.popPose();
    }
}
