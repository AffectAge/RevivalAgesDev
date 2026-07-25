package com.protyvkultury.revivalages.feature.world.structuralintegrity.network;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.CollapseShakeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CollapseShakePayload(
        BlockPos origin,
        float strength,
        int durationTicks,
        float radius
) implements CustomPacketPayload {

    public static final Type<CollapseShakePayload> TYPE =
            new Type<>(RevivalAges.id("collapse_shake"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CollapseShakePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    CollapseShakePayload::origin,
                    ByteBufCodecs.FLOAT,
                    CollapseShakePayload::strength,
                    ByteBufCodecs.VAR_INT,
                    CollapseShakePayload::durationTicks,
                    ByteBufCodecs.FLOAT,
                    CollapseShakePayload::radius,
                    CollapseShakePayload::new
            );

    public CollapseShakePayload {
        origin = origin.immutable();
        strength = finiteNonNegative(strength);
        durationTicks = Math.clamp(durationTicks, 0, 200);
        radius = finiteNonNegative(radius);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CollapseShakePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> NeoForge.EVENT_BUS.post(new CollapseShakeEvent(
                payload.origin(),
                payload.strength(),
                payload.durationTicks(),
                payload.radius()
        )));
    }

    private static float finiteNonNegative(float value) {
        return Float.isFinite(value) ? Math.max(0.0F, value) : 0.0F;
    }
}
