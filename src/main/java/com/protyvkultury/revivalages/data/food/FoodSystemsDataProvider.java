package com.protyvkultury.revivalages.data.food;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.diet.DietContribution;
import com.protyvkultury.revivalages.api.diet.DietDataMaps;
import com.protyvkultury.revivalages.api.food.FoodSpoilageDataMaps;
import com.protyvkultury.revivalages.api.food.FoodSpoilageProfile;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class FoodSystemsDataProvider extends DataMapProvider {

    private static final ResourceLocation DAIRY = RevivalAges.id("dairy");
    private static final ResourceLocation FRUIT = RevivalAges.id("fruit");
    private static final ResourceLocation GRAIN = RevivalAges.id("grain");
    private static final ResourceLocation PROTEIN = RevivalAges.id("protein");
    private static final ResourceLocation VEGETABLE = RevivalAges.id("vegetable");

    public FoodSystemsDataProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider
    ) {
        super(output, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        DataMapProvider.Builder<DietContribution, Item> diet = builder(DietDataMaps.ITEM_DIET);
        addDiet(diet, DAIRY, Items.MILK_BUCKET);
        addDiet(diet, FRUIT,
                Items.APPLE, Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.MELON_SLICE,
                Items.CHORUS_FRUIT, Items.GOLDEN_APPLE, Items.HONEY_BOTTLE);
        addDiet(diet, GRAIN, Items.WHEAT, Items.BREAD, Items.COOKIE);
        addDiet(diet, PROTEIN,
                Items.BEEF, Items.COOKED_BEEF, Items.PORKCHOP, Items.COOKED_PORKCHOP,
                Items.MUTTON, Items.COOKED_MUTTON, Items.CHICKEN, Items.COOKED_CHICKEN,
                Items.RABBIT, Items.COOKED_RABBIT, Items.COD, Items.COOKED_COD,
                Items.SALMON, Items.COOKED_SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH,
                Items.EGG, Items.SPIDER_EYE);
        addDiet(diet, VEGETABLE,
                Items.CARROT, Items.GOLDEN_CARROT, Items.POTATO, Items.POISONOUS_POTATO,
                Items.BAKED_POTATO, Items.BEETROOT,
                Items.DRIED_KELP, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM);
        addDiet(diet, map(GRAIN, 1.0D, VEGETABLE, 1.0D), Items.PUMPKIN_PIE);
        addDiet(diet, map(PROTEIN, 1.0D, VEGETABLE, 1.0D), Items.RABBIT_STEW);
        addDiet(diet, VEGETABLE, Items.SUSPICIOUS_STEW, Items.MUSHROOM_STEW, Items.BEETROOT_SOUP);
        addDiet(diet, map(DAIRY, 1.0D, GRAIN, 1.0D), Items.CAKE);
        addDiet(diet, map(DAIRY, 1.0D, FRUIT, 1.0D, GRAIN, 1.0D, PROTEIN, 1.0D, VEGETABLE, 1.0D),
                Items.ENCHANTED_GOLDEN_APPLE);

        DataMapProvider.Builder<FoodSpoilageProfile, Item> spoilage =
                builder(FoodSpoilageDataMaps.ITEM_SPOILAGE);
        addSpoilage(spoilage, 1.7D, Items.APPLE, Items.CHORUS_FRUIT);
        addSpoilage(spoilage, 4.9D, Items.SWEET_BERRIES, Items.GLOW_BERRIES);
        addSpoilage(spoilage, 2.5D, Items.MELON_SLICE, Items.PUMPKIN_PIE);
        addSpoilage(spoilage, 2.0D, Items.WHEAT);
        addSpoilage(spoilage, 1.0D, Items.BREAD);
        addSpoilage(spoilage, 0.5D, Items.SUGAR, Items.CARROT, Items.BEETROOT);
        addSpoilage(spoilage, 0.666D, Items.POTATO, Items.POISONOUS_POTATO);
        addSpoilage(spoilage, 1.0D, Items.BAKED_POTATO);
        addSpoilage(spoilage, 2.5D, Items.DRIED_KELP);
        addSpoilage(spoilage, 2.0D, Items.BEEF, Items.PORKCHOP, Items.EGG);
        addSpoilage(spoilage, 0.5D, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM);
        addSpoilage(spoilage, 3.0D,
                Items.MUTTON, Items.CHICKEN, Items.RABBIT, Items.COD, Items.SALMON,
                Items.TROPICAL_FISH, Items.PUFFERFISH, Items.SPIDER_EYE);
        addSpoilage(spoilage, 1.5D, Items.COOKED_BEEF, Items.COOKED_PORKCHOP);
        addSpoilage(spoilage, 2.25D,
                Items.COOKED_MUTTON, Items.COOKED_CHICKEN, Items.COOKED_RABBIT,
                Items.COOKED_COD, Items.COOKED_SALMON);
        addSpoilage(spoilage, 4.5D,
                Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP,
                Items.SUSPICIOUS_STEW, Items.CAKE, Items.COOKIE, Items.MILK_BUCKET);
    }

    @Override
    public String getName() {
        return "Revival Ages Diet and Food Spoilage Data Maps";
    }

    private static void addDiet(
            DataMapProvider.Builder<DietContribution, Item> builder,
            ResourceLocation group,
            Item... items
    ) {
        addDiet(builder, Map.of(group, 1.0D), items);
    }

    private static void addDiet(
            DataMapProvider.Builder<DietContribution, Item> builder,
            Map<ResourceLocation, Double> groups,
            Item... items
    ) {
        DietContribution value = new DietContribution(groups);
        for (Item item : items) {
            builder.add(BuiltInRegistries.ITEM.getKey(item), value, true);
        }
    }

    private static void addSpoilage(
            DataMapProvider.Builder<FoodSpoilageProfile, Item> builder,
            double modifier,
            Item... items
    ) {
        FoodSpoilageProfile value = new FoodSpoilageProfile(modifier, Optional.empty());
        for (Item item : items) {
            builder.add(BuiltInRegistries.ITEM.getKey(item), value, true);
        }
    }

    private static Map<ResourceLocation, Double> map(Object... values) {
        Map<ResourceLocation, Double> result = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            result.put((ResourceLocation) values[index], (Double) values[index + 1]);
        }
        return Map.copyOf(result);
    }
}
