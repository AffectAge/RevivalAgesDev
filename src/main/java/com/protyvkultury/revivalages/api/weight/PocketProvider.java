package com.protyvkultury.revivalages.api.weight;

import java.util.OptionalInt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves the number of carrying-capacity pockets supplied by an item.
 */
@FunctionalInterface
public interface PocketProvider {

    OptionalInt getPockets(ItemStack stack, @Nullable Player wearer);
}
