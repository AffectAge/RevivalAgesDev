package com.protyvkultury.revivalages.feature.technology.stonemachine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.protyvkultury.revivalages.feature.technology.primitive.client.PrimitiveRenderHelper;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineKind;
import com.protyvkultury.revivalages.feature.technology.stonemachine.block.StoneMachineBlock;
import com.protyvkultury.revivalages.feature.technology.stonemachine.blockentity.StoneMachineBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.fluids.FluidStack;

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
        ItemStack display = machine.input().isEmpty() ? machine.firstOutput() : machine.input();
        if (!display.isEmpty()) {
            pose.pushPose();
            pose.translate(0.5D, 1.2D, 0.5D);
            pose.scale(0.5F, 0.5F, 0.5F);
            PrimitiveRenderHelper.renderItem(items, machine, display, pose, buffers, light, overlay, 0);
            pose.popPose();
        }
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
        pose.popPose();
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
        float y = 1.05F + ratio * 0.35F;
        PrimitiveRenderHelper.renderFluidSurface(fluid, 0.16F, 0.84F, y, 0.16F, 0.84F,
                pose, buffers, light, overlay);
    }
}
