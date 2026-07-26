package com.protyvkultury.revivalages.gametest;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.weight.WeightApi;
import com.protyvkultury.revivalages.api.weight.WeightResult;
import com.protyvkultury.revivalages.feature.inventory.carriedweight.CarriedWeightConfig;
import com.protyvkultury.revivalages.feature.inventory.carriedweight.CarriedWeightFeature;
import com.protyvkultury.revivalages.feature.inventory.carriedweight.CarriedWeightState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(RevivalAges.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CarriedWeightGameTests {

    private CarriedWeightGameTests() {
    }

    @GameTest(template = "animal_power_empty")
    public static void itemFormulaCountsRealStackQuantityAndPortableContents(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        helper.assertTrue(CarriedWeightConfig.ENABLED.getDefault(), "feature is not enabled by default");
        helper.assertValueEqual(CarriedWeightConfig.BASE_CAPACITY.getDefault(), 90_000.0D, "base capacity");
        helper.assertValueEqual(CarriedWeightConfig.POCKET_CAPACITY.getDefault(), 9_000.0D, "pocket capacity");
        helper.assertValueEqual(
                CarriedWeightConfig.CONTAINER_CONTENTS_MULTIPLIER.getDefault(),
                0.5D,
                "container coefficient"
        );
        helper.assertValueEqual(CarriedWeightConfig.MAXIMUM_RECURSION_DEPTH.getDefault(), 8, "recursion depth");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack one = new ItemStack(Items.IRON_INGOT);
        ItemStack sixteen = new ItemStack(Items.IRON_INGOT, 16);
        player.getInventory().setItem(0, sixteen);

        WeightResult unit = WeightApi.getWeight(one, player);
        WeightResult carried = WeightApi.getCarriedWeight(player);
        helper.assertTrue(unit.weight() > 0.0D, "ingot formula produced no weight");
        helper.assertValueEqual(
                carried.weight(),
                unit.weight() * sixteen.getCount(),
                "inventory stack quantity"
        );

        SimpleContainer contents = new SimpleContainer(new ItemStack(Items.IRON_INGOT, 4));
        ItemStack container = new ItemStack(Items.SHULKER_BOX);
        container.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents.getItems()));
        WeightResult containerWeight = WeightApi.getWeight(container, player);
        helper.assertTrue(containerWeight.contentsWeight() >= unit.weight() * 4,
                "portable container did not expose unmodified contents weight");
        helper.assertTrue(containerWeight.weight() < containerWeight.baseWeight()
                        + containerWeight.contentsWeight(),
                "portable container multiplier was not applied");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void armorPocketsAndInventorySourcesRemainIndependent(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack chestplate = new ItemStack(Items.IRON_CHESTPLATE);
        player.getInventory().armor.set(2, chestplate);
        double armorWeight = WeightApi.getWeight(chestplate, player).weight();

        helper.assertTrue(armorWeight > 0.0D, "armor formula produced no weight");
        helper.assertTrue(WeightApi.getPockets(chestplate, player).orElse(0) > 0,
                "vanilla armor did not provide pockets");
        helper.assertValueEqual(
                WeightApi.getCarriedWeight(player).weight(),
                armorWeight,
                "armor inventory source"
        );
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty", timeoutTicks = 40)
    public static void disabledFeatureKeepsRegistryIdentityAndClearsDerivedState(GameTestHelper helper) {
        if (!GameTestProfiles.requireDisabledContent(helper)) {
            return;
        }
        helper.assertTrue(CarriedWeightFeature.CARRY_CAPACITY_BONUS.isBound(),
                "disabled capacity attribute lost its registry identity");
        helper.assertTrue(CarriedWeightFeature.OVERLOADED.isBound(),
                "disabled overload effect lost its registry identity");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(0, new ItemStack(Items.BEDROCK, 64));
        helper.runAfterDelay(2, () -> {
            helper.assertFalse(WeightApi.enabled(), "disabled Carried Weight API remained enabled");
            helper.assertValueEqual(WeightApi.getCarriedWeight(player), WeightResult.ZERO,
                    "disabled carried weight");
            helper.assertValueEqual(CarriedWeightFeature.state(player), CarriedWeightState.EMPTY,
                    "disabled derived state");
            player.discard();
            helper.succeed();
        });
    }
}
