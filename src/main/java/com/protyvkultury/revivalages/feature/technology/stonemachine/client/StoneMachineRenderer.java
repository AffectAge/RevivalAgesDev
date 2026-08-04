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

    private static final double FUEL_ITEM_Y = 0.25D;
    private static final double BLADE_Y = 1.25D;
    private static final float BLADE_SCALE = 0.95F;
    private static final double FUEL_COUNT_X = 0.5D;
    private static final double FUEL_COUNT_Y = 0.95D;
    private static final double FUEL_COUNT_Z = 0.95D;
    private static final float FUEL_COUNT_SCALE = 0.01F;
    private static final double PROCESS_COUNT_X = 0.5D;
    private static final double PROCESS_COUNT_Y = 1.45D;
    private static final double PROCESS_COUNT_Z = 0.91D;
    private static final float PROCESS_COUNT_SCALE = 0.01F;
    private static final double STANDARD_INPUT_Y = 1.2D;
    private static final double CRUCIBLE_INPUT_Y = 23.01D / 16.0D;
    private static final float STANDARD_INPUT_SCALE = 0.5F;
    private static final float CRUCIBLE_INPUT_SCALE = 0.30F;
    private static final float CRUCIBLE_FLUID_MIN_X = 5.0F / 16.0F;
    private static final float CRUCIBLE_FLUID_MAX_X = 11.0F / 16.0F;
    private static final float CRUCIBLE_FLUID_MIN_Z = 4.0F / 16.0F;
    private static final float CRUCIBLE_FLUID_MAX_Z = 12.0F / 16.0F;
    private static final float CRUCIBLE_FLUID_MIN_Y = 20.01F / 16.0F;
    private static final float CRUCIBLE_FLUID_MAX_Y = 23.99F / 16.0F;

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
            transformInput(machine, pose);
            PrimitiveRenderHelper.renderItem(items, machine, display, pose, buffers, light, overlay, 0);
            pose.popPose();
            if (PrimitiveTechnologyClientConfig.SHOW_PHYSICAL_ITEM_COUNTS.get()) {
                InteractionPreviewRenderer.renderCenteredCount(
                        display,
                        pose,
                        buffers,
                        light,
                        PROCESS_COUNT_X,
                        PROCESS_COUNT_Y,
                        PROCESS_COUNT_Z,
                        PROCESS_COUNT_SCALE
                );
            }
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
        transformPreview(machine, previewSlot, pose);
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
        pose.translate(0.5D, BLADE_Y, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        if (machine.isLit()) {
            pose.mulPose(Axis.ZP.rotationDegrees((machine.getLevel().getGameTime() % 20L) * 18.0F));
        }
        pose.scale(BLADE_SCALE, BLADE_SCALE, BLADE_SCALE);
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
        transformPreview(machine, PreviewSlot.FUEL, pose);
        PrimitiveRenderHelper.renderItem(items, machine, machine.fuel(), pose, buffers, light, overlay, 2);
        pose.popPose();
        if (PrimitiveTechnologyClientConfig.SHOW_PHYSICAL_ITEM_COUNTS.get()) {
            InteractionPreviewRenderer.renderCenteredCount(
                    machine.fuel(),
                    pose,
                    buffers,
                    light,
                    FUEL_COUNT_X,
                    FUEL_COUNT_Y,
                    FUEL_COUNT_Z,
                    FUEL_COUNT_SCALE
            );
        }
    }

    private static void transformInput(StoneMachineBlockEntity machine, PoseStack pose) {
        boolean crucible = machine.kind() == StoneMachineKind.CRUCIBLE;
        pose.translate(0.5D, crucible ? CRUCIBLE_INPUT_Y : STANDARD_INPUT_Y, 0.5D);
        float scale = crucible ? CRUCIBLE_INPUT_SCALE : STANDARD_INPUT_SCALE;
        pose.scale(scale, scale, scale);
    }

    private static void transformPreview(
            StoneMachineBlockEntity machine,
            PreviewSlot slot,
            PoseStack pose
    ) {
        if (slot == PreviewSlot.INPUT) {
            transformInput(machine, pose);
            return;
        }
        pose.translate(
                0.5D,
                slot == PreviewSlot.FUEL ? FUEL_ITEM_Y : BLADE_Y,
                0.5D
        );
        if (slot == PreviewSlot.BLADE) {
            pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            pose.scale(BLADE_SCALE, BLADE_SCALE, BLADE_SCALE);
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
        float ratio = Math.clamp(
                fluid.getAmount() / (float) machine.fluidTank().getCapacity(),
                0.0F,
                1.0F
        );
        float y = CRUCIBLE_FLUID_MIN_Y + ratio * (CRUCIBLE_FLUID_MAX_Y - CRUCIBLE_FLUID_MIN_Y);
        PrimitiveRenderHelper.renderFluidSurface(
                fluid,
                CRUCIBLE_FLUID_MIN_X,
                CRUCIBLE_FLUID_MAX_X,
                y,
                CRUCIBLE_FLUID_MIN_Z,
                CRUCIBLE_FLUID_MAX_Z,
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
