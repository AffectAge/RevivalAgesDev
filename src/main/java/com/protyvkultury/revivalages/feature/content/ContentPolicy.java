package com.protyvkultury.revivalages.feature.content;

import com.protyvkultury.revivalages.RevivalAges;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import net.minecraft.resources.ResourceLocation;

/**
 * Availability declaration owned by one feature module.
 *
 * @param module stable diagnostic name
 * @param infrastructure whether the module is mandatory infrastructure
 * @param definitions configuration-backed content definitions
 * @param itemMemberships public item IDs and the content keys that may expose them
 * @param blockMemberships blocks without a same-ID public item and their content keys
 */
public record ContentPolicy(
        String module,
        boolean infrastructure,
        List<ContentDefinition> definitions,
        Map<ResourceLocation, Set<ContentKey>> itemMemberships,
        Map<ResourceLocation, Set<ContentKey>> blockMemberships
) {

    public ContentPolicy {
        Objects.requireNonNull(module, "module");
        definitions = List.copyOf(definitions);
        Map<ResourceLocation, Set<ContentKey>> immutableMemberships = new LinkedHashMap<>();
        itemMemberships.forEach((id, keys) -> immutableMemberships.put(id, Set.copyOf(keys)));
        itemMemberships = Collections.unmodifiableMap(immutableMemberships);
        Map<ResourceLocation, Set<ContentKey>> immutableBlocks = new LinkedHashMap<>();
        blockMemberships.forEach((id, keys) -> immutableBlocks.put(id, Set.copyOf(keys)));
        blockMemberships = Collections.unmodifiableMap(immutableBlocks);
        if (infrastructure && (!definitions.isEmpty() || !itemMemberships.isEmpty() || !blockMemberships.isEmpty())) {
            throw new IllegalArgumentException("Infrastructure module " + module + " cannot own gameplay content");
        }
    }

    public static ContentPolicy infrastructure(String module) {
        return new ContentPolicy(module, true, List.of(), Map.of(), Map.of());
    }

    public static Builder gameplay(String module) {
        return new Builder(module);
    }

    public static final class Builder {

        private final String module;
        private final List<ContentDefinition> definitions = new ArrayList<>();
        private final Map<ResourceLocation, Set<ContentKey>> itemMemberships = new LinkedHashMap<>();
        private final Map<ResourceLocation, Set<ContentKey>> blockMemberships = new LinkedHashMap<>();

        private Builder(String module) {
            this.module = Objects.requireNonNull(module, "module");
        }

        public Builder define(ContentKey key, BooleanSupplier configuredEnabled) {
            definitions.add(new ContentDefinition(key, configuredEnabled));
            return this;
        }

        public Builder items(ContentKey key, String... paths) {
            return sharedItems(Set.of(key), paths);
        }

        public Builder sharedItems(Set<ContentKey> keys, String... paths) {
            addMemberships(itemMemberships, keys, paths);
            return this;
        }

        public Builder blocks(ContentKey key, String... paths) {
            addMemberships(blockMemberships, Set.of(key), paths);
            return this;
        }

        private static void addMemberships(
                Map<ResourceLocation, Set<ContentKey>> target,
                Set<ContentKey> keys,
                String... paths
        ) {
            if (keys.isEmpty()) {
                throw new IllegalArgumentException("Content must belong to at least one content key");
            }
            for (String path : paths) {
                ResourceLocation id = RevivalAges.id(path);
                target.compute(id, (ignored, existing) -> {
                    EnumSet<ContentKey> merged = existing == null
                            ? EnumSet.noneOf(ContentKey.class)
                            : EnumSet.copyOf(existing);
                    merged.addAll(keys);
                    return merged;
                });
            }
        }

        public ContentPolicy build() {
            if (definitions.isEmpty() && itemMemberships.isEmpty() && blockMemberships.isEmpty()) {
                throw new IllegalStateException("Gameplay module " + module + " declares no content");
            }
            return new ContentPolicy(module, false, definitions, itemMemberships, blockMemberships);
        }
    }
}
