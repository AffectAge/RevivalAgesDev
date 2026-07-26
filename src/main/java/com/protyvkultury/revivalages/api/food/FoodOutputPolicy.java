package com.protyvkultury.revivalages.api.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public record FoodOutputPolicy(Mode mode, List<ResourceLocation> traits) {

    public static final Codec<FoodOutputPolicy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Mode.CODEC.fieldOf("mode").forGetter(FoodOutputPolicy::mode),
            ResourceLocation.CODEC.listOf(0, 64)
                    .optionalFieldOf("traits", List.of())
                    .forGetter(FoodOutputPolicy::traits)
    ).apply(instance, FoodOutputPolicy::new));

    public FoodOutputPolicy {
        traits = List.copyOf(traits.stream().distinct().toList());
    }

    public enum Mode implements StringRepresentable {
        COPY_FIRST("copy_first"),
        COPY_OLDEST("copy_oldest"),
        RESET("reset"),
        ADD_TRAIT("add_trait"),
        REMOVE_TRAIT("remove_trait");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        private final String serializedName;

        Mode(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
