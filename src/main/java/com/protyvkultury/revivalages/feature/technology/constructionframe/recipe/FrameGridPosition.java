package com.protyvkultury.revivalages.feature.technology.constructionframe.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** A zero-based coordinate in the physical 3x3x3 assembly grid. */
public record FrameGridPosition(int x, int y, int z) {

    public static final Codec<FrameGridPosition> CODEC = RecordCodecBuilder.<FrameGridPosition>create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(FrameGridPosition::x),
            Codec.INT.fieldOf("y").forGetter(FrameGridPosition::y),
            Codec.INT.fieldOf("z").forGetter(FrameGridPosition::z)
    ).apply(instance, FrameGridPosition::new)).comapFlatMap(
            value -> value.valid()
                    ? DataResult.success(value)
                    : DataResult.error(() -> "Frame coordinate must be within 0..2 on every axis"),
            value -> value
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FrameGridPosition> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, value) -> {
                        buffer.writeByte(value.x);
                        buffer.writeByte(value.y);
                        buffer.writeByte(value.z);
                    },
                    buffer -> new FrameGridPosition(buffer.readUnsignedByte(), buffer.readUnsignedByte(),
                            buffer.readUnsignedByte())
            );

    public boolean valid() {
        return x >= 0 && x < 3 && y >= 0 && y < 3 && z >= 0 && z < 3;
    }

    public int index() {
        return FrameGridMath.index(x, y, z);
    }

    public int rotatedIndex(int quarterTurns) {
        return FrameGridMath.rotatedIndex(x, y, z, quarterTurns);
    }

    public static FrameGridPosition fromIndex(int index) {
        if (index < 0 || index >= 27) {
            throw new IllegalArgumentException("Frame index must be within 0..26");
        }
        int[] position = FrameGridMath.position(index);
        return new FrameGridPosition(position[0], position[1], position[2]);
    }
}
