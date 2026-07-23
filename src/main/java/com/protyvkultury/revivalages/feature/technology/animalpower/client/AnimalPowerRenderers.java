package com.protyvkultury.revivalages.feature.technology.animalpower.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalMachineKind;
import com.protyvkultury.revivalages.feature.technology.animalpower.block.AnimalMachineBlock;
import com.protyvkultury.revivalages.feature.technology.animalpower.blockentity.AnimalMachineBlockEntity;
import com.protyvkultury.revivalages.feature.technology.animalpower.blockentity.HandGrindstoneBlockEntity;
import com.protyvkultury.revivalages.feature.technology.primitive.client.PrimitiveRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;

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

    public static final class AnimalMachine implements BlockEntityRenderer<AnimalMachineBlockEntity> {

        private final ItemRenderer items;

        public AnimalMachine(BlockEntityRendererProvider.Context context) {
            items = context.getItemRenderer();
        }

        @Override
        public void render(
                AnimalMachineBlockEntity machine,
                float partialTick,
                PoseStack pose,
                MultiBufferSource buffers,
                int light,
                int overlay
        ) {
            if (machine.getBlockState().getValue(AnimalMachineBlock.HALF) == DoubleBlockHalf.UPPER) {
                return;
            }
            renderWorkerLeash(machine, partialTick, pose, buffers, light);
            renderInvalidArea(machine, pose, buffers);
            Direction facing = machine.getBlockState().getValue(AnimalMachineBlock.FACING);
            pose.pushPose();
            PrimitiveRenderHelper.rotateInteractionSpace(pose, facing);
            renderWoodVariant(machine, pose, buffers, light, overlay);
            renderMovingPart(machine, pose, buffers, light, overlay);
            renderItem(machine, pose, buffers, light, overlay);
            if (machine.kind() == AnimalMachineKind.PRESS) {
                renderFluid(machine, pose, buffers, light, overlay);
            }
            pose.popPose();
        }

        private static void renderWoodVariant(
                AnimalMachineBlockEntity machine,
                PoseStack pose,
                MultiBufferSource buffers,
                int light,
                int overlay
        ) {
            if (machine.kind() != AnimalMachineKind.CHOPPING_BLOCK) {
                return;
            }
            Block wood = BuiltInRegistries.BLOCK.get(machine.woodVariant());
            if (wood == Blocks.AIR) {
                wood = Blocks.OAK_LOG;
            }
            pose.pushPose();
            pose.scale(1.0F, 0.375F, 1.0F);
            Minecraft.getInstance()
                    .getBlockRenderer()
                    .renderSingleBlock(
                            wood.defaultBlockState(),
                            pose,
                            buffers,
                            light,
                            overlay,
                            ModelData.EMPTY,
                            null
                    );
            pose.popPose();
        }

        private static void renderWorkerLeash(
                AnimalMachineBlockEntity machine,
                float partialTick,
                PoseStack pose,
                MultiBufferSource buffers,
                int light
        ) {
            if (machine.getLevel() == null || machine.workerId().isEmpty()) {
                return;
            }
            Mob worker = machine.getLevel()
                    .getEntitiesOfClass(
                            Mob.class,
                            new AABB(machine.getBlockPos()).inflate(64.0D),
                            mob -> mob.getUUID().equals(machine.workerId().orElse(null)))
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (worker == null) {
                return;
            }
            double anchorHeight = switch (machine.kind()) {
                case GRINDSTONE -> 0.4D;
                case CHOPPING_BLOCK -> 1.1D;
                case PRESS -> 0.4D;
            };
            Vec3 target = worker.getRopeHoldPosition(partialTick);
            double originX = machine.getBlockPos().getX() + 0.5D;
            double originY = machine.getBlockPos().getY() + anchorHeight;
            double originZ = machine.getBlockPos().getZ() + 0.5D;
            float x = (float) (target.x - originX);
            float y = (float) (target.y - originY);
            float z = (float) (target.z - originZ);
            float inverseLength = Mth.invSqrt(x * x + z * z) * 0.0125F;
            float sideX = z * inverseLength;
            float sideZ = x * inverseLength;
            VertexConsumer vertices = buffers.getBuffer(RenderType.leash());

            pose.pushPose();
            pose.translate(0.5D, anchorHeight, 0.5D);
            for (int step = 0; step <= 24; step++) {
                addLeashPair(vertices, pose, x, y, z, sideX, sideZ, step, false, light);
            }
            for (int step = 24; step >= 0; step--) {
                addLeashPair(vertices, pose, x, y, z, sideX, sideZ, step, true, light);
            }
            pose.popPose();
        }

        private static void addLeashPair(
                VertexConsumer vertices,
                PoseStack pose,
                float x,
                float y,
                float z,
                float sideX,
                float sideZ,
                int step,
                boolean reverse,
                int light
        ) {
            float progress = step / 24.0F;
            float shade = (step % 2 == (reverse ? 1 : 0)) ? 0.7F : 1.0F;
            float px = x * progress;
            float py = y > 0.0F
                    ? y * progress * progress
                    : y - y * (1.0F - progress) * (1.0F - progress);
            float pz = z * progress;
            float firstY = reverse ? 0.0F : 0.025F;
            float secondY = reverse ? 0.025F : 0.0F;
            vertices.addVertex(pose.last().pose(), px - sideX, py + firstY, pz + sideZ)
                    .setColor(0.5F * shade, 0.4F * shade, 0.3F * shade, 1.0F)
                    .setLight(light);
            vertices.addVertex(pose.last().pose(), px + sideX, py + secondY, pz - sideZ)
                    .setColor(0.5F * shade, 0.4F * shade, 0.3F * shade, 1.0F)
                    .setLight(light);
        }

        private static void renderInvalidArea(
                AnimalMachineBlockEntity machine,
                PoseStack pose,
                MultiBufferSource buffers
        ) {
            if (machine.workAreaValid()) {
                return;
            }
            LevelRenderer.renderLineBox(
                    pose,
                    buffers.getBuffer(RenderType.lines()),
                    -3.0D,
                    -1.0D,
                    -3.0D,
                    4.0D,
                    2.0D,
                    4.0D,
                    1.0F,
                    0.1F,
                    0.1F,
                    0.8F
            );
        }

        private static void renderMovingPart(
                AnimalMachineBlockEntity machine,
                PoseStack pose,
                MultiBufferSource buffers,
                int light,
                int overlay
        ) {
            String model;
            pose.pushPose();
            switch (machine.kind()) {
                case GRINDSTONE -> {
                    model = "horse_grindstone_rotor";
                    pose.translate(0.5D, 0.5D, 0.5D);
                    pose.mulPose(Axis.YP.rotationDegrees((float) (machine.progress() * 360.0D)));
                    pose.translate(-0.5D, -0.5D, -0.5D);
                }
                case CHOPPING_BLOCK -> {
                    model = "horse_chopping_blade";
                    double cycle = machine.progress() % 1.0D;
                    double windup = cycle < 0.75D
                            ? cycle / 0.75D * 0.42D
                            : (1.0D - cycle) / 0.25D * 0.42D;
                    pose.translate(0.0D, windup, 0.0D);
                }
                case PRESS -> {
                    model = "horse_press_platen";
                    pose.translate(0.0D, -0.58D * machine.progress(), 0.0D);
                }
                default -> throw new IllegalStateException("Unknown animal machine kind: " + machine.kind());
            }
            renderPart(model, machine, pose, buffers, light, overlay);
            pose.popPose();
        }

        private void renderItem(
                AnimalMachineBlockEntity machine,
                PoseStack pose,
                MultiBufferSource buffers,
                int light,
                int overlay
        ) {
            ItemStack display = machine.item(0).isEmpty() ? machine.item(1) : machine.item(0);
            if (display.isEmpty()) {
                return;
            }
            pose.pushPose();
            double height = machine.kind() == AnimalMachineKind.GRINDSTONE ? 0.9D : 1.1D;
            pose.translate(0.5D, height, 0.5D);
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            if (machine.kind() == AnimalMachineKind.GRINDSTONE) {
                pose.mulPose(Axis.ZP.rotationDegrees((float) (machine.progress() * 360.0D)));
            }
            pose.scale(0.5F, 0.5F, 0.5F);
            PrimitiveRenderHelper.renderItem(items, machine, display, pose, buffers, light, overlay, 0);
            pose.popPose();
        }

        private static void renderFluid(
                AnimalMachineBlockEntity machine,
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
            float y = 0.3F + ratio * 0.45F;
            PrimitiveRenderHelper.renderFluidSurface(
                    fluid, 0.16F, 0.84F, y, 0.16F, 0.84F, pose, buffers, light, overlay);
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
