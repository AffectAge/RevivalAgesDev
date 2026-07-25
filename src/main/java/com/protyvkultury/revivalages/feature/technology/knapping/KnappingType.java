package com.protyvkultury.revivalages.feature.technology.knapping;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record KnappingType(
        SizedIngredient input,
        int amountToConsume,
        Holder<SoundEvent> clickSound,
        boolean consumeAfterComplete,
        boolean hasOffTexture,
        boolean spawnsParticles,
        ItemStack viewerIcon
) {

    public static final Codec<KnappingType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SizedIngredient.FLAT_CODEC.fieldOf("input").forGetter(KnappingType::input),
            Codec.intRange(1, 64).optionalFieldOf("amount_to_consume")
                    .forGetter(type -> type.amountToConsume == type.input.count()
                            ? Optional.empty()
                            : Optional.of(type.amountToConsume)),
            SoundEvent.CODEC.fieldOf("click_sound").forGetter(KnappingType::clickSound),
            Codec.BOOL.optionalFieldOf("consume_after_complete", false).forGetter(KnappingType::consumeAfterComplete),
            Codec.BOOL.optionalFieldOf("has_off_texture", true).forGetter(KnappingType::hasOffTexture),
            Codec.BOOL.optionalFieldOf("spawns_particles", false).forGetter(KnappingType::spawnsParticles),
            ItemStack.STRICT_CODEC.fieldOf("viewer_icon").forGetter(KnappingType::viewerIcon)
    ).apply(instance, KnappingType::new));

    private KnappingType(
            SizedIngredient input,
            Optional<Integer> amountToConsume,
            Holder<SoundEvent> clickSound,
            boolean consumeAfterComplete,
            boolean hasOffTexture,
            boolean spawnsParticles,
            ItemStack viewerIcon
    ) {
        this(
                input,
                amountToConsume.orElse(input.count()),
                clickSound,
                consumeAfterComplete,
                hasOffTexture,
                spawnsParticles,
                viewerIcon
        );
    }

    public KnappingType {
        if (amountToConsume > input.count()) {
            throw new IllegalArgumentException("amount_to_consume cannot exceed the required input count");
        }
        if (viewerIcon.isEmpty()) {
            throw new IllegalArgumentException("viewer_icon cannot be empty");
        }
        viewerIcon = viewerIcon.copy();
    }

    @Override
    public ItemStack viewerIcon() {
        return viewerIcon.copy();
    }
}
