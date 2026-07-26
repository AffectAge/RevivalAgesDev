package com.protyvkultury.revivalages.feature.food.spoilage;

import com.protyvkultury.revivalages.RevivalAges;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FoodClockPayload(long ticks, boolean enabled, long baseLifetime, double globalMultiplier)
        implements CustomPacketPayload {

    public FoodClockPayload {
        if (ticks < 0L || baseLifetime < 1L
                || !Double.isFinite(globalMultiplier)
                || globalMultiplier <= 0.0D
                || globalMultiplier > 10_000.0D) {
            throw new IllegalArgumentException("Invalid food spoilage clock payload");
        }
    }

    public static final Type<FoodClockPayload> TYPE =
            new Type<>(RevivalAges.id("food_spoilage_clock"));
    public static final StreamCodec<ByteBuf, FoodClockPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            FoodClockPayload::ticks,
            ByteBufCodecs.BOOL,
            FoodClockPayload::enabled,
            ByteBufCodecs.VAR_LONG,
            FoodClockPayload::baseLifetime,
            ByteBufCodecs.DOUBLE,
            FoodClockPayload::globalMultiplier,
            FoodClockPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FoodClockPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> FoodSpoilageSettings.acceptRemote(
                payload.ticks(),
                payload.enabled(),
                payload.baseLifetime(),
                payload.globalMultiplier()
        ));
    }
}
