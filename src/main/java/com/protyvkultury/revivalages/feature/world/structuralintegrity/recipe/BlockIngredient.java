package com.protyvkultury.revivalages.feature.world.structuralintegrity.recipe;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.state.BlockState;

public record BlockIngredient(List<String> entries) {

    public static final Codec<BlockIngredient> CODEC = Codec.withAlternative(
            Codec.STRING.xmap(value -> new BlockIngredient(List.of(value)), value -> value.entries().getFirst()),
            Codec.STRING.listOf().xmap(BlockIngredient::new, BlockIngredient::entries)
    ).validate(value -> value.entries().isEmpty()
            ? com.mojang.serialization.DataResult.error(() -> "Block ingredient cannot be empty")
            : com.mojang.serialization.DataResult.success(value));

    public BlockIngredient {
        entries = List.copyOf(entries);
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("Block ingredient cannot be empty");
        }
    }

    public boolean test(BlockState state) {
        for (String entry : entries) {
            if (entry.startsWith("#")) {
                ResourceLocation id = ResourceLocation.tryParse(entry.substring(1));
                if (id != null && state.is(TagKey.create(Registries.BLOCK, id))) {
                    return true;
                }
            } else {
                ResourceLocation id = ResourceLocation.tryParse(entry);
                if (id != null && BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }
}
