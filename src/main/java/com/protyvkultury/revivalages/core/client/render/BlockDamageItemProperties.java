package com.protyvkultury.revivalages.core.client.render;

import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Selects an item's wear-stage model from the block state copied into its stack. */
public final class BlockDamageItemProperties {

    private BlockDamageItemProperties() {
    }

    /** Registers the shared damage predicate for a block item on the client thread. */
    public static void register(Item item, IntegerProperty damageProperty) {
        ItemProperties.register(item, RevivalAges.id("damage"), (stack, level, entity, seed) -> {
            BlockItemStateProperties state = stack.get(DataComponents.BLOCK_STATE);
            Integer damage = state == null ? null : state.get(damageProperty);
            return damage == null ? 0.0F : damage.floatValue();
        });
    }
}
