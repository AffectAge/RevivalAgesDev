package com.protyvkultury.revivalages.feature.technology.knapping.network;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.knapping.menu.KnappingMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record KnappingCellPayload(int containerId, int cell) implements CustomPacketPayload {

    public static final Type<KnappingCellPayload> TYPE =
            new Type<>(RevivalAges.id("knapping_cell"));
    public static final StreamCodec<RegistryFriendlyByteBuf, KnappingCellPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    KnappingCellPayload::containerId,
                    ByteBufCodecs.VAR_INT,
                    KnappingCellPayload::cell,
                    KnappingCellPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(KnappingCellPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.containerMenu instanceof KnappingMenu menu)
                || menu.containerId != payload.containerId()) {
            return;
        }
        menu.removeCell(player, payload.cell());
    }
}
