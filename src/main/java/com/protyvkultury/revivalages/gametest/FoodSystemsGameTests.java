package com.protyvkultury.revivalages.gametest;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
import com.protyvkultury.revivalages.feature.food.spoilage.FoodFreshnessService;
import com.protyvkultury.revivalages.feature.food.spoilage.FoodSpoilageFeature;
import com.protyvkultury.revivalages.feature.food.spoilage.SpoilageClockData;
import com.protyvkultury.revivalages.api.food.FoodState;
import com.protyvkultury.revivalages.feature.player.diet.DietFeature;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(RevivalAges.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodSystemsGameTests {

    private FoodSystemsGameTests() {
    }

    @GameTest(template = "animal_power_empty", timeoutTicks = 40)
    public static void spoilageClockAdvancesWithoutPlayers(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        long initial = SpoilageClockData.get(helper.getLevel().getServer().overworld()).ticks();
        helper.runAfterDelay(5L, () -> {
            long current = SpoilageClockData.get(helper.getLevel().getServer().overworld()).ticks();
            helper.assertTrue(current >= initial + 5L, "spoilage clock did not advance on an empty server");
            helper.succeed();
        });
    }

    @GameTest(template = "animal_power_empty")
    public static void traitsPreserveFreshnessAndExpiredFoodMaterializes(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        ItemStack apple = new ItemStack(Items.APPLE);
        FoodFreshnessApi.initialize(apple);
        long originalLifetime = FoodFreshnessApi.lifetime(apple);
        FoodFreshnessApi.applyTrait(apple, FoodFreshnessService.DRIED);
        helper.assertTrue(FoodFreshnessApi.lifetime(apple) > originalLifetime,
                "dried trait did not extend lifetime");

        apple.set(
                FoodSpoilageFeature.FOOD_STATE.get(),
                new FoodState(
                        FoodFreshnessApi.now() - FoodFreshnessApi.lifetime(apple) - 1L,
                        FoodFreshnessApi.state(apple).orElseThrow().traits()
                )
        );
        ItemStack result = FoodFreshnessApi.materialize(apple);
        helper.assertTrue(result.is(Items.ROTTEN_FLESH), "expired food did not materialize");
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void registriesContainBuiltInDietAndFoodDefinitions(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        helper.assertValueEqual(
                helper.getLevel().registryAccess().registryOrThrow(DietFeature.DIET_GROUPS).size(),
                5,
                "built-in Diet group count"
        );
        helper.assertTrue(DietFeature.DIET_TOUGHNESS.isBound(), "Diet Toughness registry identity is missing");
        helper.assertTrue(FoodSpoilageFeature.FOOD_STATE.isBound(), "food state component identity is missing");
        helper.succeed();
    }
}
