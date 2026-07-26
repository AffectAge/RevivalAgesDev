package com.protyvkultury.revivalages.api.diet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

/**
 * Reloadable definition of one independently tracked diet group.
 *
 * @param icon item rendered by the diet screen
 * @param color RGB display color
 * @param decayMultiplier multiplier applied when vanilla hunger decreases
 */
public record DietGroup(ItemStack icon, int color, double decayMultiplier) {

    public static final Codec<DietGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.STRICT_CODEC.fieldOf("icon").forGetter(DietGroup::icon),
            Codec.intRange(0, 0xFFFFFF).fieldOf("color").forGetter(DietGroup::color),
            Codec.doubleRange(0.0D, 1_000.0D)
                    .optionalFieldOf("decay_multiplier", 1.0D)
                    .forGetter(DietGroup::decayMultiplier)
    ).apply(instance, DietGroup::new));

    public DietGroup {
        if (icon.isEmpty()) {
            throw new IllegalArgumentException("Diet group icon cannot be empty");
        }
        icon = icon.copyWithCount(1);
    }

    @Override
    public ItemStack icon() {
        return icon.copy();
    }
}
