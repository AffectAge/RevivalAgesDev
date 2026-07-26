package com.protyvkultury.revivalages.api.size;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * Ordered physical size categories used by storage and in-world processing rules.
 */
public enum Size implements StringRepresentable {
    TINY,
    VERY_SMALL,
    SMALL,
    NORMAL,
    LARGE,
    VERY_LARGE,
    HUGE;

    public static final Codec<Size> CODEC = StringRepresentable.fromEnum(Size::values);
    public static final StreamCodec<ByteBuf, Size> STREAM_CODEC =
            ByteBufCodecs.idMapper(Size::byOrdinal, Size::ordinal);

    private final String serializedName = name().toLowerCase(Locale.ROOT);

    public boolean isSmallerThan(Size other) {
        return ordinal() < other.ordinal();
    }

    public boolean isEqualOrSmallerThan(Size other) {
        return ordinal() <= other.ordinal();
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    private static Size byOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            throw new IllegalArgumentException("Unknown item size ordinal: " + ordinal);
        }
        return values()[ordinal];
    }
}
