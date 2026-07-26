package com.protyvkultury.revivalages.feature.food.spoilage;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
import com.protyvkultury.revivalages.api.food.FreshnessMath;
import com.protyvkultury.revivalages.api.food.FoodSpoilageProfile;
import com.protyvkultury.revivalages.api.food.FoodOutputPolicy;
import com.protyvkultury.revivalages.api.food.FoodState;
import com.protyvkultury.revivalages.api.food.FoodTrait;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.BundleContents;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class FoodFreshnessService implements FoodFreshnessApi.Runtime {

    public static final ResourceLocation DRIED = RevivalAges.id("dried");
    public static final ResourceLocation PRESERVED = RevivalAges.id("preserved");

    @Override
    public boolean enabled() {
        return FoodSpoilageSettings.remoteEnabled();
    }

    @Override
    public long now() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && server.overworld() != null) {
            return SpoilageClockData.get(server.overworld()).ticks();
        }
        return FoodSpoilageSettings.remoteTicks();
    }

    @Override
    public Optional<FoodState> state(ItemStack stack) {
        return Optional.ofNullable(stack.get(FoodSpoilageFeature.FOOD_STATE.get()));
    }

    @Override
    public long lifetime(ItemStack stack) {
        FoodSpoilageProfile profile = FoodFreshnessApi.profile(stack).orElse(null);
        if (profile == null) {
            return Long.MAX_VALUE;
        }
        double speed = profile.decayModifier() * FoodSpoilageSettings.globalMultiplier();
        FoodState state = stack.get(FoodSpoilageFeature.FOOD_STATE.get());
        if (state != null) {
            for (ResourceLocation trait : state.traits()) {
                speed *= traitMultiplier(trait);
            }
        }
        return FreshnessMath.lifetime(FoodSpoilageSettings.baseLifetime(), speed);
    }

    @Override
    public long remaining(ItemStack stack) {
        FoodState state = stack.get(FoodSpoilageFeature.FOOD_STATE.get());
        long lifetime = lifetime(stack);
        if (state == null || lifetime == Long.MAX_VALUE) {
            return lifetime;
        }
        return Math.max(0L, lifetime - Math.max(0L, now() - state.creationTick()));
    }

    @Override
    public boolean expired(ItemStack stack) {
        return enabled()
                && FoodFreshnessApi.profile(stack).isPresent()
                && state(stack).isPresent()
                && remaining(stack) <= 0L;
    }

    @Override
    public ItemStack initialize(ItemStack stack) {
        if (!enabled()
                || stack.isEmpty()
                || FoodFreshnessApi.profile(stack).isEmpty()
                || stack.has(FoodSpoilageFeature.FOOD_STATE.get())) {
            return stack;
        }
        stack.set(FoodSpoilageFeature.FOOD_STATE.get(), new FoodState(now(), List.of()));
        return stack;
    }

    @Override
    public ItemStack materialize(ItemStack stack) {
        return materialize(stack, 0);
    }

    private ItemStack materialize(ItemStack stack, int depth) {
        if (!enabled() || stack.isEmpty()) {
            return stack;
        }
        boolean needsFoodState = FoodFreshnessApi.profile(stack).isPresent()
                && !stack.has(FoodSpoilageFeature.FOOD_STATE.get());
        BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        int maximumDepth = FoodSpoilageConfig.SPEC.isLoaded()
                ? FoodSpoilageConfig.MAXIMUM_CONTAINER_DEPTH.get()
                : FoodSpoilageConfig.MAXIMUM_CONTAINER_DEPTH.getDefault();
        boolean hasInspectableContents = depth < maximumDepth && (bundle != null || contents != null);
        if (needsFoodState || hasInspectableContents) {
            stack = stack.copy();
        }
        initialize(stack);
        if (hasInspectableContents) {
            materializeContents(stack, bundle, contents, depth);
        }
        if (!expired(stack)) {
            return stack;
        }
        FoodSpoilageProfile profile = FoodFreshnessApi.profile(stack).orElseThrow();
        ItemStack result = profile.result().map(ItemStack::copy).orElseGet(() -> new ItemStack(Items.ROTTEN_FLESH));
        int count = stack.getCount();
        result.setCount(count);
        return result;
    }

    @Override
    public ItemStack copyOldest(ItemStack output, List<ItemStack> inputs) {
        if (output.isEmpty() || FoodFreshnessApi.profile(output).isEmpty()) {
            return output;
        }
        FoodState oldest = inputs.stream()
                .map(stack -> stack.get(FoodSpoilageFeature.FOOD_STATE.get()))
                .filter(java.util.Objects::nonNull)
                .min(Comparator.comparingLong(FoodState::creationTick))
                .orElse(null);
        if (oldest == null) {
            return initialize(output);
        }
        output.set(FoodSpoilageFeature.FOOD_STATE.get(), oldest);
        return output;
    }

    @Override
    public ItemStack transformOutput(ItemStack output, List<ItemStack> inputs, ResourceLocation recipeId) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        FoodOutputPolicy policy = server == null ? null : server.registryAccess()
                .registryOrThrow(FoodSpoilageFeature.FOOD_OUTPUT_POLICIES)
                .get(recipeId);
        if (policy == null) {
            return copyOldest(output, inputs);
        }
        switch (policy.mode()) {
            case COPY_FIRST -> inputs.stream()
                    .map(input -> input.get(FoodSpoilageFeature.FOOD_STATE.get()))
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .ifPresent(state -> output.set(FoodSpoilageFeature.FOOD_STATE.get(), state));
            case COPY_OLDEST -> copyOldest(output, inputs);
            case RESET -> {
                output.remove(FoodSpoilageFeature.FOOD_STATE.get());
                initialize(output);
            }
            case ADD_TRAIT -> {
                copyOldest(output, inputs);
                policy.traits().forEach(trait -> applyTrait(output, trait));
            }
            case REMOVE_TRAIT -> {
                copyOldest(output, inputs);
                policy.traits().forEach(trait -> removeTrait(output, trait));
            }
        }
        return output;
    }

    @Override
    public ItemStack applyTrait(ItemStack stack, ResourceLocation trait) {
        initialize(stack);
        FoodState old = stack.get(FoodSpoilageFeature.FOOD_STATE.get());
        if (old == null || old.traits().contains(trait)) {
            return stack;
        }
        double multiplier = traitMultiplier(trait);
        long adjusted = preserveRemainingFreshness(old.creationTick(), 1.0D / multiplier);
        List<ResourceLocation> traits = new ArrayList<>(old.traits());
        traits.add(trait);
        stack.set(FoodSpoilageFeature.FOOD_STATE.get(), new FoodState(adjusted, traits));
        return stack;
    }

    @Override
    public ItemStack removeTrait(ItemStack stack, ResourceLocation trait) {
        FoodState old = stack.get(FoodSpoilageFeature.FOOD_STATE.get());
        if (old == null || !old.traits().contains(trait)) {
            return stack;
        }
        long adjusted = preserveRemainingFreshness(old.creationTick(), traitMultiplier(trait));
        List<ResourceLocation> traits = new ArrayList<>(old.traits());
        traits.remove(trait);
        stack.set(FoodSpoilageFeature.FOOD_STATE.get(), new FoodState(adjusted, traits));
        return stack;
    }

    public boolean mayMerge(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty() || !first.is(second.getItem())) {
            return false;
        }
        FoodState a = first.get(FoodSpoilageFeature.FOOD_STATE.get());
        FoodState b = second.get(FoodSpoilageFeature.FOOD_STATE.get());
        if (a == null || b == null) {
            return a == b;
        }
        long window = FoodSpoilageConfig.SPEC.isLoaded()
                ? FoodSpoilageConfig.STACKING_WINDOW_TICKS.get()
                : FoodSpoilageConfig.STACKING_WINDOW_TICKS.getDefault();
        return FreshnessMath.mayStack(a, b, window);
    }

    public void inheritOldestAfterMerge(ItemStack target, ItemStack source) {
        FoodState a = target.get(FoodSpoilageFeature.FOOD_STATE.get());
        FoodState b = source.get(FoodSpoilageFeature.FOOD_STATE.get());
        if (a != null && b != null && b.creationTick() < a.creationTick()) {
            target.set(FoodSpoilageFeature.FOOD_STATE.get(), b);
        }
    }

    private long preserveRemainingFreshness(long oldCreation, double proportion) {
        return FreshnessMath.creationForPreservedFraction(now(), oldCreation, proportion);
    }

    private double traitMultiplier(ResourceLocation id) {
        if (DRIED.equals(id)) {
            return value(FoodSpoilageConfig.DRIED_MULTIPLIER);
        }
        if (PRESERVED.equals(id)) {
            return value(FoodSpoilageConfig.PRESERVED_MULTIPLIER);
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            FoodTrait trait = server.registryAccess()
                    .registryOrThrow(FoodSpoilageFeature.FOOD_TRAITS)
                    .get(id);
            if (trait != null) {
                return positive(trait.decayMultiplier());
            }
        }
        return 1.0D;
    }

    private void materializeContents(
            ItemStack stack,
            BundleContents bundle,
            ItemContainerContents contents,
            int depth
    ) {
        if (bundle != null) {
            List<ItemStack> bundleItems = new ArrayList<>();
            for (ItemStack original : bundle.itemsCopy()) {
                bundleItems.add(materialize(original, depth + 1));
            }
            stack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(bundleItems));
        }
        if (contents == null) {
            return;
        }
        NonNullList<ItemStack> replaced = NonNullList.withSize(contents.getSlots(), ItemStack.EMPTY);
        contents.copyInto(replaced);
        for (int slot = 0; slot < replaced.size(); slot++) {
            replaced.set(slot, materialize(replaced.get(slot), depth + 1));
        }
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(replaced));
    }

    private static double value(net.neoforged.neoforge.common.ModConfigSpec.DoubleValue setting) {
        return positive(FoodSpoilageConfig.SPEC.isLoaded() ? setting.get() : setting.getDefault());
    }

    private static double positive(double value) {
        return Double.isFinite(value) && value > 0.0D ? value : 1.0D;
    }
}
