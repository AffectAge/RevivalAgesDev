package com.protyvkultury.revivalages.feature.technology.stonemachine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.protyvkultury.revivalages.core.client.render.InteractionPreviewRenderer;
import com.protyvkultury.revivalages.feature.technology.primitive.client.PrimitiveRenderHelper;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyClientConfig;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineKind;
import com.protyvkultury.revivalages.feature.technology.stonemachine.block.StoneMachineBlock;
import com.protyvkultury.revivalages.feature.technology.stonemachine.blockentity.StoneMachineBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.phys.BlockHitResult;

public final class StoneMachineRenderer implements BlockEntityRenderer<StoneMachineBlockEntity> {

    private final ItemRenderer items;

    public StoneMachineRenderer(BlockEntityRendererProvider.Context context) {
        items = context.getItemRenderer();
    }

    @Override
    public void render(
            StoneMachineBlockEntity machine,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay
    ) {
        if (machine.getBlockState().getValue(StoneMachineBlock.HALF) == DoubleBlockHalf.UPPER) {
            return;
        }
        Direction facing = machine.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        pose.pushPose();
        PrimitiveRenderHelper.rotateInteractionSpace(pose, facing);
        if (machine.kind() == StoneMachineKind.SAWMILL) {
            renderBlade(machine, pose, buffers, light, overlay);
        }
        if (machine.kind() == StoneMachineKind.CRUCIBLE) {
            renderFluid(machine, pose, buffers, light, overlay);
        }
        renderFuel(machine, pose, buffers, light, overlay);
        ItemStack display = machine.input().isEmpty() ? machine.firstOutput() : machine.input();
        if (!display.isEmpty()) {
            pose.pushPose();
            pose.translate(0.5D, 1.2D, 0.5D);
            pose.scale(0.5F, 0.5F, 0.5F);
            PrimitiveRenderHelper.renderItem(items, machine, display, pose, buffers, light, overlay, 0);
            if (PrimitiveTechnologyClientConfig.SHOW_PHYSICAL_ITEM_COUNTS.get()) {
                InteractionPreviewRenderer.renderCount(display, pose, buffers, light);
            }
            pose.popPose();
        }
        renderPreview(machine, pose, buffers, light, overlay);
        pose.popPose();
    }

    private void renderPreview(
            StoneMachineBlockEntity machine,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!PrimitiveTechnologyClientConfig.SHOW_INTERACTION_PREVIEWS.get()
                || minecraft.player == null
                || !(minecraft.hitResult instanceof BlockHitResult hit)) {
            return;
        }
        boolean upper = hit.getBlockPos().equals(machine.getBlockPos().above());
        boolean lower = hit.getBlockPos().equals(machine.getBlockPos());
        if (!upper && !lower) {
            return;
        }
        ItemStack held = minecraft.player.getMainHandItem();
        ItemStack preview = ItemStack.EMPTY;
        PreviewSlot previewSlot = upper ? PreviewSlot.INPUT : PreviewSlot.FUEL;
        if (machine.kind() == StoneMachineKind.SAWMILL
                && !held.isEmpty()
                && machine.canInsertBlade(held)) {
            preview = held;
            previewSlot = PreviewSlot.BLADE;
        } else if (upper) {
            if (!held.isEmpty() && machine.canInsertInput(held)) {
                preview = held;
            } else {
                ItemStack shown = machine.firstOutput().isEmpty()
                        ? machine.input()
                        : machine.firstOutput();
                if (!shown.isEmpty() && (held.isEmpty() || minecraft.player.isShiftKeyDown())) {
                    preview = shown;
                } else if (machine.kind() == StoneMachineKind.SAWMILL
                        && !machine.blade().isEmpty()
                        && (held.isEmpty() || minecraft.player.isShiftKeyDown())) {
                    preview = machine.blade();
                    previewSlot = PreviewSlot.BLADE;
                }
            }
        } else if (!held.isEmpty() && machine.canInsertFuel(held)) {
            preview = held;
        } else if (!machine.fuel().isEmpty() && (held.isEmpty() || minecraft.player.isShiftKeyDown())) {
            preview = machine.fuel();
        }
        if (preview.isEmpty()) {
            return;
        }
        pose.pushPose();
        transformPreview(previewSlot, pose);
        InteractionPreviewRenderer.renderItemPreview(
                items, machine, preview, previewSlot.seed, pose, buffers, light, overlay
        );
        pose.popPose();
    }

    private void renderBlade(
            StoneMachineBlockEntity machine,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay
    ) {
        if (machine.blade().isEmpty()) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5D, 1.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        if (machine.isLit()) {
            pose.mulPose(Axis.ZP.rotationDegrees((machine.getLevel().getGameTime() % 20L) * 18.0F));
        }
        pose.scale(0.75F, 0.75F, 0.75F);
        PrimitiveRenderHelper.renderItem(items, machine, machine.blade(), pose, buffers, light, overlay, 1);
        if (PrimitiveTechnologyClientConfig.SHOW_PHYSICAL_ITEM_COUNTS.get()) {
            InteractionPreviewRenderer.renderCount(machine.blade(), pose, buffers, light);
        }
        pose.popPose();
    }

    private void renderFuel(
            StoneMachineBlockEntity machine,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay
    ) {
        if (machine.fuel().isEmpty()) {
            return;
        }
        pose.pushPose();
        transformPreview(PreviewSlot.FUEL, pose);
        PrimitiveRenderHelper.renderItem(items, machine, machine.fuel(), pose, buffers, light, overlay, 2);
        if (PrimitiveTechnologyClientConfig.SHOW_PHYSICAL_ITEM_COUNTS.get()) {
            InteractionPreviewRenderer.renderCount(machine.fuel(), pose, buffers, light);
        }
        pose.popPose();
    }

    private static void transformPreview(PreviewSlot slot, PoseStack pose) {
        pose.translate(
                0.5D,
                slot == PreviewSlot.FUEL ? 0.2D : slot == PreviewSlot.BLADE ? 1.0D : 1.2D,
                0.5D
        );
        if (slot == PreviewSlot.BLADE) {
            pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            pose.scale(0.75F, 0.75F, 0.75F);
        } else {
            pose.scale(0.5F, 0.5F, 0.5F);
        }
    }

    private static void renderFluid(
            StoneMachineBlockEntity machine,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay
    ) {
        FluidStack fluid = machine.fluidTank().getFluid();
        if (fluid.isEmpty()) {
            return;
        }
        float ratio = fluid.getAmount() / (float) machine.fluidTank().getCapacity();
        float y = 14.0F / 16.0F + ratio * 9.0F / 16.0F;
        PrimitiveRenderHelper.renderFluidSurface(
                fluid,
                2.0F / 16.0F,
                14.0F / 16.0F,
                y,
                2.0F / 16.0F,
                14.0F / 16.0F,
                pose, buffers, light, overlay);
    }

    private enum PreviewSlot {
        INPUT(0),
        BLADE(1),
        FUEL(2);

        private final int seed;

        PreviewSlot(int seed) {
            this.seed = seed;
        }
    }
}
