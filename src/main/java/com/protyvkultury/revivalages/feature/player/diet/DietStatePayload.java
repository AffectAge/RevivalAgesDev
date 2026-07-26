package com.protyvkultury.revivalages.feature.player.diet;

import com.protyvkultury.revivalages.RevivalAges;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DietStatePayload(DietState state, DietSettings.Snapshot settings) implements CustomPacketPayload {

    private static final int MAX_GROUPS = 256;
    public static final Type<DietStatePayload> TYPE = new Type<>(RevivalAges.id("diet_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DietStatePayload> STREAM_CODEC =
            StreamCodec.of(DietStatePayload::encode, DietStatePayload::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DietStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            DietFeature.setState(context.player(), payload.state);
            DietSettings.acceptRemote(payload.settings);
        });
    }

    private static void encode(RegistryFriendlyByteBuf buffer, DietStatePayload payload) {
        Map<ResourceLocation, Double> values = payload.state.values();
        if (values.size() > MAX_GROUPS) {
            throw new IllegalArgumentException("Too many Diet groups");
        }
        buffer.writeVarInt(values.size());
        values.forEach((id, value) -> {
            ResourceLocation.STREAM_CODEC.encode(buffer, id);
            buffer.writeDouble(value);
        });
        buffer.writeByte(payload.state.lastFoodLevel());
        buffer.writeBoolean(payload.settings.enabled());
        buffer.writeDouble(payload.settings.nutritionMultiplier());
        buffer.writeDouble(payload.settings.multiGroupReduction());
        buffer.writeDouble(payload.settings.milkNutrition());
        buffer.writeDouble(payload.settings.cakeSliceNutrition());
    }

    private static DietStatePayload decode(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > MAX_GROUPS) {
            throw new IllegalArgumentException("Invalid Diet group count");
        }
        Map<ResourceLocation, Double> values = new LinkedHashMap<>();
        for (int index = 0; index < size; index++) {
            ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buffer);
            double value = buffer.readDouble();
            if (!Double.isFinite(value) || value < 0.0D || value > 100.0D) {
                throw new IllegalArgumentException("Invalid Diet value");
            }
            values.put(id, value);
        }
        int foodLevel = buffer.readByte();
        if (foodLevel < -1 || foodLevel > 20) {
            throw new IllegalArgumentException("Invalid Diet food level");
        }
        boolean enabled = buffer.readBoolean();
        double nutritionMultiplier = buffer.readDouble();
        double multiGroupReduction = buffer.readDouble();
        double milkNutrition = buffer.readDouble();
        double cakeSliceNutrition = buffer.readDouble();
        return new DietStatePayload(
                new DietState(values, foodLevel),
                new DietSettings.Snapshot(
                        enabled,
                        nutritionMultiplier,
                        multiGroupReduction,
                        milkNutrition,
                        cakeSliceNutrition
                )
        );
    }
}
