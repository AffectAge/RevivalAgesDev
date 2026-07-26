package com.protyvkultury.revivalages.gametest;

import com.protyvkultury.revivalages.RevivalAges;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.protyvkultury.revivalages.feature.technology.knapping.KnappingFeature;
import com.protyvkultury.revivalages.feature.technology.knapping.network.KnappingStatePayload;
import com.protyvkultury.revivalages.feature.technology.knapping.recipe.KnappingPattern;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralFallingBlockEntity;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityFeature;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityTags;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.SupportService;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.SupportIngredient;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.SupportDefinition;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.SupportWood;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.network.CollapseShakePayload;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import java.util.Map;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(RevivalAges.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StructuralKnappingGameTests {

    private static final BlockPos TEST_COLUMN = new BlockPos(4, 2, 4);

    private StructuralKnappingGameTests() {
    }

    @GameTest(template = "animal_power_empty")
    public static void allSupportMaterialsRemainRegistered(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        helper.assertValueEqual(
                StructuralIntegrityFeature.supportItems().size(),
                SupportWood.values().length,
                "support material count"
        );
        for (SupportWood wood : SupportWood.values()) {
            var item = StructuralIntegrityFeature.supportItems().get(wood);
            helper.assertTrue(item != null && item.isBound(), "missing support item for " + wood.serializedName());
            var standing = item.get().getBlock().defaultBlockState();
            helper.assertTrue(
                    standing.hasProperty(BlockStateProperties.WATERLOGGED),
                    "standing support is not waterloggable for " + wood.serializedName()
            );
        }
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void supportDataMapIsAvailableOnTheDedicatedServer(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        var vertical = StructuralIntegrityFeature.verticalSupport(SupportWood.OAK);
        var horizontal = StructuralIntegrityFeature.horizontalSupport(SupportWood.OAK);
        helper.assertTrue(
                SupportService.definition(horizontal.defaultBlockState()) != null,
                "reloadable support data map did not assign the horizontal beam"
        );
        helper.assertTrue(
                SupportService.definition(vertical.defaultBlockState()) == null,
                "vertical post incorrectly became a structural support source"
        );
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void supportIngredientMatchesSpecificBlockStates(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        SupportIngredient powered = new SupportIngredient(Map.of("powered", "true"));
        var unpowered = Blocks.LEVER.defaultBlockState();
        helper.assertTrue(!powered.test(unpowered), "state ingredient accepted the wrong property value");
        helper.assertTrue(
                powered.test(unpowered.setValue(BlockStateProperties.POWERED, true)),
                "state ingredient rejected the configured property value"
        );
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void supportDefinitionKeepsUpAndDownAsymmetric(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        SupportDefinition definition = new SupportDefinition(SupportIngredient.ANY, 1, 5, 0);
        BlockPos source = new BlockPos(4, 4, 4);
        helper.assertTrue(definition.supports(source, source.above()), "supportUp boundary was rejected");
        helper.assertTrue(!definition.supports(source, source.above(2)), "supportUp exceeded its boundary");
        helper.assertTrue(definition.supports(source, source.below(5)), "supportDown boundary was rejected");
        helper.assertTrue(!definition.supports(source, source.below(6)), "supportDown exceeded its boundary");
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void verticalSupportAutoStacksThreeAndSneakPlacesOne(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        var support = StructuralIntegrityFeature.verticalSupport(SupportWood.OAK);
        BlockPos base = new BlockPos(2, 2, 2);
        helper.setBlock(base.below(), Blocks.STONE);
        helper.setBlock(base, support);
        var placer = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(8, 2, 8));
        support.setPlacedBy(helper.getLevel(), helper.absolutePos(base), support.defaultBlockState(), placer,
                new ItemStack(support, 3));
        helper.assertBlockPresent(support, base.above(2));

        BlockPos sneakingBase = new BlockPos(6, 2, 2);
        helper.setBlock(sneakingBase.below(), Blocks.STONE);
        helper.setBlock(sneakingBase, support);
        placer.setShiftKeyDown(true);
        support.setPlacedBy(
                helper.getLevel(),
                helper.absolutePos(sneakingBase),
                support.defaultBlockState(),
                placer,
                new ItemStack(support, 3)
        );
        helper.assertBlockNotPresent(support, sneakingBase.above());
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void autoPlacementRejectsPlantsAndAcceptsWater(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        var support = StructuralIntegrityFeature.verticalSupport(SupportWood.OAK);
        var placer = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(8, 2, 8));
        BlockPos plantBase = new BlockPos(2, 2, 5);
        helper.setBlock(plantBase.below(), Blocks.STONE);
        helper.setBlock(plantBase, support);
        helper.setBlock(plantBase.above(), Blocks.POPPY);
        support.setPlacedBy(helper.getLevel(), helper.absolutePos(plantBase), support.defaultBlockState(), placer,
                new ItemStack(support, 3));
        helper.assertBlockPresent(Blocks.POPPY, plantBase.above());

        BlockPos waterBase = new BlockPos(6, 2, 5);
        helper.setBlock(waterBase.below(), Blocks.STONE);
        helper.setBlock(waterBase, support);
        helper.setBlock(waterBase.above(), Blocks.WATER);
        helper.setBlock(waterBase.above(2), Blocks.WATER);
        support.setPlacedBy(helper.getLevel(), helper.absolutePos(waterBase), support.defaultBlockState(), placer,
                new ItemStack(support, 3));
        helper.assertBlockPresent(support, waterBase.above(2));
        helper.assertTrue(
                helper.getBlockState(waterBase.above()).getValue(BlockStateProperties.WATERLOGGED),
                "support placed into water was not waterlogged"
        );
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void verticalSupportUsesCenterFoundationSemantics(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        var support = StructuralIntegrityFeature.verticalSupport(SupportWood.OAK);
        BlockPos pos = new BlockPos(4, 2, 4);
        helper.setBlock(pos.below(), Blocks.OAK_FENCE);
        helper.setBlock(pos, support);
        helper.assertBlockPresent(support, pos);
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void horizontalSpanBuildsFiveAndInvalidSegmentsDrop(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        var vertical = StructuralIntegrityFeature.verticalSupport(SupportWood.OAK);
        var horizontal = StructuralIntegrityFeature.horizontalSupport(SupportWood.OAK);
        var supportItem = StructuralIntegrityFeature.supportItems().get(SupportWood.OAK).get();
        BlockPos firstPost = new BlockPos(1, 2, 4);
        BlockPos secondPost = firstPost.east(6);
        helper.setBlock(firstPost, vertical);
        helper.setBlock(secondPost, vertical);
        BlockPos start = firstPost.east();
        helper.setBlock(start, horizontal);
        var placer = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(8, 2, 8));
        horizontal.setPlacedBy(
                helper.getLevel(),
                helper.absolutePos(start),
                horizontal.defaultBlockState(),
                placer,
                new ItemStack(horizontal, 5)
        );
        for (int offset = 1; offset <= 5; offset++) {
            helper.assertBlockPresent(horizontal, firstPost.east(offset));
        }

        int dropsBeforeBreak = countItems(helper, supportItem);
        helper.setBlock(firstPost, Blocks.AIR);
        for (int offset = 1; offset <= 5; offset++) {
            helper.assertBlockNotPresent(horizontal, firstPost.east(offset));
        }
        helper.assertTrue(
                countItems(helper, supportItem) == dropsBeforeBreak + 5,
                "five invalid span segments did not each drop one support"
        );
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void breakingPostBaseDropsDependentColumnAndSpan(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        var vertical = StructuralIntegrityFeature.verticalSupport(SupportWood.OAK);
        var horizontal = StructuralIntegrityFeature.horizontalSupport(SupportWood.OAK);
        var supportItem = StructuralIntegrityFeature.supportItems().get(SupportWood.OAK).get();
        BlockPos leftBase = new BlockPos(1, 2, 4);
        BlockPos rightBase = leftBase.east(6);
        for (int height = 0; height < 3; height++) {
            helper.setBlock(leftBase.above(height), vertical);
            helper.setBlock(rightBase.above(height), vertical);
        }
        BlockPos spanStart = leftBase.above(2).east();
        helper.setBlock(spanStart, horizontal);
        var placer = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(8, 2, 8));
        horizontal.setPlacedBy(
                helper.getLevel(),
                helper.absolutePos(spanStart),
                horizontal.defaultBlockState(),
                placer,
                new ItemStack(horizontal, 5)
        );
        for (int offset = 1; offset <= 5; offset++) {
            helper.assertBlockPresent(horizontal, leftBase.above(2).east(offset));
        }

        int dropsBeforeBreak = countItems(helper, supportItem);
        helper.setBlock(leftBase, Blocks.AIR);

        helper.assertBlockNotPresent(vertical, leftBase.above());
        helper.assertBlockNotPresent(vertical, leftBase.above(2));
        for (int offset = 1; offset <= 5; offset++) {
            helper.assertBlockNotPresent(horizontal, leftBase.above(2).east(offset));
        }
        helper.assertTrue(
                countItems(helper, supportItem) == dropsBeforeBreak + 7,
                "two upper post and five span segments did not each drop one support"
        );
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void builtInKnappingDataLoadsFromCanonicalRegistries(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        var registry = helper.getLevel().registryAccess().registryOrThrow(KnappingFeature.KNAPPING_TYPES);
        for (String name : new String[]{"rock", "clay", "leather", "horn"}) {
            helper.assertTrue(
                    registry.containsKey(RevivalAges.id(name)),
                    "missing knapping type " + name
            );
        }
        int recipes = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(KnappingFeature.RECIPE_TYPE.get())
                .size();
        helper.assertTrue(recipes >= 16, "expected built-in knapping recipes, found " + recipes);
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void unrelatedTerrainIsNotCollapsible(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        for (var block : new net.minecraft.world.level.block.Block[]{
                Blocks.CLAY,
                Blocks.DIRT,
                Blocks.SAND,
                Blocks.RED_SAND
        }) {
            var state = block.defaultBlockState();
            helper.assertTrue(
                    !state.is(StructuralIntegrityTags.CAN_TRIGGER_COLLAPSE)
                            && !state.is(StructuralIntegrityTags.CAN_START_COLLAPSE)
                            && !state.is(StructuralIntegrityTags.CAN_COLLAPSE),
                    "unrelated terrain entered collapse tags: " + block
            );
        }
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void knappingPatternAndFullGridPayloadPreserveCanonicalState(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        KnappingPattern pattern = KnappingPattern.CODEC
                .parse(JsonOps.INSTANCE, JsonParser.parseString("[\"#X.\"]"))
                .getOrThrow();
        helper.assertValueEqual(pattern.cells(), 0b111, "non-space pattern cells");

        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(
                Unpooled.buffer(),
                helper.getLevel().registryAccess(),
                net.neoforged.neoforge.network.connection.ConnectionType.OTHER
        );
        KnappingStatePayload original = new KnappingStatePayload(7, (1 << 25) - 1, 24);
        KnappingStatePayload.STREAM_CODEC.encode(buffer, original);
        KnappingStatePayload decoded = KnappingStatePayload.STREAM_CODEC.decode(buffer);
        helper.assertValueEqual(decoded.cells(), original.cells(), "25-bit knapping state");
        helper.assertValueEqual(decoded.acceptedCell(), 24, "bottom-right knapping cell");
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void collapseShakePayloadPreservesAtmosphereParameters(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(
                Unpooled.buffer(),
                helper.getLevel().registryAccess(),
                net.neoforged.neoforge.network.connection.ConnectionType.OTHER
        );
        CollapseShakePayload original = new CollapseShakePayload(
                new BlockPos(12, -24, 48),
                1.25F,
                36,
                48.0F
        );
        CollapseShakePayload.STREAM_CODEC.encode(buffer, original);
        CollapseShakePayload decoded = CollapseShakePayload.STREAM_CODEC.decode(buffer);
        helper.assertValueEqual(decoded, original, "collapse shake payload");
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty", timeoutTicks = 100)
    public static void fallingStructuralBlockPlacesItsResult(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        helper.setBlock(TEST_COLUMN.below(), Blocks.OBSIDIAN);
        braceLanding(helper, TEST_COLUMN);
        BlockPos source = helper.absolutePos(TEST_COLUMN.above(4));
        StructuralFallingBlockEntity.fall(
                helper.getLevel(),
                source,
                Blocks.COBBLESTONE.defaultBlockState(),
                2.0F,
                20
        );
        helper.runAfterDelay(60, () -> {
            helper.assertBlockPresent(Blocks.COBBLESTONE, TEST_COLUMN);
            helper.succeed();
        });
    }

    @GameTest(template = "animal_power_empty", timeoutTicks = 100)
    public static void fallingStructuralBlockStopsOnSturdyTerrain(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        helper.setBlock(TEST_COLUMN.below(), Blocks.OBSIDIAN);
        helper.setBlock(TEST_COLUMN, Blocks.CLAY);
        braceLanding(helper, TEST_COLUMN.above());
        BlockPos source = helper.absolutePos(TEST_COLUMN.above(4));
        StructuralFallingBlockEntity.fall(
                helper.getLevel(),
                source,
                Blocks.COBBLESTONE.defaultBlockState(),
                2.0F,
                20
        );
        helper.runAfterDelay(60, () -> {
            helper.assertBlockPresent(Blocks.CLAY, TEST_COLUMN);
            helper.assertBlockPresent(Blocks.COBBLESTONE, TEST_COLUMN.above());
            helper.succeed();
        });
    }

    @GameTest(template = "animal_power_empty", timeoutTicks = 100)
    public static void fallingStructuralBlockDoesNotDrillThroughDeepslate(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        helper.setBlock(TEST_COLUMN, Blocks.DEEPSLATE);
        braceLanding(helper, TEST_COLUMN.above());
        BlockPos source = helper.absolutePos(TEST_COLUMN.above(4));
        StructuralFallingBlockEntity.fall(
                helper.getLevel(),
                source,
                Blocks.COBBLESTONE.defaultBlockState(),
                2.0F,
                20
        );
        helper.runAfterDelay(60, () -> {
            helper.assertBlockPresent(Blocks.DEEPSLATE, TEST_COLUMN);
            helper.assertBlockPresent(Blocks.COBBLESTONE, TEST_COLUMN.above());
            helper.succeed();
        });
    }

    @GameTest(template = "animal_power_empty", timeoutTicks = 100)
    public static void fallingStructuralBlockBreaksNonSturdyObstacleOnce(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        helper.setBlock(TEST_COLUMN.below(), Blocks.OBSIDIAN);
        helper.setBlock(TEST_COLUMN, Blocks.SNOW);
        braceLanding(helper, TEST_COLUMN);
        BlockPos source = helper.absolutePos(TEST_COLUMN.above(4));
        StructuralFallingBlockEntity.fall(
                helper.getLevel(),
                source,
                Blocks.COBBLESTONE.defaultBlockState(),
                2.0F,
                20
        );
        helper.runAfterDelay(60, () -> {
            helper.assertBlockPresent(Blocks.COBBLESTONE, TEST_COLUMN);
            helper.succeed();
        });
    }

    private static void braceLanding(GameTestHelper helper, BlockPos landingPos) {
        helper.setBlock(landingPos.north(), Blocks.OBSIDIAN);
        helper.setBlock(landingPos.south(), Blocks.OBSIDIAN);
    }

    private static int countItems(GameTestHelper helper, net.minecraft.world.item.Item item) {
        return helper.getEntities(EntityType.ITEM).stream()
                .filter(entity -> entity.getItem().is(item))
                .mapToInt(entity -> entity.getItem().getCount())
                .sum();
    }
}
