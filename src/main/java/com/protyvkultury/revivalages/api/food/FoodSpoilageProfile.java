package com.protyvkultury.revivalages.api.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

/**
 * Per-item spoilage definition. A missing result uses rotten flesh.
 */
public record FoodSpoilageProfile(double decayModifier, Optional<ItemStack> result) {

    public static final Codec<FoodSpoilageProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.doubleRange(Double.MIN_NORMAL, 1_000.0D)
                    .fieldOf("decay_modifier")
                    .forGetter(FoodSpoilageProfile::decayModifier),
            ItemStack.STRICT_CODEC.optionalFieldOf("result").forGetter(FoodSpoilageProfile::result)
    ).apply(instance, FoodSpoilageProfile::new));

    public FoodSpoilageProfile {
        result = result.map(stack -> {
            if (stack.isEmpty()) {
                throw new IllegalArgumentException("Spoilage result cannot be empty");
            }
            return stack.copy();
        });
    }

    @Override
    public Optional<ItemStack> result() {
        return result.map(ItemStack::copy);
    }
}
