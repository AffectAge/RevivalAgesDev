package com.protyvkultury.revivalages.feature.player.diet;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.diet.DietApi;
import com.protyvkultury.revivalages.api.diet.DietContribution;
import com.protyvkultury.revivalages.api.diet.DietDetector;
import com.protyvkultury.revivalages.api.diet.DietEffectRule;
import com.protyvkultury.revivalages.api.diet.DietGroup;
import com.protyvkultury.revivalages.api.diet.DietMath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.network.PacketDistributor;

final class DietService implements DietApi.Runtime {

    @Override
    public boolean enabled() {
        return DietSettings.enabled();
    }

    @Override
    public Map<ResourceLocation, Double> values(net.minecraft.world.entity.player.Player player) {
        return DietFeature.state(player).values();
    }

    @Override
    public void set(net.minecraft.world.entity.player.Player player, ResourceLocation group, double value) {
        DietFeature.setState(player, DietFeature.state(player).withValue(group, value));
        if (player instanceof ServerPlayer serverPlayer) {
            sync(serverPlayer);
        }
    }

    void initializeGroups(ServerPlayer player) {
        DietState state = DietFeature.state(player);
        Map<ResourceLocation, Double> values = new LinkedHashMap<>(state.values());
        groups(player).forEach(entry -> values.putIfAbsent(
                entry.getKey().location(),
                DietConfig.STARTING_VALUE.get()
        ));
        DietState updated = new DietState(values, state.lastFoodLevel());
        DietFeature.setState(player, updated);
    }

    void consume(ServerPlayer player, ItemStack consumed) {
        if (!enabled() || consumed.isEmpty()) {
            return;
        }
        DietContribution contribution = DietApi.contribution(consumed).orElse(null);
        if (contribution == null) {
            return;
        }
        FoodProperties food = consumed.get(DataComponents.FOOD);
        double nutrition = food == null ? specialNutrition(consumed) : food.nutrition();
        if (nutrition <= 0.0D) {
            return;
        }
        int groupCount = contribution.groups().size();
        DietState updated = DietFeature.state(player);
        for (Map.Entry<ResourceLocation, Double> entry : contribution.groups().entrySet()) {
            double gain = DietMath.gain(
                    nutrition,
                    DietConfig.NUTRITION_MULTIPLIER.get(),
                    entry.getValue(),
                    DietConfig.MULTI_GROUP_REDUCTION.get(),
                    groupCount
            );
            double current = updated.values().getOrDefault(entry.getKey(), DietConfig.STARTING_VALUE.get());
            updated = updated.withValue(entry.getKey(), current + gain);
        }
        DietFeature.setState(player, updated.withFoodLevel(player.getFoodData().getFoodLevel()));
        refreshEffects(player);
        sync(player);
    }

    void tick(ServerPlayer player) {
        if (!enabled()) {
            player.removeEffect(DietFeature.DIET_TOUGHNESS);
            return;
        }
        initializeGroups(player);
        DietState state = DietFeature.state(player);
        int currentFood = player.getFoodData().getFoodLevel();
        if (state.lastFoodLevel() >= 0 && currentFood < state.lastFoodLevel()) {
            int lost = state.lastFoodLevel() - currentFood;
            Map<ResourceLocation, Double> changed = new LinkedHashMap<>(state.values());
            Map<ResourceLocation, DietGroup> groups = groupMap(player);
            changed.replaceAll((id, value) -> {
                DietGroup group = groups.get(id);
                double multiplier = group == null ? 1.0D : group.decayMultiplier();
                return DietMath.decay(value, lost, DietConfig.HUNGER_DECAY.get(), multiplier);
            });
            state = new DietState(changed, currentFood);
        } else {
            state = state.withFoodLevel(currentFood);
        }
        DietFeature.setState(player, state);
        if (player.tickCount % DietConfig.EFFECT_CADENCE.get() == 0) {
            refreshEffects(player);
            sync(player);
        }
    }

    void refreshEffects(ServerPlayer player) {
        if (!enabled()) {
            return;
        }
        DietState state = DietFeature.state(player);
        Registry<DietEffectRule> rules = player.registryAccess().registryOrThrow(DietFeature.DIET_EFFECT_RULES);
        rules.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().location()))
                .forEach(entry -> applyRule(player, state, entry.getKey().location(), entry.getValue()));
    }

    void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new DietStatePayload(
                DietFeature.state(player),
                DietSettings.localSnapshot()
        ));
    }

    private void applyRule(
            ServerPlayer player,
            DietState state,
            ResourceLocation ruleId,
            DietEffectRule rule
    ) {
        List<Double> values = selectedValues(player, state, rule.groups());
        if (values.isEmpty()) {
            return;
        }
        double minimum = configuredMinimum(ruleId, rule.minimum());
        double maximum = configuredMaximum(ruleId, rule.maximum());
        int amplifier = rule.amplifier();
        boolean matches = switch (rule.detector()) {
            case ANY -> values.stream().anyMatch(value -> within(value, minimum, maximum));
            case ALL -> values.stream().allMatch(value -> within(value, minimum, maximum));
            case AVERAGE -> within(
                    values.stream().mapToDouble(Double::doubleValue).average().orElse(-1.0D),
                    minimum,
                    maximum
            );
            case CUMULATIVE -> {
                int count = (int) values.stream().filter(value -> within(value, minimum, maximum)).count();
                amplifier = count * rule.cumulativeMultiplier() - 1;
                yield count > 0;
            }
        };
        if (matches) {
            player.addEffect(new MobEffectInstance(
                    rule.effect(),
                    DietConfig.EFFECT_DURATION.get(),
                    Math.clamp(amplifier, 0, 255),
                    true,
                    false,
                    true
            ));
        }
    }

    private List<Double> selectedValues(
            ServerPlayer player,
            DietState state,
            List<ResourceLocation> selected
    ) {
        List<ResourceLocation> ids = selected.isEmpty()
                ? groups(player).stream().map(entry -> entry.getKey().location()).toList()
                : selected;
        List<Double> values = new ArrayList<>(ids.size());
        ids.forEach(id -> values.add(state.values().getOrDefault(id, DietConfig.STARTING_VALUE.get())));
        return values;
    }

    private static boolean within(double value, double minimum, double maximum) {
        return value >= minimum && value <= maximum;
    }

    private static double configuredMinimum(ResourceLocation id, double fallback) {
        if (!RevivalAges.MOD_ID.equals(id.getNamespace())) {
            return fallback;
        }
        return switch (id.getPath()) {
            case "average_resistance" -> DietConfig.RESISTANCE_MINIMUM.get();
            case "average_strength" -> DietConfig.STRENGTH_MINIMUM.get();
            case "cumulative_toughness" -> DietConfig.TOUGHNESS_MINIMUM.get();
            default -> fallback;
        };
    }

    private static double configuredMaximum(ResourceLocation id, double fallback) {
        if (!RevivalAges.MOD_ID.equals(id.getNamespace())) {
            return fallback;
        }
        return switch (id.getPath()) {
            case "average_weakness" -> DietConfig.WEAKNESS_MAXIMUM.get();
            case "average_mining_fatigue" -> DietConfig.MINING_FATIGUE_MAXIMUM.get();
            default -> fallback;
        };
    }

    private static List<Map.Entry<ResourceKey<DietGroup>, DietGroup>> groups(ServerPlayer player) {
        return player.registryAccess()
                .registryOrThrow(DietFeature.DIET_GROUPS)
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().location()))
                .toList();
    }

    private static Map<ResourceLocation, DietGroup> groupMap(ServerPlayer player) {
        Map<ResourceLocation, DietGroup> values = new LinkedHashMap<>();
        groups(player).forEach(entry -> values.put(entry.getKey().location(), entry.getValue()));
        return values;
    }

    private static double specialNutrition(ItemStack stack) {
        if (stack.is(net.minecraft.world.item.Items.MILK_BUCKET)) {
            return DietSettings.milkNutrition();
        }
        if (stack.is(net.minecraft.world.item.Items.CAKE)) {
            return DietSettings.cakeSliceNutrition();
        }
        return 0.0D;
    }
}
