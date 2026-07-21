package com.protyvkultury.revivalages.feature.technology.primitive.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.protyvkultury.revivalages.feature.technology.barrel.block.BarrelBlock;
import com.protyvkultury.revivalages.feature.technology.barrel.blockentity.BarrelBlockEntity;
import com.protyvkultury.revivalages.feature.technology.campfire.blockentity.CampfireBlockEntity;
import com.protyvkultury.revivalages.feature.technology.choppingblock.blockentity.ChoppingBlockEntity;
import com.protyvkultury.revivalages.feature.technology.pitkiln.block.PitKilnBlock;
import com.protyvkultury.revivalages.feature.technology.pitkiln.block.PitKilnStage;
import com.protyvkultury.revivalages.feature.technology.pitkiln.blockentity.PitKilnBlockEntity;
import com.protyvkultury.revivalages.feature.technology.soakingpot.blockentity.SoakingPotBlockEntity;
import com.protyvkultury.revivalages.feature.technology.tanningrack.blockentity.TanningRackBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public final class PrimitiveDeviceRenderers {

    private PrimitiveDeviceRenderers() {
    }

    public static final class Campfire implements BlockEntityRenderer<CampfireBlockEntity> {

        private final ItemRenderer items;

        public Campfire(BlockEntityRendererProvider.Context context) {
            items = context.getItemRenderer();
        }

        @Override
        public void render(CampfireBlockEntity campfire, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            for (int index = 0; index < 8; index++) {
                ItemStack log = campfire.logStack(index);
                if (log.isEmpty()) {
                    continue;
                }
                pose.pushPose();
                if (index < 4) {
                    pose.translate(0.5D, 0.2D, 0.5D);
                    pose.mulPose(Axis.YP.rotationDegrees(90.0F * index));
                    pose.translate(0.375D, 0.0D, 0.0D);
                    pose.mulPose(Axis.ZP.rotationDegrees(67.5F));
                } else {
                    pose.translate(0.5D, 0.125D, 0.5D);
                    pose.mulPose(Axis.YP.rotationDegrees(90.0F * (index % 4) + 45.0F));
                    pose.translate(0.4375D, 0.0D, 0.0D);
                    pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
                }
                pose.scale(0.25F, 0.5F, 0.25F);
                PrimitiveRenderHelper.renderItem(items, campfire, log, pose, buffers, light, overlay, index);
                pose.popPose();
            }
            if (!campfire.cookingStack().isEmpty()) {
                pose.pushPose();
                pose.translate(0.5D, 0.5D, 0.5D);
                pose.scale(0.75F, 0.75F, 0.75F);
                PrimitiveRenderHelper.renderItem(items, campfire, campfire.cookingStack(), pose, buffers, light, overlay, 20);
                pose.popPose();
            }
        }
    }

    public static final class Chopping implements BlockEntityRenderer<ChoppingBlockEntity> {

        private final ItemRenderer items;

        public Chopping(BlockEntityRendererProvider.Context context) {
            items = context.getItemRenderer();
        }

        @Override
        public void render(ChoppingBlockEntity chopping, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            if (!chopping.input().isEmpty()) {
                pose.pushPose();
                pose.translate(0.5D, 0.75D, 0.5D);
                pose.scale(0.75F, 0.75F, 0.75F);
                PrimitiveRenderHelper.renderItem(items, chopping, chopping.input(), pose, buffers, light, overlay, 0);
                pose.popPose();
            }
            if (chopping.sawdust() > 0) {
                pose.pushPose();
                pose.translate(0.72D, 0.77D, 0.72D);
                pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                pose.scale(0.25F + chopping.sawdust() * 0.025F, 0.25F, 0.25F);
                PrimitiveRenderHelper.renderItem(items, chopping,
                        new ItemStack(com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature.WOOD_CHIPS.get()),
                        pose, buffers, light, overlay, 1);
                pose.popPose();
            }
        }
    }

    public static final class PitKiln implements BlockEntityRenderer<PitKilnBlockEntity> {

        private final ItemRenderer items;

        public PitKiln(BlockEntityRendererProvider.Context context) {
            items = context.getItemRenderer();
        }

        @Override
        public void render(PitKilnBlockEntity kiln, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            PitKilnStage stage = kiln.getBlockState().getValue(PitKilnBlock.STAGE);
            ItemStack shown = stage == PitKilnStage.COMPLETE ? kiln.displayOutput() : kiln.input();
            if (!shown.isEmpty() && stage == PitKilnStage.EMPTY) {
                pose.pushPose();
                pose.translate(0.5D, 0.35D, 0.5D);
                pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                pose.scale(0.5F, 0.5F, 0.5F);
                PrimitiveRenderHelper.renderItem(items, kiln, shown, pose, buffers, light, overlay, 0);
                pose.popPose();
            }
            for (int index = 0; index < 3; index++) {
                ItemStack log = kiln.logStack(index);
                if (log.isEmpty()) {
                    continue;
                }
                pose.pushPose();
                // Pyrotech parity: three adjacent one-third-width logs spanning the pit.
                double oneThird = 1.0D / 3.0D;
                double oneSixth = 1.0D / 6.0D;
                pose.translate(index * oneThird + oneSixth, 2.0D * oneThird + oneSixth, 0.5D);
                pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                pose.scale((float) oneThird, 1.0F, (float) oneThird);
                PrimitiveRenderHelper.renderItem(items, kiln, log, pose, buffers, light, overlay, index + 1);
                pose.popPose();
            }
        }
    }

    public static final class Barrel implements BlockEntityRenderer<BarrelBlockEntity> {

        private final ItemRenderer items;

        public Barrel(BlockEntityRendererProvider.Context context) {
            items = context.getItemRenderer();
        }

        @Override
        public void render(BarrelBlockEntity barrel, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            if (barrel.getBlockState().getValue(BarrelBlock.SEALED)) {
                return;
            }
            float ratio = barrel.fluidTank().getFluidAmount() / (float) Math.max(1, barrel.fluidTank().getCapacity());
            PrimitiveRenderHelper.renderFluidSurface(
                    barrel.fluidTank().getFluid(), 0.16F, 0.84F, 0.2F + ratio * 0.63F, 0.16F, 0.84F,
                    pose, buffers, light, overlay
            );
            ItemStack[] stacks = barrel.itemsForView();
            for (int slot = 0; slot < stacks.length; slot++) {
                if (stacks[slot].isEmpty()) {
                    continue;
                }
                pose.pushPose();
                pose.translate((slot & 1) == 0 ? 0.3125D : 0.6875D,
                        14.0D / 16.0D,
                        (slot & 2) == 0 ? 0.3125D : 0.6875D);
                pose.scale(3.0F / 16.0F, 3.0F / 16.0F, 3.0F / 16.0F);
                PrimitiveRenderHelper.renderItem(items, barrel, stacks[slot], pose, buffers, light, overlay, slot);
                pose.popPose();
            }
        }
    }

    public static final class SoakingPot implements BlockEntityRenderer<SoakingPotBlockEntity> {

        private final ItemRenderer items;

        public SoakingPot(BlockEntityRendererProvider.Context context) {
            items = context.getItemRenderer();
        }

        @Override
        public void render(SoakingPotBlockEntity pot, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            float ratio = pot.fluidTank().getFluidAmount() / (float) Math.max(1, pot.fluidTank().getCapacity());
            float y = 0.16F + ratio * 0.31F;
            PrimitiveRenderHelper.renderFluidSurface(pot.fluidTank().getFluid(), 0.19F, 0.81F, y, 0.19F, 0.81F, pose, buffers, light, overlay);
            ItemStack shown = pot.output().isEmpty() ? pot.input() : pot.output();
            if (!shown.isEmpty()) {
                pose.pushPose();
                boolean campfire = pot.getLevel() != null
                        && pot.getLevel().getBlockState(pot.getBlockPos().below())
                        .is(com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature.CAMPFIRE.get());
                pose.translate(0.5D, campfire ? 3.0D / 16.0D : 0.5D, 0.5D);
                pose.scale(6.0F / 16.0F, 6.0F / 16.0F, 6.0F / 16.0F);
                PrimitiveRenderHelper.renderItem(items, pot, shown, pose, buffers, light, overlay, 0);
                pose.popPose();
            }
        }
    }

    public static final class TanningRack implements BlockEntityRenderer<TanningRackBlockEntity> {

        private final ItemRenderer items;

        public TanningRack(BlockEntityRendererProvider.Context context) {
            items = context.getItemRenderer();
        }

        @Override
        public void render(TanningRackBlockEntity rack, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            ItemStack shown = rack.output().isEmpty() ? rack.input() : rack.output();
            if (shown.isEmpty()) {
                return;
            }
            Direction facing = rack.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
            pose.pushPose();
            // Match TESRInteractable: rotate the entire interaction space around
            // the block center before applying the rack-local item transform.
            PrimitiveRenderHelper.rotateInteractionSpace(pose, facing);
            pose.translate(0.5D, 0.525D, 0.475D);
            pose.mulPose(Axis.XP.rotationDegrees(22.5F));
            pose.scale(0.75F, 0.75F, 0.75F);
            PrimitiveRenderHelper.renderItem(items, rack, shown, pose, buffers, light, overlay, 0);
            pose.popPose();
        }
    }
}
