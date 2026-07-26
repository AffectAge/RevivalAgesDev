package com.protyvkultury.revivalages.feature.content;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Server-authoritative availability source shared by gameplay, data, and integrations. */
public final class ContentAvailability {

    public static final String FORCE_ALL_DISABLED_PROPERTY = "revivalages.test.forceAllContentDisabled";
    private static volatile ContentStateResolver<ContentKey> stateResolver;
    private static volatile Map<ResourceLocation, Set<ContentKey>> itemMemberships = Map.of();
    private static volatile Map<ResourceLocation, Set<ContentKey>> blockMemberships = Map.of();

    private ContentAvailability() {
    }

    public static void install(List<FeatureModule> modules) {
        EnumMap<ContentKey, BooleanSupplier> collectedDefinitions = new EnumMap<>(ContentKey.class);
        Map<ResourceLocation, Set<ContentKey>> collectedItems = new LinkedHashMap<>();
        Map<ResourceLocation, Set<ContentKey>> collectedBlocks = new LinkedHashMap<>();
        Set<String> moduleNames = new java.util.HashSet<>();

        for (FeatureModule module : modules) {
            ContentPolicy policy = module.contentPolicy();
            if (!moduleNames.add(policy.module())) {
                throw new IllegalStateException("Duplicate content policy module: " + policy.module());
            }
            for (ContentDefinition definition : policy.definitions()) {
                BooleanSupplier previous = collectedDefinitions.putIfAbsent(
                        definition.key(),
                        definition.configuredEnabled()
                );
                if (previous != null) {
                    throw new IllegalStateException("Duplicate content definition: " + definition.key().id());
                }
            }
            policy.itemMemberships().forEach((id, keys) -> collectedItems.compute(id, (ignored, existing) -> {
                EnumSet<ContentKey> merged = existing == null
                        ? EnumSet.noneOf(ContentKey.class)
                        : EnumSet.copyOf(existing);
                merged.addAll(keys);
                return Set.copyOf(merged);
            }));
            policy.blockMemberships().forEach((id, keys) -> collectedBlocks.compute(id, (ignored, existing) -> {
                EnumSet<ContentKey> merged = existing == null
                        ? EnumSet.noneOf(ContentKey.class)
                        : EnumSet.copyOf(existing);
                merged.addAll(keys);
                return Set.copyOf(merged);
            }));
        }

        EnumSet<ContentKey> missing = EnumSet.allOf(ContentKey.class);
        missing.removeAll(collectedDefinitions.keySet());
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing content definitions: " + missing);
        }
        validateParentGraph();
        EnumMap<ContentKey, Set<ContentKey>> parents = new EnumMap<>(ContentKey.class);
        for (ContentKey key : ContentKey.values()) {
            parents.put(key, Set.copyOf(key.parents()));
        }
        stateResolver = new ContentStateResolver<>(collectedDefinitions, parents);
        itemMemberships = Collections.unmodifiableMap(new LinkedHashMap<>(collectedItems));
        blockMemberships = Collections.unmodifiableMap(new LinkedHashMap<>(collectedBlocks));
    }

    public static boolean isEnabled(ContentKey key) {
        if (isForcedAllDisabled()) {
            return false;
        }
        ContentStateResolver<ContentKey> resolver = stateResolver;
        return resolver == null || resolver.isEnabled(key);
    }

    public static boolean isForcedAllDisabled() {
        return Boolean.getBoolean(FORCE_ALL_DISABLED_PROPERTY);
    }

    public static boolean hasDisabledContent() {
        for (ContentKey key : ContentKey.values()) {
            if (!isEnabled(key)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<Boolean> isEnabled(ResourceLocation id) {
        ContentKey key = ContentKey.fromId(id);
        return key == null ? Optional.empty() : Optional.of(isEnabled(key));
    }

    public static boolean isItemEnabled(Item item) {
        return isItemEnabled(BuiltInRegistries.ITEM.getKey(item));
    }

    public static boolean isItemEnabled(ResourceLocation itemId) {
        Set<ContentKey> keys = itemMemberships.get(itemId);
        return keys != null && keys.stream().anyMatch(ContentAvailability::isEnabled);
    }

    public static boolean isResultEnabled(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return !id.getNamespace().equals(RevivalAges.MOD_ID) || isItemEnabled(id);
    }

    public static boolean isBlockEnabled(Block block) {
        return isBlockEnabled(BuiltInRegistries.BLOCK.getKey(block));
    }

    public static boolean isBlockEnabled(ResourceLocation blockId) {
        Set<ContentKey> keys = blockKeys(blockId);
        return !keys.isEmpty() && keys.stream().anyMatch(ContentAvailability::isEnabled);
    }

    public static Set<ContentKey> itemKeys(ResourceLocation itemId) {
        return itemMemberships.getOrDefault(itemId, Set.of());
    }

    public static Set<ContentKey> blockKeys(ResourceLocation blockId) {
        Set<ContentKey> explicit = blockMemberships.get(blockId);
        return explicit == null ? itemKeys(blockId) : explicit;
    }

    public static void validateRegisteredContent() {
        List<ResourceLocation> missing = BuiltInRegistries.ITEM.entrySet().stream()
                .map(entry -> entry.getKey().location())
                .filter(id -> id.getNamespace().equals(RevivalAges.MOD_ID))
                .filter(id -> !itemMemberships.containsKey(id))
                .sorted()
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Public Revival Ages items without content policy: " + missing);
        }
        List<ResourceLocation> missingBlocks = BuiltInRegistries.BLOCK.entrySet().stream()
                .map(entry -> entry.getKey().location())
                .filter(id -> id.getNamespace().equals(RevivalAges.MOD_ID))
                .filter(id -> blockKeys(id).isEmpty())
                .sorted()
                .toList();
        if (!missingBlocks.isEmpty()) {
            throw new IllegalStateException("Revival Ages blocks without content policy: " + missingBlocks);
        }
    }

    public static void reportConflicts() {
        conflicts().forEach((key, disabledParents) -> RevivalAges.LOGGER.warn(
                "Content {} is configured enabled but remains disabled because these parents are disabled: {}",
                key.id(),
                disabledParents.stream().map(ContentKey::id).toList()
        ));
    }

    public static Map<ContentKey, List<ContentKey>> conflicts() {
        if (isForcedAllDisabled()) {
            return Map.of();
        }
        EnumMap<ContentKey, List<ContentKey>> conflicts = new EnumMap<>(ContentKey.class);
        for (ContentKey key : ContentKey.values()) {
            if (configuredEnabled(key)) {
                List<ContentKey> disabledParents = key.parents().stream()
                        .filter(parent -> !isEnabled(parent))
                        .toList();
                if (!disabledParents.isEmpty()) {
                    conflicts.put(key, disabledParents);
                }
            }
        }
        return Collections.unmodifiableMap(conflicts);
    }

    public static Map<ResourceLocation, Set<ContentKey>> itemMemberships() {
        return itemMemberships;
    }

    private static boolean configuredEnabled(ContentKey key) {
        ContentStateResolver<ContentKey> resolver = stateResolver;
        return resolver == null || resolver.configuredEnabled(key);
    }

    private static void validateParentGraph() {
        EnumMap<ContentKey, Set<ContentKey>> graph = new EnumMap<>(ContentKey.class);
        for (ContentKey key : ContentKey.values()) {
            graph.put(key, Set.copyOf(key.parents()));
        }
        ContentDependencyGraph.validate(graph);
    }
}
