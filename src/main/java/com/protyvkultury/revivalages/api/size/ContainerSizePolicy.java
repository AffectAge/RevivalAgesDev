package com.protyvkultury.revivalages.api.size;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

/**
 * Data-driven inclusive maximum size accepted by a storage container.
 */
public record ContainerSizePolicy(Size maxSize) {

    public static final Codec<ContainerSizePolicy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Size.CODEC.fieldOf("max_size").forGetter(ContainerSizePolicy::maxSize)
    ).apply(instance, ContainerSizePolicy::new));

    public boolean accepts(ItemStack stack) {
        return SizeApi.getSize(stack).isEqualOrSmallerThan(maxSize);
    }
}
