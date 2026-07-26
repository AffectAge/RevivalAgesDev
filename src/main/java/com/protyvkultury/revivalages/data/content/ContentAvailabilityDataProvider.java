package com.protyvkultury.revivalages.data.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import java.io.IOException;
import java.io.Reader;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Validates all normal acquisition resources and emits a deterministic review
 * manifest. The provider makes missing content gates fail {@code runData}.
 */
public final class ContentAvailabilityDataProvider implements DataProvider {

    private static final Set<String> LEGACY_CONDITIONS = Set.of(
            "revivalages:knapping_enabled",
            "revivalages:construction_frame_enabled",
            "revivalages:structural_enabled"
    );

    private final PackOutput.PathProvider output;
    private final ResourceManager resources;

    public ContentAvailabilityDataProvider(PackOutput output, ResourceManager resources) {
        this.output = output.createPathProvider(PackOutput.Target.DATA_PACK, "content_availability");
        this.resources = resources;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        JsonObject manifest = new JsonObject();
        JsonArray keys = new JsonArray();
        for (ContentKey key : ContentKey.values()) {
            keys.add(key.id().toString());
        }
        manifest.add("content_keys", keys);

        JsonObject gatedResources = new JsonObject();
        gatedResources.add("recipes", validateConditionedResources("recipe"));
        gatedResources.add("loot_tables", validateConditionedResources("loot_table"));
        gatedResources.add("biome_modifiers", validateBiomeModifiers());
        manifest.add("gated_resources", gatedResources);

        return DataProvider.saveStable(
                cachedOutput,
                manifest,
                output.json(RevivalAges.id("manifest"))
        );
    }

    @Override
    public String getName() {
        return "Revival Ages content availability manifest";
    }

    private JsonArray validateConditionedResources(String prefix) {
        List<Map.Entry<ResourceLocation, Resource>> found = resources.listResources(
                prefix,
                id -> id.getNamespace().equals(RevivalAges.MOD_ID) && id.getPath().endsWith(".json")
        ).entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .toList();
        if (found.isEmpty()) {
            throw new IllegalStateException("No Revival Ages resources found under " + prefix);
        }

        JsonArray ids = new JsonArray();
        for (Map.Entry<ResourceLocation, Resource> entry : found) {
            JsonObject root = read(entry);
            JsonArray conditions = root.has("neoforge:conditions")
                    ? root.getAsJsonArray("neoforge:conditions")
                    : null;
            if (conditions == null || conditions.isEmpty()) {
                throw new IllegalStateException(entry.getKey() + " has no content availability condition");
            }
            boolean gated = false;
            for (JsonElement element : conditions) {
                if (element.isJsonObject() && validateCondition(element.getAsJsonObject(), entry.getKey())) {
                    gated = true;
                }
            }
            if (!gated) {
                throw new IllegalStateException(entry.getKey() + " has no recognized content availability condition");
            }
            ids.add(entry.getKey().toString());
        }
        return ids;
    }

    private JsonArray validateBiomeModifiers() {
        List<Map.Entry<ResourceLocation, Resource>> found = resources.listResources(
                "neoforge/biome_modifier",
                id -> id.getNamespace().equals(RevivalAges.MOD_ID) && id.getPath().endsWith(".json")
        ).entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .toList();
        if (found.isEmpty()) {
            throw new IllegalStateException("No Revival Ages biome modifiers found");
        }

        JsonArray ids = new JsonArray();
        for (Map.Entry<ResourceLocation, Resource> entry : found) {
            JsonObject root = read(entry);
            requireKnownContentId(root.get("content"), entry.getKey());
            ids.add(entry.getKey().toString());
        }
        return ids;
    }

    private static boolean validateCondition(JsonObject condition, ResourceLocation owner) {
        String type = condition.has("type") ? condition.get("type").getAsString() : "";
        if (LEGACY_CONDITIONS.contains(type)) {
            return true;
        }
        if (type.equals("revivalages:content_enabled")) {
            requireKnownContentId(condition.get("content"), owner);
            return true;
        }
        if (type.equals("revivalages:any_content_enabled")) {
            JsonArray contents = condition.has("contents") ? condition.getAsJsonArray("contents") : null;
            if (contents == null || contents.isEmpty()) {
                throw new IllegalStateException(owner + " has an empty any_content_enabled condition");
            }
            for (JsonElement content : contents) {
                requireKnownContentId(content, owner);
            }
            return true;
        }
        return false;
    }

    private static void requireKnownContentId(JsonElement element, ResourceLocation owner) {
        if (element == null || !element.isJsonPrimitive()) {
            throw new IllegalStateException(owner + " has no content key");
        }
        ResourceLocation id = ResourceLocation.tryParse(element.getAsString());
        if (id == null || ContentKey.fromId(id) == null) {
            throw new IllegalStateException(owner + " has unknown content key " + element);
        }
    }

    private static JsonObject read(Map.Entry<ResourceLocation, Resource> entry) {
        try (Reader reader = entry.getValue().openAsReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Cannot read " + entry.getKey(), exception);
        }
    }
}
