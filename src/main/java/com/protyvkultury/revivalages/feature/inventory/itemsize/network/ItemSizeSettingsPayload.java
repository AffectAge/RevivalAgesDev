package com.protyvkultury.revivalages.feature.inventory.itemsize.network;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.size.Size;
import com.protyvkultury.revivalages.feature.inventory.itemsize.ItemSizeSettings;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ItemSizeSettingsPayload(
        ItemSizeSettings.Snapshot snapshot
) implements CustomPacketPayload {

    public static final Type<ItemSizeSettingsPayload> TYPE = new Type<>(RevivalAges.id("item_size_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemSizeSettingsPayload> STREAM_CODEC =
            StreamCodec.of(ItemSizeSettingsPayload::encode, ItemSizeSettingsPayload::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ItemSizeSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ItemSizeSettings.acceptRemote(payload.snapshot()));
    }

    private static void encode(RegistryFriendlyByteBuf buffer, ItemSizeSettingsPayload payload) {
        ItemSizeSettings.Snapshot snapshot = payload.snapshot();
        buffer.writeBoolean(snapshot.enabled());
        Size.STREAM_CODEC.encode(buffer, snapshot.chestMaximumSize());
        Size.STREAM_CODEC.encode(buffer, snapshot.bundleMaximumSize());
        Size.STREAM_CODEC.encode(buffer, snapshot.pitKilnBatchableMaximumSize());
        buffer.writeVarInt(snapshot.pitKilnBatchSize());
        buffer.writeVarInt(snapshot.pitKilnOversizedBatchSize());
        writeMap(buffer, snapshot.blockOverrides());
        writeMap(buffer, snapshot.itemOverrides());
    }

    private static ItemSizeSettingsPayload decode(RegistryFriendlyByteBuf buffer) {
        return new ItemSizeSettingsPayload(new ItemSizeSettings.Snapshot(
                buffer.readBoolean(),
                Size.STREAM_CODEC.decode(buffer),
                Size.STREAM_CODEC.decode(buffer),
                Size.STREAM_CODEC.decode(buffer),
                buffer.readVarInt(),
                buffer.readVarInt(),
                readMap(buffer),
                readMap(buffer)
        ));
    }

    private static void writeMap(RegistryFriendlyByteBuf buffer, Map<ResourceLocation, Size> values) {
        buffer.writeVarInt(values.size());
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    ResourceLocation.STREAM_CODEC.encode(buffer, entry.getKey());
                    Size.STREAM_CODEC.encode(buffer, entry.getValue());
                });
    }

    private static Map<ResourceLocation, Size> readMap(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > 4096) {
            throw new IllegalArgumentException("Invalid Item Size override count: " + count);
        }
        Map<ResourceLocation, Size> values = new LinkedHashMap<>();
        for (int index = 0; index < count; index++) {
            ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buffer);
            Size size = Size.STREAM_CODEC.decode(buffer);
            values.put(id, size);
        }
        return values;
    }
}
