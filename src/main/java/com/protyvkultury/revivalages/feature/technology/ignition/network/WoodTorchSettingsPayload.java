package com.protyvkultury.revivalages.feature.technology.ignition.network;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.ignition.WoodTorchSettings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WoodTorchSettingsPayload(
        WoodTorchSettings.Snapshot snapshot
) implements CustomPacketPayload {

    public static final Type<WoodTorchSettingsPayload> TYPE =
            new Type<>(RevivalAges.id("wood_torch_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WoodTorchSettingsPayload> STREAM_CODEC =
            StreamCodec.of(WoodTorchSettingsPayload::encode, WoodTorchSettingsPayload::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WoodTorchSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> WoodTorchSettings.acceptRemote(payload.snapshot()));
    }

    private static void encode(RegistryFriendlyByteBuf buffer, WoodTorchSettingsPayload payload) {
        buffer.writeBoolean(payload.snapshot().burnsUp());
        buffer.writeBoolean(payload.snapshot().rainExtinguishes());
    }

    private static WoodTorchSettingsPayload decode(RegistryFriendlyByteBuf buffer) {
        return new WoodTorchSettingsPayload(new WoodTorchSettings.Snapshot(
                buffer.readBoolean(),
                buffer.readBoolean()
        ));
    }
}
