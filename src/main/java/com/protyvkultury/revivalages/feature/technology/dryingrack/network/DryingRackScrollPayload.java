package com.protyvkultury.revivalages.feature.technology.dryingrack.network;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.core.interaction.ItemStackInteraction;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.technology.dryingrack.block.AbstractDryingRackBlock;
import com.protyvkultury.revivalages.feature.technology.dryingrack.blockentity.DryingRackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DryingRackScrollPayload(
        BlockPos pos,
        boolean insert,
        int faceOrdinal,
        float localX,
        float localY,
        float localZ
) implements CustomPacketPayload {

    public static final Type<DryingRackScrollPayload> TYPE =
            new Type<>(RevivalAges.id("drying_rack_scroll"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DryingRackScrollPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        BlockPos.STREAM_CODEC.encode(buffer, payload.pos());
                        buffer.writeBoolean(payload.insert());
                        buffer.writeByte(payload.faceOrdinal());
                        buffer.writeFloat(payload.localX());
                        buffer.writeFloat(payload.localY());
                        buffer.writeFloat(payload.localZ());
                    },
                    buffer -> new DryingRackScrollPayload(
                            BlockPos.STREAM_CODEC.decode(buffer),
                            buffer.readBoolean(),
                            buffer.readUnsignedByte(),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat()));

    public DryingRackScrollPayload {
        pos = pos.immutable();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DryingRackScrollPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        context.enqueueWork(() -> apply(player, payload));
    }

    private static void apply(ServerPlayer player, DryingRackScrollPayload payload) {
        if (!player.isShiftKeyDown()
                || payload.faceOrdinal() < 0
                || payload.faceOrdinal() >= Direction.values().length
                || !finiteUnit(payload.localX())
                || !finiteUnit(payload.localY())
                || !finiteUnit(payload.localZ())
                || !player.level().getChunkSource().hasChunk(
                        payload.pos().getX() >> 4,
                        payload.pos().getZ() >> 4)
                || player.distanceToSqr(Vec3.atCenterOf(payload.pos())) > 36.0D) {
            return;
        }
        BlockState state = player.level().getBlockState(payload.pos());
        if (!(state.getBlock() instanceof AbstractDryingRackBlock block)
                || !(player.level().getBlockEntity(payload.pos()) instanceof DryingRackBlockEntity rack)
                || !ContentAvailability.isEnabled(rack.contentKey())) {
            return;
        }
        Direction face = Direction.values()[payload.faceOrdinal()];
        if (!block.allowsInteractionFace(state, face)) {
            return;
        }
        Vec3 location = new Vec3(
                payload.pos().getX() + payload.localX(),
                payload.pos().getY() + payload.localY(),
                payload.pos().getZ() + payload.localZ());
        int slot = block.interactionSlot(
                state,
                new BlockHitResult(location, face, payload.pos(), false));
        if (payload.insert()) {
            ItemStack held = player.getMainHandItem();
            if (!held.isEmpty() && rack.canInsert(slot)) {
                rack.insert(slot, held, player.hasInfiniteMaterials());
            }
            return;
        }
        ItemStackInteraction.giveOrDrop(
                player.level(),
                payload.pos(),
                player,
                rack.extract(slot));
    }

    private static boolean finiteUnit(float value) {
        return Float.isFinite(value) && value >= 0.0F && value <= 1.0F;
    }
}
