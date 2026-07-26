package com.protyvkultury.revivalages.feature.inventory.carriedweight;

import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CarriedWeightStatePayload(
        CarriedWeightState state
) implements CustomPacketPayload {

    public static final Type<CarriedWeightStatePayload> TYPE =
            new Type<>(RevivalAges.id("carried_weight_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CarriedWeightStatePayload> STREAM_CODEC =
            StreamCodec.of(CarriedWeightStatePayload::encode, CarriedWeightStatePayload::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CarriedWeightStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> CarriedWeightFeature.setState(context.player(), payload.state()));
    }

    private static void encode(RegistryFriendlyByteBuf buffer, CarriedWeightStatePayload payload) {
        buffer.writeDouble(payload.state.currentWeight());
        buffer.writeDouble(payload.state.capacity());
        buffer.writeBoolean(payload.state.overloaded());
    }

    private static CarriedWeightStatePayload decode(RegistryFriendlyByteBuf buffer) {
        double current = readNonNegative(buffer, "current weight");
        double capacity = readNonNegative(buffer, "capacity");
        return new CarriedWeightStatePayload(new CarriedWeightState(
                current,
                capacity,
                buffer.readBoolean()
        ));
    }

    private static double readNonNegative(RegistryFriendlyByteBuf buffer, String field) {
        double value = buffer.readDouble();
        if (!Double.isFinite(value) || value < 0.0D || value > 1_000_000_000_000.0D) {
            throw new IllegalArgumentException("Invalid Carried Weight " + field);
        }
        return value;
    }
}
