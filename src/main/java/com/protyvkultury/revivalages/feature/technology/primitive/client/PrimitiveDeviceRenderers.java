package com.protyvkultury.revivalages.feature.technology.primitive.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.protyvkultury.revivalages.core.client.render.InteractionPreviewRenderer;
import com.protyvkultury.revivalages.feature.technology.barrel.block.BarrelBlock;
import com.protyvkultury.revivalages.feature.technology.barrel.blockentity.BarrelBlockEntity;
import com.protyvkultury.revivalages.feature.technology.campfire.blockentity.CampfireBlockEntity;
import com.protyvkultury.revivalages.feature.technology.choppingblock.blockentity.ChoppingBlockEntity;
import com.protyvkultury.revivalages.feature.technology.pitkiln.block.PitKilnBlock;
import com.protyvkultury.revivalages.feature.technology.pitkiln.block.PitKilnStage;
import com.protyvkultury.revivalages.feature.technology.pitkiln.blockentity.PitKilnBlockEntity;
import com.protyvkultury.revivalages.feature.technology.soakingpot.block.SoakingPotBlock;
import com.protyvkultury.revivalages.feature.technology.soakingpot.blockentity.SoakingPotBlockEntity;
import com.protyvkultury.revivalages.feature.technology.tanningrack.blockentity.TanningRackBlockEntity;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveTags;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.BlockHitResult;

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
                renderCampfireLog(items, campfire, log, index, false, pose, buffers, light, overlay);
            }
            if (!campfire.cookingStack().isEmpty()) {
                pose.pushPose();
                pose.translate(0.5D, 0.5D, 0.5D);
                pose.scale(0.75F, 0.75F, 0.75F);
                PrimitiveRenderHelper.renderItem(items, campfire, campfire.cookingStack(), pose, buffers, light, overlay, 20);
                if (PrimitiveTechnologyClientConfig.SHOW_PHYSICAL_ITEM_COUNTS.get()) {
                    InteractionPreviewRenderer.renderCount(campfire.cookingStack(), pose, buffers, light);
                }
                pose.popPose();
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (isTargeted(campfire, minecraft, null)
                    && PrimitiveTechnologyClientConfig.SHOW_INTERACTION_PREVIEWS.get()) {
                ItemStack held = minecraft.player.getMainHandItem();
                ItemStack preview = ItemStack.EMPTY;
                int previewLog = -1;
                if (!held.isEmpty()
                        && (held.is(PrimitiveTags.CAMPFIRE_FUELS) || held.is(net.minecraft.tags.ItemTags.LOGS))
                        && campfire.canAddLog()) {
                    preview = held;
                    previewLog = campfire.fuelLevel();
                } else if (!held.isEmpty() && campfire.canCook(held) && campfire.cookingStack().isEmpty()) {
                    preview = held;
                } else if (!campfire.cookingStack().isEmpty()
                        && (held.isEmpty() || minecraft.player.isShiftKeyDown())) {
                    preview = campfire.cookingStack();
                } else if ((held.isEmpty() || minecraft.player.isShiftKeyDown()) && campfire.canRemoveLog()) {
                    preview = campfire.logStack(campfire.fuelLevel() - 1);
                    previewLog = campfire.fuelLevel() - 1;
                }
                if (!preview.isEmpty()) {
                    if (previewLog >= 0) {
                        renderCampfireLog(items, campfire, preview, previewLog, true, pose, buffers, light, overlay);
                        return;
                    }
                    pose.pushPose();
                    pose.translate(0.5D, 0.5D, 0.5D);
                    pose.scale(0.75F, 0.75F, 0.75F);
                    InteractionPreviewRenderer.renderItemPreview(
                            items, campfire, preview, 20, pose, buffers, light, overlay
                    );
                    pose.popPose();
                }
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
            ItemStack shown = chopping.output().isEmpty() ? chopping.input() : chopping.output();
            if (!shown.isEmpty()) {
                pose.pushPose();
                pose.translate(0.5D, 0.75D, 0.5D);
                pose.scale(0.75F, 0.75F, 0.75F);
                PrimitiveRenderHelper.renderItem(items, chopping, shown, pose, buffers, light, overlay, 0);
                if (PrimitiveTechnologyClientConfig.SHOW_PHYSICAL_ITEM_COUNTS.get()) {
                    InteractionPreviewRenderer.renderCount(shown, pose, buffers, light);
                }
                pose.popPose();
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (!PrimitiveTechnologyClientConfig.SHOW_INTERACTION_PREVIEWS.get()
                    || minecraft.player == null
                    || !(minecraft.hitResult instanceof BlockHitResult hit)
                    || !hit.getBlockPos().equals(chopping.getBlockPos())) {
                return;
            }
            ItemStack held = minecraft.player.getMainHandItem();
            ItemStack preview = ItemStack.EMPTY;
            if (!held.isEmpty() && chopping.canInsert(held)) {
                preview = held;
            } else if (!shown.isEmpty() && (held.isEmpty() || minecraft.player.isShiftKeyDown())) {
                preview = shown;
            }
            if (!preview.isEmpty()) {
                pose.pushPose();
                pose.translate(0.5D, 0.75D, 0.5D);
                pose.scale(0.75F, 0.75F, 0.75F);
                InteractionPreviewRenderer.renderItemPreview(
                        items,
                        chopping,
                        preview,
                        0,
                        pose,
                        buffers,
                        light,
                        overlay
                );
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
            if (!shown.isEmpty() && (stage == PitKilnStage.EMPTY || stage == PitKilnStage.COMPLETE)) {
                pose.pushPose();
                pose.translate(0.5D, 0.35D, 0.5D);
                pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                pose.scale(0.5F, 0.5F, 0.5F);
                PrimitiveRenderHelper.renderItem(items, kiln, shown, pose, buffers, light, overlay, 0);
                if (PrimitiveTechnologyClientConfig.SHOW_PHYSICAL_ITEM_COUNTS.get()) {
                    InteractionPreviewRenderer.renderCount(shown, pose, buffers, light);
                }
                pose.popPose();
            }
            for (int index = 0; index < 3; index++) {
                ItemStack log = kiln.logStack(index);
                if (log.isEmpty()) {
                    continue;
                }
                pose.pushPose();
                // Reference parity: three adjacent one-third-width logs spanning the pit.
                double oneThird = 1.0D / 3.0D;
                double oneSixth = 1.0D / 6.0D;
                pose.translate(index * oneThird + oneSixth, 2.0D * oneThird + oneSixth, 0.5D);
                pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                pose.scale((float) oneThird, 1.0F, (float) oneThird);
                PrimitiveRenderHelper.renderItem(items, kiln, log, pose, buffers, light, overlay, index + 1);
                if (PrimitiveTechnologyClientConfig.SHOW_PHYSICAL_ITEM_COUNTS.get()) {
                    InteractionPreviewRenderer.renderCount(log, pose, buffers, light);
                }
                pose.popPose();
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (isTargeted(kiln, minecraft, Direction.UP)
                    && PrimitiveTechnologyClientConfig.SHOW_INTERACTION_PREVIEWS.get()) {
                ItemStack held = minecraft.player.getMainHandItem();
                ItemStack preview = ItemStack.EMPTY;
                int previewLog = -1;
                if ((stage == PitKilnStage.THATCH || stage == PitKilnStage.WOOD)
                        && !held.isEmpty()
                        && (held.is(PrimitiveTags.PIT_KILN_LOGS) || held.is(net.minecraft.tags.ItemTags.LOGS))
                        && kiln.canAddLog()) {
                    preview = held;
                    previewLog = kiln.logCount();
                } else if (stage == PitKilnStage.EMPTY && !held.isEmpty() && kiln.canInsert(held)) {
                    preview = held;
                } else if ((stage == PitKilnStage.THATCH || stage == PitKilnStage.WOOD)
                        && kiln.logCount() > 0
                        && (held.isEmpty() || minecraft.player.isShiftKeyDown())) {
                    previewLog = kiln.logCount() - 1;
                    preview = kiln.logStack(previewLog);
                } else if (!shown.isEmpty() && (held.isEmpty() || minecraft.player.isShiftKeyDown())) {
                    preview = shown;
                }
                if (!preview.isEmpty()) {
                    if (previewLog >= 0) {
                        pose.pushPose();
                        double oneThird = 1.0D / 3.0D;
                        double oneSixth = 1.0D / 6.0D;
                        pose.translate(previewLog * oneThird + oneSixth, 2.0D * oneThird + oneSixth, 0.5D);
                        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                        pose.scale((float) oneThird, 1.0F, (float) oneThird);
                        InteractionPreviewRenderer.renderItemPreview(
                                items, kiln, preview, previewLog + 1, pose, buffers, light, overlay
                        );
                        pose.popPose();
                        return;
                    }
                    pose.pushPose();
                    pose.translate(0.5D, 0.35D, 0.5D);
                    pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                    pose.scale(0.5F, 0.5F, 0.5F);
                    InteractionPreviewRenderer.renderItemPreview(
                            items, kiln, preview, 0, pose, buffers, light, overlay
                    );
                    pose.popPose();
                }
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
                    barrel.fluidTank().getFluid(),
                    2.0F / 16.0F,
                    14.0F / 16.0F,
                    2.0F / 16.0F + ratio * 12.0F / 16.0F,
                    2.0F / 16.0F,
                    14.0F / 16.0F,
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
                if (PrimitiveTechnologyClientConfig.SHOW_PHYSICAL_ITEM_COUNTS.get()) {
                    InteractionPreviewRenderer.renderCount(stacks[slot], pose, buffers, light);
                }
                pose.popPose();
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (isTargeted(barrel, minecraft, Direction.UP)
                    && PrimitiveTechnologyClientConfig.SHOW_INTERACTION_PREVIEWS.get()) {
                BlockHitResult hit = (BlockHitResult) minecraft.hitResult;
                int slot = barrel.slotFromHit(
                        hit.getLocation().x - barrel.getBlockPos().getX(),
                        hit.getLocation().z - barrel.getBlockPos().getZ()
                );
                ItemStack held = minecraft.player.getMainHandItem();
                ItemStack stored = barrel.item(slot);
                ItemStack preview = !held.isEmpty() && barrel.canInsert(slot, held)
                        ? held
                        : !stored.isEmpty() && (held.isEmpty() || minecraft.player.isShiftKeyDown())
                                ? stored
                                : ItemStack.EMPTY;
                if (!preview.isEmpty()) {
                    pose.pushPose();
                    pose.translate(
                            (slot & 1) == 0 ? 0.3125D : 0.6875D,
                            14.0D / 16.0D,
                            (slot & 2) == 0 ? 0.3125D : 0.6875D
                    );
                    pose.scale(3.0F / 16.0F, 3.0F / 16.0F, 3.0F / 16.0F);
                    InteractionPreviewRenderer.renderItemPreview(
                            items, barrel, preview, slot, pose, buffers, light, overlay
                    );
                    pose.popPose();
                }
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
            boolean campfire = pot.getBlockState().getValue(SoakingPotBlock.CAMPFIRE);
            float y = 1.0F / 16.0F + ratio * 7.0F / 16.0F;
            if (campfire) {
                y -= 5.0F / 16.0F;
            }
            PrimitiveRenderHelper.renderFluidSurface(
                    pot.fluidTank().getFluid(),
                    0.25F,
                    0.75F,
                    y,
                    0.25F,
                    0.75F,
                    pose,
                    buffers,
                    light,
                    overlay
            );
            ItemStack shown = pot.output().isEmpty() ? pot.input() : pot.output();
            if (!shown.isEmpty()) {
                pose.pushPose();
                pose.translate(0.5D, campfire ? 3.0D / 16.0D : 0.5D, 0.5D);
                pose.scale(6.0F / 16.0F, 6.0F / 16.0F, 6.0F / 16.0F);
                PrimitiveRenderHelper.renderItem(items, pot, shown, pose, buffers, light, overlay, 0);
                if (PrimitiveTechnologyClientConfig.SHOW_PHYSICAL_ITEM_COUNTS.get()) {
                    InteractionPreviewRenderer.renderCount(shown, pose, buffers, light);
                }
                pose.popPose();
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (isTargeted(pot, minecraft, Direction.UP)
                    && PrimitiveTechnologyClientConfig.SHOW_INTERACTION_PREVIEWS.get()) {
                ItemStack held = minecraft.player.getMainHandItem();
                ItemStack preview = !held.isEmpty() && pot.canInsert(held)
                        ? held
                        : !shown.isEmpty() && (held.isEmpty() || minecraft.player.isShiftKeyDown())
                                ? shown
                                : ItemStack.EMPTY;
                if (!preview.isEmpty()) {
                    pose.pushPose();
                    pose.translate(0.5D, campfire ? 3.0D / 16.0D : 0.5D, 0.5D);
                    pose.scale(6.0F / 16.0F, 6.0F / 16.0F, 6.0F / 16.0F);
                    InteractionPreviewRenderer.renderItemPreview(
                            items, pot, preview, 0, pose, buffers, light, overlay
                    );
                    pose.popPose();
                }
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
            Direction facing = rack.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
            if (!shown.isEmpty()) {
                pose.pushPose();
                // Match TESRInteractable: rotate the entire interaction space around
                // the block center before applying the rack-local item transform.
                PrimitiveRenderHelper.rotateInteractionSpace(pose, facing);
                pose.translate(0.5D, 0.525D, 0.475D);
                pose.mulPose(Axis.XP.rotationDegrees(22.5F));
                pose.scale(0.75F, 0.75F, 0.75F);
                PrimitiveRenderHelper.renderItem(items, rack, shown, pose, buffers, light, overlay, 0);
                if (PrimitiveTechnologyClientConfig.SHOW_PHYSICAL_ITEM_COUNTS.get()) {
                    InteractionPreviewRenderer.renderCount(shown, pose, buffers, light);
                }
                pose.popPose();
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (isTargeted(rack, minecraft, null)
                    && PrimitiveTechnologyClientConfig.SHOW_INTERACTION_PREVIEWS.get()) {
                ItemStack held = minecraft.player.getMainHandItem();
                ItemStack preview = !held.isEmpty() && rack.canInsert(held)
                        ? held
                        : (held.isEmpty() || minecraft.player.isShiftKeyDown()) ? shown : ItemStack.EMPTY;
                if (!preview.isEmpty()) {
                    pose.pushPose();
                    PrimitiveRenderHelper.rotateInteractionSpace(pose, facing);
                    pose.translate(0.5D, 0.525D, 0.475D);
                    pose.mulPose(Axis.XP.rotationDegrees(22.5F));
                    pose.scale(0.75F, 0.75F, 0.75F);
                    InteractionPreviewRenderer.renderItemPreview(
                            items, rack, preview, 0, pose, buffers, light, overlay
                    );
                    pose.popPose();
                }
            }
        }

    }

    private static void renderCampfireLog(
            ItemRenderer items,
            CampfireBlockEntity campfire,
            ItemStack stack,
            int index,
            boolean preview,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay
    ) {
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
        if (preview) {
            InteractionPreviewRenderer.renderItemPreview(
                    items, campfire, stack, index, pose, buffers, light, overlay
            );
        } else {
            PrimitiveRenderHelper.renderItem(items, campfire, stack, pose, buffers, light, overlay, index);
            if (PrimitiveTechnologyClientConfig.SHOW_PHYSICAL_ITEM_COUNTS.get()) {
                InteractionPreviewRenderer.renderCount(stack, pose, buffers, light);
            }
        }
        pose.popPose();
    }

    private static boolean isTargeted(
            net.minecraft.world.level.block.entity.BlockEntity blockEntity,
            Minecraft minecraft,
            Direction requiredFace
    ) {
        return minecraft.player != null
                && minecraft.hitResult instanceof BlockHitResult hit
                && hit.getBlockPos().equals(blockEntity.getBlockPos())
                && (requiredFace == null || hit.getDirection() == requiredFace);
    }
}
