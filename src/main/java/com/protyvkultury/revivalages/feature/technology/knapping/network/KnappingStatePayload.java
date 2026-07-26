package com.protyvkultury.revivalages.feature.technology.knapping.network;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.knapping.menu.KnappingMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record KnappingStatePayload(int containerId, int cells, int acceptedCell)
        implements CustomPacketPayload {

    public static final Type<KnappingStatePayload> TYPE =
            new Type<>(RevivalAges.id("knapping_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, KnappingStatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    KnappingStatePayload::containerId,
                    ByteBufCodecs.VAR_INT,
                    KnappingStatePayload::cells,
                    ByteBufCodecs.VAR_INT,
                    payload -> payload.acceptedCell + 1,
                    (containerId, cells, encodedCell) ->
                            new KnappingStatePayload(containerId, cells, encodedCell - 1)
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(KnappingStatePayload payload, IPayloadContext context) {
        if (ContentAvailability.isEnabled(ContentKey.KNAPPING)
                && context.player().containerMenu instanceof KnappingMenu menu
                && menu.containerId == payload.containerId()) {
            menu.applyServerState(payload.cells(), payload.acceptedCell());
        }
    }
}
