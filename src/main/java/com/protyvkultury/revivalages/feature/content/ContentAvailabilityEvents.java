package com.protyvkultury.revivalages.feature.content;

import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Common interaction boundary which prevents disabled content from being used. */
public final class ContentAvailabilityEvents {

    private ContentAvailabilityEvents() {
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (denyItem(event.getItemStack(), event.getEntity())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        boolean denied = denyItem(event.getItemStack(), event.getEntity());
        if (!denied) {
            var state = event.getLevel().getBlockState(event.getPos());
            var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (id.getNamespace().equals(com.protyvkultury.revivalages.RevivalAges.MOD_ID)
                    && !ContentAvailability.isBlockEnabled(id)) {
                deny(ContentAvailability.blockKeys(id), event.getEntity());
                denied = true;
            }
        }
        if (denied) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setUseBlock(net.neoforged.neoforge.common.util.TriState.FALSE);
            event.setUseItem(net.neoforged.neoforge.common.util.TriState.FALSE);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (denyItem(event.getItemStack(), event.getEntity())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) {
            return;
        }
        var state = event.getState();
        var blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!blockId.getNamespace().equals(com.protyvkultury.revivalages.RevivalAges.MOD_ID)
                || ContentAvailability.isBlockEnabled(blockId)
                || state.getBlock().asItem() == Items.AIR) {
            return;
        }

        var blockEntity = level.getBlockEntity(event.getPos());
        ItemStack preserved = new ItemStack(state.getBlock().asItem());
        if (blockEntity != null) {
            preserved.set(
                    DataComponents.BLOCK_ENTITY_DATA,
                    CustomData.of(blockEntity.saveWithFullMetadata(level.registryAccess()))
            );
        }
        event.setCanceled(true);
        if (blockEntity != null) {
            level.removeBlockEntity(event.getPos());
        }
        var fluidState = state.getFluidState();
        level.setBlock(
                event.getPos(),
                fluidState.isEmpty() ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                        : fluidState.createLegacyBlock(),
                Block.UPDATE_ALL
        );
        Block.popResource(level, event.getPos(), preserved);
    }

    private static boolean denyItem(ItemStack stack, net.minecraft.world.entity.player.Player player) {
        if (stack.isEmpty()) {
            return false;
        }
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!id.getNamespace().equals(com.protyvkultury.revivalages.RevivalAges.MOD_ID)
                || ContentAvailability.isItemEnabled(id)) {
            return false;
        }
        deny(ContentAvailability.itemKeys(id), player);
        return true;
    }

    private static void deny(Set<ContentKey> keys, net.minecraft.world.entity.player.Player player) {
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        Component name = keys.stream()
                .filter(key -> !ContentAvailability.isEnabled(key))
                .findFirst()
                .orElse(ContentKey.PRIMITIVE_TECHNOLOGY)
                .displayName();
        player.displayClientMessage(
                Component.translatable("message.revivalages.content.disabled", name),
                true
        );
    }
}
