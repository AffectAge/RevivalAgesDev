package com.protyvkultury.revivalages.feature.inventory.carriedweight;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.weight.CapacityModifier;
import com.protyvkultury.revivalages.api.weight.CapacityProvider;
import com.protyvkultury.revivalages.api.weight.CarriedWeightRegistrar;
import com.protyvkultury.revivalages.api.weight.ItemWeightDataMaps;
import com.protyvkultury.revivalages.api.weight.ItemWeightProvider;
import com.protyvkultury.revivalages.api.weight.PlayerWeightSource;
import com.protyvkultury.revivalages.api.weight.PocketProvider;
import com.protyvkultury.revivalages.api.weight.WeightApi;
import com.protyvkultury.revivalages.api.weight.WeightContext;
import com.protyvkultury.revivalages.api.weight.WeightLookup;
import com.protyvkultury.revivalages.api.weight.WeightResult;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

final class CarriedWeightService implements WeightApi.Runtime, WeightLookup, CarriedWeightRegistrar {

    private final PrioritizedProviderRegistry<ItemWeightProvider> itemProviders =
            new PrioritizedProviderRegistry<>();
    private final PrioritizedProviderRegistry<PlayerWeightSource> playerSources =
            new PrioritizedProviderRegistry<>();
    private final PrioritizedProviderRegistry<CapacityProvider> capacityProviders =
            new PrioritizedProviderRegistry<>();
    private final PrioritizedProviderRegistry<PocketProvider> pocketProviders =
            new PrioritizedProviderRegistry<>();
    private boolean frozen;

    CarriedWeightService() {
        registerDefaults();
    }

    void freeze() {
        itemProviders.freeze();
        playerSources.freeze();
        capacityProviders.freeze();
        pocketProviders.freeze();
        frozen = true;
    }

    @Override
    public WeightResult getWeight(ItemStack stack, Player player) {
        int maximumDepth = CarriedWeightSettings.snapshot().maximumRecursionDepth();
        return getWeight(stack, new WeightContext(
                player == null ? null : player.level(),
                player,
                0,
                maximumDepth
        ));
    }

    @Override
    public WeightResult getWeight(ItemStack stack, WeightContext context) {
        if (stack == null || stack.isEmpty() || context.recursionLimitExceeded()) {
            return WeightResult.ZERO;
        }
        if (stack.getItem() instanceof ItemWeightProvider provider) {
            Optional<WeightResult> direct = safeItemWeight(
                    "item implementation",
                    provider,
                    stack,
                    context
            );
            if (direct.isPresent()) {
                return direct.get();
            }
        }
        if (stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ItemWeightProvider provider) {
            Optional<WeightResult> direct = safeItemWeight(
                    "block implementation",
                    provider,
                    stack,
                    context
            );
            if (direct.isPresent()) {
                return direct.get();
            }
        }
        for (PrioritizedProviderRegistry.Entry<ItemWeightProvider> entry : itemProviders.entries()) {
            Optional<WeightResult> result = safeItemWeight(
                    entry.id().toString(),
                    entry.value(),
                    stack,
                    context
            );
            if (result.isPresent()) {
                return result.get();
            }
        }
        return WeightResult.ZERO;
    }

    @Override
    public WeightResult getCarriedWeight(Player player) {
        if (!enabled() || !frozen) {
            return WeightResult.ZERO;
        }
        WeightContext context = new WeightContext(
                player.level(),
                player,
                0,
                CarriedWeightSettings.snapshot().maximumRecursionDepth()
        );
        WeightResult total = WeightResult.ZERO;
        for (PrioritizedProviderRegistry.Entry<PlayerWeightSource> entry : playerSources.entries()) {
            try {
                WeightResult result = entry.value().getWeight(player, context, this);
                total = total.add(result == null ? WeightResult.ZERO : result);
            } catch (RuntimeException exception) {
                RevivalAges.LOGGER.error(
                        "Carried Weight source {} failed for player {}",
                        entry.id(),
                        player.getGameProfile().getName(),
                        exception
                );
            }
        }
        return total;
    }

    @Override
    public double getCapacity(ServerPlayer player) {
        if (!enabled() || !frozen) {
            return 0.0D;
        }
        double additive = CarriedWeightFeature.capacityBonus(player);
        double multiplier = 1.0D;
        for (PrioritizedProviderRegistry.Entry<CapacityProvider> entry : capacityProviders.entries()) {
            try {
                CapacityModifier modifier = entry.value().getCapacityModifier(player);
                if (modifier != null) {
                    additive += modifier.additive();
                    multiplier *= modifier.multiplier();
                }
            } catch (RuntimeException exception) {
                RevivalAges.LOGGER.error(
                        "Carried Weight capacity provider {} failed for player {}",
                        entry.id(),
                        player.getGameProfile().getName(),
                        exception
                );
            }
        }
        double capacity = (CarriedWeightSettings.snapshot().baseCapacity() + additive) * multiplier;
        return Double.isFinite(capacity) ? Math.max(1.0D, capacity) : 1.0D;
    }

    @Override
    public OptionalInt getPockets(ItemStack stack, Player wearer) {
        if (!enabled() || stack == null || stack.isEmpty()) {
            return OptionalInt.empty();
        }
        for (PrioritizedProviderRegistry.Entry<PocketProvider> entry : pocketProviders.entries()) {
            try {
                OptionalInt result = entry.value().getPockets(stack, wearer);
                if (result != null && result.isPresent()) {
                    return OptionalInt.of(Math.max(0, result.getAsInt()));
                }
            } catch (RuntimeException exception) {
                RevivalAges.LOGGER.error(
                        "Carried Weight pocket provider {} failed for {}",
                        entry.id(),
                        stack.getItemHolder().getRegisteredName(),
                        exception
                );
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public double getCurrentWeight(Player player) {
        return CarriedWeightFeature.state(player).currentWeight();
    }

    @Override
    public double getCurrentCapacity(Player player) {
        return CarriedWeightFeature.state(player).capacity();
    }

    @Override
    public boolean isOverloaded(Player player) {
        return CarriedWeightFeature.state(player).overloaded();
    }

    @Override
    public boolean enabled() {
        return CarriedWeightSettings.enabled();
    }

    @Override
    public void registerItemWeightProvider(ResourceLocation id, int priority, ItemWeightProvider provider) {
        itemProviders.register(id, priority, provider);
    }

    @Override
    public void registerPlayerWeightSource(ResourceLocation id, int priority, PlayerWeightSource source) {
        playerSources.register(id, priority, source);
    }

    @Override
    public void registerCapacityProvider(ResourceLocation id, int priority, CapacityProvider provider) {
        capacityProviders.register(id, priority, provider);
    }

    @Override
    public void registerPocketProvider(ResourceLocation id, int priority, PocketProvider provider) {
        pocketProviders.register(id, priority, provider);
    }

    private void registerDefaults() {
        registerItemWeightProvider(RevivalAges.id("data_map_weight"), 9_000, this::dataMapWeight);
        registerItemWeightProvider(RevivalAges.id("portable_container_weight"), 8_000, this::containerWeight);
        registerItemWeightProvider(RevivalAges.id("block_weight"), 1_000, this::blockWeight);
        registerItemWeightProvider(RevivalAges.id("item_weight"), 900, this::itemWeight);
        registerPlayerWeightSource(RevivalAges.id("vanilla_inventory_weight"), 1_000, this::inventoryWeight);
        registerCapacityProvider(RevivalAges.id("armor_pocket_capacity"), 1_000, this::armorCapacity);
        registerPocketProvider(RevivalAges.id("data_map_pockets"), 9_000, this::dataMapPockets);
        registerPocketProvider(RevivalAges.id("vanilla_armor_pockets"), 1_000, this::armorPockets);
    }

    private Optional<WeightResult> dataMapWeight(
            ItemStack stack,
            WeightContext context,
            WeightLookup lookup
    ) {
        Double value = stack.getItemHolder().getData(ItemWeightDataMaps.ITEM_WEIGHT);
        return value == null ? Optional.empty() : Optional.of(WeightResult.of(value));
    }

    private Optional<WeightResult> containerWeight(
            ItemStack stack,
            WeightContext context,
            WeightLookup lookup
    ) {
        Iterable<ItemStack> contents = componentContents(stack);
        if (contents != null) {
            return Optional.of(calculateContainer(contents, context, lookup));
        }
        IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (handler == null) {
            return Optional.empty();
        }
        double inside = 0.0D;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack contained = handler.getStackInSlot(slot);
            inside += stackWeight(contained, context.nested(), lookup);
        }
        return Optional.of(containerResult(inside));
    }

    private Optional<WeightResult> blockWeight(
            ItemStack stack,
            WeightContext context,
            WeightLookup lookup
    ) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return Optional.empty();
        }
        return Optional.of(WeightResult.of(
                WeightFormula.blockWeight(blockItem, stack, CarriedWeightSettings.snapshot().formula())
        ));
    }

    private Optional<WeightResult> itemWeight(
            ItemStack stack,
            WeightContext context,
            WeightLookup lookup
    ) {
        return Optional.of(WeightResult.of(
                WeightFormula.itemWeight(stack, CarriedWeightSettings.snapshot().formula())
        ));
    }

    private WeightResult inventoryWeight(Player player, WeightContext context, WeightLookup lookup) {
        WeightResult total = WeightResult.ZERO;
        for (ItemStack stack : player.getInventory().items) {
            total = total.add(lookup.getWeight(stack, context).multiply(stack.getCount()));
        }
        for (ItemStack stack : player.getInventory().offhand) {
            total = total.add(lookup.getWeight(stack, context).multiply(stack.getCount()));
        }
        for (ItemStack stack : player.getInventory().armor) {
            total = total.add(lookup.getWeight(stack, context).multiply(stack.getCount()));
        }
        return total;
    }

    private CapacityModifier armorCapacity(ServerPlayer player) {
        int pockets = 0;
        for (ItemStack stack : player.getInventory().armor) {
            pockets += getPockets(stack, player).orElse(0);
        }
        return CapacityModifier.additive(pockets * CarriedWeightSettings.snapshot().pocketCapacity());
    }

    private OptionalInt dataMapPockets(ItemStack stack, Player wearer) {
        Integer pockets = stack.getItemHolder().getData(ItemWeightDataMaps.POCKETS);
        return pockets == null ? OptionalInt.empty() : OptionalInt.of(pockets);
    }

    private OptionalInt armorPockets(ItemStack stack, Player wearer) {
        return stack.getItem() instanceof ArmorItem armor
                ? OptionalInt.of(WeightFormula.armorPockets(armor))
                : OptionalInt.empty();
    }

    private WeightResult calculateContainer(
            Iterable<ItemStack> contents,
            WeightContext context,
            WeightLookup lookup
    ) {
        double inside = 0.0D;
        for (ItemStack contained : contents) {
            inside += stackWeight(contained, context.nested(), lookup);
        }
        return containerResult(inside);
    }

    private WeightResult containerResult(double inside) {
        WeightFormulaSettings formula = CarriedWeightSettings.snapshot().formula();
        double empty = formula.itemWeight();
        return WeightResult.container(
                empty + inside * formula.containerContentsMultiplier(),
                empty,
                inside
        );
    }

    private static double stackWeight(ItemStack stack, WeightContext context, WeightLookup lookup) {
        if (stack == null || stack.isEmpty()) {
            return 0.0D;
        }
        return lookup.getWeight(stack, context).multiply(stack.getCount()).weight();
    }

    private static Iterable<ItemStack> componentContents(ItemStack stack) {
        if (stack.has(DataComponents.BUNDLE_CONTENTS)) {
            BundleContents contents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            return contents.items();
        }
        if (stack.has(DataComponents.CONTAINER)) {
            ItemContainerContents contents = stack.getOrDefault(
                    DataComponents.CONTAINER,
                    ItemContainerContents.EMPTY
            );
            return contents.nonEmptyItems();
        }
        return null;
    }

    private Optional<WeightResult> safeItemWeight(
            String source,
            ItemWeightProvider provider,
            ItemStack stack,
            WeightContext context
    ) {
        try {
            Optional<WeightResult> result = provider.getWeight(stack, context, this);
            if (result == null || result.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(result.get().sanitized());
        } catch (RuntimeException exception) {
            RevivalAges.LOGGER.error(
                    "Carried Weight item provider {} failed for {}",
                    source,
                    stack.getItemHolder().getRegisteredName(),
                    exception
            );
            return Optional.empty();
        }
    }
}
