package com.protyvkultury.revivalages.feature.world.structuralintegrity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.protyvkultury.revivalages.config.InteractionOutlineConfig;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.CollapseShakeEvent;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityConfig;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityFeature;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityTags;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.block.HorizontalSupportBlock;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.block.VerticalSupportBlock;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.item.SupportBeamItem;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class StructuralIntegrityClientEvents {

    private static final CameraShakeState CAMERA_SHAKE = new CameraShakeState();
    // Match the support post and arm model bounds, which are narrower than their collision shapes.
    private static final VoxelShape POST = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    private static final VoxelShape NORTH_ARM = Block.box(6.0D, 11.0D, 0.0D, 10.0D, 15.0D, 8.0D);
    private static final VoxelShape EAST_ARM = Block.box(8.0D, 11.0D, 6.0D, 16.0D, 15.0D, 10.0D);
    private static final VoxelShape SOUTH_ARM = Block.box(6.0D, 11.0D, 8.0D, 10.0D, 15.0D, 16.0D);
    private static final VoxelShape WEST_ARM = Block.box(0.0D, 11.0D, 6.0D, 8.0D, 15.0D, 10.0D);

    private StructuralIntegrityClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(StructuralIntegrityClientEvents::registerRenderers);
        NeoForge.EVENT_BUS.addListener(StructuralIntegrityClientEvents::onShake);
        NeoForge.EVENT_BUS.addListener(StructuralIntegrityClientEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(StructuralIntegrityClientEvents::onCameraAngles);
        NeoForge.EVENT_BUS.addListener(StructuralIntegrityClientEvents::renderPlacementHighlight);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                StructuralIntegrityFeature.FALLING_BLOCK_ENTITY.get(),
                FallingBlockRenderer::new
        );
    }

    private static void onShake(CollapseShakeEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!StructuralIntegrityConfig.cameraShakeEnabled() || minecraft.player == null) {
            return;
        }
        double distance = minecraft.player.position().distanceTo(Vec3.atCenterOf(event.origin()));
        if (event.radius() <= 0.0F || distance >= event.radius()) {
            return;
        }
        float attenuation = Mth.clamp(1.0F - (float) (distance / event.radius()), 0.0F, 1.0F);
        float amplitude = event.strength()
                * attenuation
                * (float) StructuralIntegrityConfig.cameraShakeIntensity();
        CAMERA_SHAKE.add(amplitude, event.durationTicks(), event.origin().asLong());
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!StructuralIntegrityConfig.cameraShakeEnabled()
                || minecraft.level == null
                || minecraft.player == null) {
            CAMERA_SHAKE.clear();
            return;
        }
        CAMERA_SHAKE.tick();
    }

    private static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        CAMERA_SHAKE.apply(event);
    }

    private static void renderPlacementHighlight(RenderHighlightEvent.Block event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack stack = minecraft.player.getMainHandItem();
        if (!(stack.getItem() instanceof SupportBeamItem)) {
            hand = InteractionHand.OFF_HAND;
            stack = minecraft.player.getOffhandItem();
        }
        if (!(stack.getItem() instanceof SupportBeamItem beamItem)) {
            return;
        }
        BlockHitResult hit = event.getTarget();
        BlockPlaceContext context = new BlockPlaceContext(minecraft.player, hand, stack, hit);
        BlockState state = beamItem.previewState(context);
        if (state == null) {
            return;
        }
        BlockPos origin = context.getClickedPos();
        List<BlockPos> positions = switch (state.getBlock()) {
            case VerticalSupportBlock vertical -> vertical.placementPositions(
                    minecraft.level, origin, minecraft.player, stack);
            case HorizontalSupportBlock horizontal -> horizontal.placementPositions(minecraft.level, origin, stack);
            default -> List.of();
        };
        if (positions.isEmpty()) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        int color = InteractionOutlineConfig.rgb();
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        Set<BlockPos> plannedPositions = Set.copyOf(positions);
        VertexConsumer lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
        for (BlockPos pos : positions) {
            VoxelShape shape = previewShape(state, minecraft.level, pos, plannedPositions);
            double x = pos.getX() - camera.x;
            double y = pos.getY() - camera.y;
            double z = pos.getZ() - camera.z;
            renderPreviewShape(event.getPoseStack(), lines, shape, x, y, z, red, green, blue);
        }
        event.setCanceled(true);
    }

    private static VoxelShape previewShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Set<BlockPos> plannedPositions
    ) {
        VoxelShape shape = state.getBlock() instanceof VerticalSupportBlock ? POST : Shapes.empty();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(direction);
            if (!plannedPositions.contains(neighbor)
                    && !level.getBlockState(neighbor).is(StructuralIntegrityTags.SUPPORT_BEAMS)) {
                continue;
            }
            shape = Shapes.or(shape, switch (direction) {
                case NORTH -> NORTH_ARM;
                case EAST -> EAST_ARM;
                case SOUTH -> SOUTH_ARM;
                case WEST -> WEST_ARM;
                default -> throw new IllegalStateException("Unexpected support direction: " + direction);
            });
        }
        return shape;
    }

    private static void renderPreviewShape(
            PoseStack poseStack,
            VertexConsumer lines,
            VoxelShape shape,
            double x,
            double y,
            double z,
            float red,
            float green,
            float blue
    ) {
        PoseStack.Pose pose = poseStack.last();
        shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float dx = (float) (maxX - minX);
            float dy = (float) (maxY - minY);
            float dz = (float) (maxZ - minZ);
            float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            lines.addVertex(pose, (float) (x + minX), (float) (y + minY), (float) (z + minZ))
                    .setColor(red, green, blue, 1.0F)
                    .setNormal(pose, dx / length, dy / length, dz / length);
            lines.addVertex(pose, (float) (x + maxX), (float) (y + maxY), (float) (z + maxZ))
                    .setColor(red, green, blue, 1.0F)
                    .setNormal(pose, dx / length, dy / length, dz / length);
        });
    }

    private static final class CameraShakeState {

        private float amplitude;
        private float phase;
        private int remainingTicks;
        private int totalTicks;

        private void add(float addedAmplitude, int durationTicks, long seed) {
            if (addedAmplitude <= 0.0F || durationTicks <= 0) {
                return;
            }
            if (remainingTicks > 0) {
                amplitude = Math.min(
                        8.0F,
                        Math.max(amplitude, addedAmplitude)
                                + Math.min(amplitude, addedAmplitude) * 0.35F
                );
            } else {
                amplitude = Math.min(8.0F, addedAmplitude);
            }
            remainingTicks = Math.max(remainingTicks, durationTicks);
            totalTicks = remainingTicks;
            phase = (float) ((seed ^ seed >>> 32) & 0xFFFFL) / 65535.0F * Mth.TWO_PI;
        }

        private void tick() {
            if (remainingTicks > 0) {
                remainingTicks--;
            }
            if (remainingTicks == 0) {
                clear();
            }
        }

        private void apply(ViewportEvent.ComputeCameraAngles event) {
            if (remainingTicks <= 0 || totalTicks <= 0 || amplitude <= 0.0F) {
                return;
            }
            float age = totalTicks - remainingTicks + (float) event.getPartialTick();
            float envelope = remainingTicks / (float) totalTicks;
            envelope *= envelope;
            float current = amplitude * envelope;
            event.setPitch(event.getPitch() + Mth.sin(age * 2.35F + phase) * current);
            event.setYaw(event.getYaw() + Mth.sin(age * 1.65F + phase * 1.37F) * current * 0.55F);
            event.setRoll(event.getRoll() + Mth.sin(age * 2.05F + phase * 0.73F) * current * 0.45F);
        }

        private void clear() {
            amplitude = 0.0F;
            phase = 0.0F;
            remainingTicks = 0;
            totalTicks = 0;
        }
    }
}
