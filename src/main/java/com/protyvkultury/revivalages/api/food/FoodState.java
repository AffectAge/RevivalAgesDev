package com.protyvkultury.revivalages.api.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Stable per-stack freshness state.
 */
public record FoodState(long creationTick, List<ResourceLocation> traits) {

    public static final Codec<FoodState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("creation_tick").forGetter(FoodState::creationTick),
            ResourceLocation.CODEC.listOf(0, 64)
                    .optionalFieldOf("traits", List.of())
                    .forGetter(FoodState::traits)
    ).apply(instance, FoodState::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FoodState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            FoodState::creationTick,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list(64)),
            FoodState::traits,
            FoodState::new
    );

    public FoodState {
        traits = List.copyOf(traits.stream().distinct().toList());
    }
}
