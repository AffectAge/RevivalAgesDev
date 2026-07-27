package com.protyvkultury.revivalages.feature.technology.dryingrack.client;

import com.protyvkultury.revivalages.feature.technology.dryingrack.DryingRackFeature;
import com.protyvkultury.revivalages.feature.technology.dryingrack.block.AbstractDryingRackBlock;
import com.protyvkultury.revivalages.feature.technology.dryingrack.network.DryingRackScrollPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class DryingRackClientEvents {

    private DryingRackClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(DryingRackClientEvents::registerRenderers);
        NeoForge.EVENT_BUS.addListener(DryingRackClientEvents::onMouseScroll);
    }

    private static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getScrollDeltaY() == 0.0D
                || minecraft.screen != null
                || minecraft.player == null
                || !minecraft.player.isShiftKeyDown()
                || minecraft.hitResult == null
                || minecraft.hitResult.getType() != HitResult.Type.BLOCK
                || !(minecraft.hitResult instanceof BlockHitResult hit)
                || !(minecraft.level.getBlockState(hit.getBlockPos()).getBlock()
                        instanceof AbstractDryingRackBlock block)
                || !block.allowsInteractionFace(
                        minecraft.level.getBlockState(hit.getBlockPos()),
                        hit.getDirection())) {
            return;
        }
        double localX = hit.getLocation().x - hit.getBlockPos().getX();
        double localY = hit.getLocation().y - hit.getBlockPos().getY();
        double localZ = hit.getLocation().z - hit.getBlockPos().getZ();
        PacketDistributor.sendToServer(new DryingRackScrollPayload(
                hit.getBlockPos(),
                event.getScrollDeltaY() > 0.0D,
                hit.getDirection().ordinal(),
                (float) Math.clamp(localX, 0.0D, 1.0D),
                (float) Math.clamp(localY, 0.0D, 1.0D),
                (float) Math.clamp(localZ, 0.0D, 1.0D)));
        event.setCanceled(true);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                DryingRackFeature.CRUDE_DRYING_RACK_BLOCK_ENTITY.get(),
                DryingRackRenderer::new
        );
        event.registerBlockEntityRenderer(
                DryingRackFeature.DRYING_RACK_BLOCK_ENTITY.get(),
                DryingRackRenderer::new
        );
    }
}
