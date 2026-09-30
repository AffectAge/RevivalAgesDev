package com.protyvkultury.revivalages.feature.world.structuralintegrity.client;

import com.protyvkultury.revivalages.config.InteractionOutlineConfig;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.CollapseShakeEvent;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityConfig;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityFeature;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.block.HorizontalSupportBlock;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.block.VerticalSupportBlock;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.item.SupportBeamItem;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class StructuralIntegrityClientEvents {

    private static final CameraShakeState CAMERA_SHAKE = new CameraShakeState();

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
        for (BlockPos pos : positions) {
            double x = pos.getX() - camera.x;
            double y = pos.getY() - camera.y;
            double z = pos.getZ() - camera.z;
            LevelRenderer.renderLineBox(event.getPoseStack(),
                    event.getMultiBufferSource().getBuffer(RenderType.lines()),
                    x, y, z, x + 1.0D, y + 1.0D, z + 1.0D,
                    red, green, blue, 1.0F);
        }
        event.setCanceled(true);
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
