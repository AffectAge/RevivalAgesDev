package com.protyvkultury.revivalages.gametest;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentEnabledCondition;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.animalpower.view.AnimalPowerRecipeCatalog;
import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import com.protyvkultury.revivalages.feature.technology.barrel.blockentity.BarrelBlockEntity;
import com.protyvkultury.revivalages.feature.technology.constructionframe.FrameEnabledCondition;
import com.protyvkultury.revivalages.feature.technology.constructionframe.view.FrameAssemblyRecipeCatalog;
import com.protyvkultury.revivalages.feature.technology.dryingrack.view.DryingRecipeCatalog;
import com.protyvkultury.revivalages.feature.technology.knapping.KnappingEnabledCondition;
import com.protyvkultury.revivalages.feature.technology.knapping.view.KnappingRecipeCatalog;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeCatalog;
import com.protyvkultury.revivalages.feature.technology.stonemachine.view.StoneTechnologyRecipeCatalog;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralEnabledCondition;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(RevivalAges.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ContentAvailabilityGameTests {

    private static final BlockPos MACHINE = new BlockPos(4, 2, 4);

    private ContentAvailabilityGameTests() {
    }

    @GameTest(template = "animal_power_empty")
    public static void allEnabledProfileExposesEveryContentKey(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        ContentAvailability.validateRegisteredContent();
        for (ContentKey key : ContentKey.values()) {
            helper.assertTrue(ContentAvailability.isEnabled(key), key.id() + " is not enabled by default");
        }
        helper.assertTrue(
                new ItemStack(PrimitiveMaterialsFeature.TANNIN_BUCKET.get())
                        .getCapability(Capabilities.FluidHandler.ITEM) != null,
                "enabled tannin bucket lost its fluid capability"
        );
        assertViewerCatalogsAvailable(helper);
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void allDisabledProfileKeepsRegistryAndRemovesAcquisition(GameTestHelper helper) {
        if (!GameTestProfiles.requireDisabledContent(helper)) {
            return;
        }

        ContentAvailability.validateRegisteredContent();
        for (ContentKey key : ContentKey.values()) {
            helper.assertFalse(ContentAvailability.isEnabled(key), key.id() + " remained enabled");
        }
        helper.assertTrue(
                new ContentEnabledCondition(ContentKey.BARREL.id(), false).test(null),
                "generic content condition did not use effective disabled state"
        );
        helper.assertTrue(new KnappingEnabledCondition(false).test(null), "legacy knapping condition diverged");
        helper.assertTrue(new FrameEnabledCondition(false).test(null), "legacy frame condition diverged");
        helper.assertTrue(
                new StructuralEnabledCondition(
                        StructuralEnabledCondition.Scope.SUPPORT_BEAMS,
                        false
                ).test(null),
                "legacy structural condition diverged"
        );
        BuiltInRegistries.ITEM.entrySet().stream()
                .map(entry -> entry.getKey().location())
                .filter(id -> id.getNamespace().equals(RevivalAges.MOD_ID))
                .forEach(id -> helper.assertFalse(
                        ContentAvailability.isItemEnabled(id),
                        id + " is registered but still available"
                ));
        BuiltInRegistries.BLOCK.entrySet().stream()
                .map(entry -> entry.getKey().location())
                .filter(id -> id.getNamespace().equals(RevivalAges.MOD_ID))
                .forEach(id -> helper.assertFalse(
                        ContentAvailability.isBlockEnabled(id),
                        id + " is registered but still active"
                ));

        helper.getLevel().getRecipeManager().getRecipes().forEach(holder -> {
            var typeId = BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType());
            helper.assertFalse(
                    typeId != null && typeId.getNamespace().equals(RevivalAges.MOD_ID),
                    holder.id() + " loaded a disabled custom recipe type"
            );
            ItemStack result = holder.value().getResultItem(helper.getLevel().registryAccess());
            var resultId = BuiltInRegistries.ITEM.getKey(result.getItem());
            helper.assertFalse(
                    !result.isEmpty() && resultId.getNamespace().equals(RevivalAges.MOD_ID),
                    holder.id() + " still acquires disabled result " + resultId
            );
        });
        assertViewerCatalogsEmpty(helper);
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void everyDisabledItemInteractionIsRejected(GameTestHelper helper) {
        if (!GameTestProfiles.requireDisabledContent(helper)) {
            return;
        }
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos target = helper.absolutePos(MACHINE.below());
        BuiltInRegistries.ITEM.entrySet().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(RevivalAges.MOD_ID))
                .forEach(entry -> {
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(entry.getValue()));
                    PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(
                            player,
                            InteractionHand.MAIN_HAND,
                            target,
                            new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target, false)
                    );
                    NeoForge.EVENT_BUS.post(event);
                    helper.assertTrue(
                            event.isCanceled(),
                            "disabled item interaction was not rejected for " + entry.getKey().location()
                    );
                });
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void everyDisabledCapabilityIsNull(GameTestHelper helper) {
        if (!GameTestProfiles.requireDisabledContent(helper)) {
            return;
        }
        BuiltInRegistries.ITEM.entrySet().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(RevivalAges.MOD_ID))
                .forEach(entry -> {
                    ItemStack stack = new ItemStack(entry.getValue());
                    helper.assertTrue(
                            stack.getCapability(Capabilities.FluidHandler.ITEM) == null,
                            entry.getKey().location() + " exposed an item fluid capability"
                    );
                    helper.assertTrue(
                            stack.getCapability(Capabilities.ItemHandler.ITEM) == null,
                            entry.getKey().location() + " exposed an item inventory capability"
                    );
                });

        BlockPos absolute = helper.absolutePos(MACHINE);
        BuiltInRegistries.BLOCK.entrySet().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(RevivalAges.MOD_ID))
                .forEach(entry -> {
                    helper.getLevel().setBlock(absolute, entry.getValue().defaultBlockState(), 2);
                    for (Direction direction : Direction.values()) {
                        helper.assertTrue(
                                helper.getLevel().getCapability(
                                        Capabilities.FluidHandler.BLOCK,
                                        absolute,
                                        direction
                                ) == null,
                                entry.getKey().location() + " exposed a block fluid capability"
                        );
                        helper.assertTrue(
                                helper.getLevel().getCapability(
                                        Capabilities.ItemHandler.BLOCK,
                                        absolute,
                                        direction
                                ) == null,
                                entry.getKey().location() + " exposed a block inventory capability"
                        );
                    }
                    helper.getLevel().setBlock(absolute, Blocks.AIR.defaultBlockState(), 2);
                });
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void everyDisabledPublicBlockDropsExactlyOnePreservedItem(GameTestHelper helper) {
        if (!GameTestProfiles.requireDisabledContent(helper)) {
            return;
        }
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absolute = helper.absolutePos(MACHINE);
        player.setPos(absolute.getX() + 0.5D, absolute.getY(), absolute.getZ() + 0.5D);

        BuiltInRegistries.BLOCK.entrySet().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(RevivalAges.MOD_ID))
                .filter(entry -> entry.getValue().asItem() != net.minecraft.world.item.Items.AIR)
                .forEach(entry -> {
                    helper.getLevel().setBlock(absolute, entry.getValue().defaultBlockState(), 2);
                    BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(
                            helper.getLevel(),
                            absolute,
                            helper.getLevel().getBlockState(absolute),
                            player
                    );
                    NeoForge.EVENT_BUS.post(event);
                    helper.assertTrue(
                            event.isCanceled(),
                            "disabled break was not intercepted for " + entry.getKey().location()
                    );
                    List<ItemEntity> drops = new ArrayList<>(helper.getEntities(EntityType.ITEM));
                    List<ItemEntity> ownDrops = drops.stream()
                            .filter(entity -> entity.getItem().is(entry.getValue().asItem()))
                            .toList();
                    helper.assertValueEqual(
                            ownDrops.size(),
                            1,
                            entry.getKey().location() + " preserved drops"
                    );
                    drops.forEach(net.minecraft.world.entity.Entity::discard);
                    helper.assertBlockPresent(Blocks.AIR, MACHINE);
                });
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void disabledMachineDropsPreservedBlockEntityState(GameTestHelper helper) {
        if (!GameTestProfiles.requireDisabledContent(helper)) {
            return;
        }
        helper.setBlock(MACHINE, BarrelFeature.BARREL.get());
        if (!(helper.getBlockEntity(MACHINE) instanceof BarrelBlockEntity barrel)) {
            helper.fail("barrel block entity was not created", MACHINE);
            return;
        }
        int filled = barrel.fluidTank().fill(
                new FluidStack(Fluids.WATER, 750),
                IFluidHandler.FluidAction.EXECUTE
        );
        helper.assertValueEqual(filled, 750, "test fluid fill");
        helper.assertTrue(
                helper.getLevel().getCapability(
                        Capabilities.FluidHandler.BLOCK,
                        helper.absolutePos(MACHINE),
                        net.minecraft.core.Direction.UP
                ) == null,
                "disabled machine exposed a fluid capability"
        );

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absolute = helper.absolutePos(MACHINE);
        player.setPos(absolute.getX() + 0.5D, absolute.getY(), absolute.getZ() + 0.5D);
        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(
                helper.getLevel(),
                absolute,
                helper.getLevel().getBlockState(absolute),
                player
        );
        NeoForge.EVENT_BUS.post(breakEvent);
        helper.assertTrue(breakEvent.isCanceled(), "disabled machine break was not intercepted");
        List<ItemEntity> drops = helper.getEntities(EntityType.ITEM).stream()
                .filter(entity -> entity.getItem().is(BarrelFeature.BARREL_ITEM.get()))
                .toList();
        helper.assertValueEqual(drops.size(), 1, "preserved barrel drops");
        var data = drops.getFirst().getItem().get(DataComponents.BLOCK_ENTITY_DATA);
        helper.assertTrue(data != null, "preserved barrel lacks block entity data");
        helper.assertFalse(data.copyTag().getCompound("Tank").isEmpty(), "preserved barrel lost its fluid");
        helper.assertBlockPresent(Blocks.AIR, MACHINE);
        player.discard();
        helper.succeed();
    }

    private static void assertViewerCatalogsAvailable(GameTestHelper helper) {
        var manager = helper.getLevel().getRecipeManager();
        var registries = helper.getLevel().registryAccess();
        helper.assertFalse(DryingRecipeCatalog.crude(manager).isEmpty(), "crude drying viewer catalog is empty");
        helper.assertFalse(DryingRecipeCatalog.normal(manager).isEmpty(), "drying viewer catalog is empty");
        helper.assertFalse(PrimitiveRecipeCatalog.campfire(manager, registries).isEmpty(), "campfire viewer catalog is empty");
        helper.assertFalse(PrimitiveRecipeCatalog.chopping(manager).isEmpty(), "chopping viewer catalog is empty");
        helper.assertFalse(PrimitiveRecipeCatalog.pitKiln(manager).isEmpty(), "pit kiln viewer catalog is empty");
        helper.assertFalse(PrimitiveRecipeCatalog.pitBurn(manager).isEmpty(), "pit burn viewer catalog is empty");
        helper.assertFalse(PrimitiveRecipeCatalog.barrel(manager).isEmpty(), "barrel viewer catalog is empty");
        helper.assertFalse(PrimitiveRecipeCatalog.soakingPot(manager).isEmpty(), "soaking viewer catalog is empty");
        helper.assertFalse(PrimitiveRecipeCatalog.tanningRack(manager).isEmpty(), "tanning viewer catalog is empty");
        helper.assertFalse(StoneTechnologyRecipeCatalog.sawmill(manager).isEmpty(), "sawmill viewer catalog is empty");
        helper.assertFalse(StoneTechnologyRecipeCatalog.oven(helper.getLevel()).isEmpty(), "oven viewer catalog is empty");
        helper.assertFalse(StoneTechnologyRecipeCatalog.kiln(helper.getLevel()).isEmpty(), "kiln viewer catalog is empty");
        helper.assertFalse(
                StoneTechnologyRecipeCatalog.crucible(helper.getLevel()).isEmpty(),
                "crucible viewer catalog is empty"
        );
        helper.assertFalse(StoneTechnologyRecipeCatalog.anvil(manager).isEmpty(), "anvil viewer catalog is empty");
        helper.assertFalse(AnimalPowerRecipeCatalog.grinding(manager).isEmpty(), "grinding viewer catalog is empty");
        helper.assertFalse(AnimalPowerRecipeCatalog.pressing(manager).isEmpty(), "pressing viewer catalog is empty");
        helper.assertFalse(FrameAssemblyRecipeCatalog.recipes(manager).isEmpty(), "frame viewer catalog is empty");
        helper.assertFalse(KnappingRecipeCatalog.recipes(manager, registries).isEmpty(), "knapping viewer catalog is empty");
    }

    private static void assertViewerCatalogsEmpty(GameTestHelper helper) {
        var manager = helper.getLevel().getRecipeManager();
        var registries = helper.getLevel().registryAccess();
        helper.assertTrue(DryingRecipeCatalog.crude(manager).isEmpty(), "crude drying viewer recipes leaked");
        helper.assertTrue(DryingRecipeCatalog.normal(manager).isEmpty(), "drying viewer recipes leaked");
        helper.assertTrue(PrimitiveRecipeCatalog.campfire(manager, registries).isEmpty(), "campfire viewer recipes leaked");
        helper.assertTrue(PrimitiveRecipeCatalog.chopping(manager).isEmpty(), "chopping viewer recipes leaked");
        helper.assertTrue(PrimitiveRecipeCatalog.pitKiln(manager).isEmpty(), "pit kiln viewer recipes leaked");
        helper.assertTrue(PrimitiveRecipeCatalog.pitBurn(manager).isEmpty(), "pit burn viewer recipes leaked");
        helper.assertTrue(PrimitiveRecipeCatalog.barrel(manager).isEmpty(), "barrel viewer recipes leaked");
        helper.assertTrue(PrimitiveRecipeCatalog.soakingPot(manager).isEmpty(), "soaking viewer recipes leaked");
        helper.assertTrue(PrimitiveRecipeCatalog.tanningRack(manager).isEmpty(), "tanning viewer recipes leaked");
        helper.assertTrue(StoneTechnologyRecipeCatalog.sawmill(manager).isEmpty(), "sawmill viewer recipes leaked");
        helper.assertTrue(StoneTechnologyRecipeCatalog.oven(helper.getLevel()).isEmpty(), "oven viewer recipes leaked");
        helper.assertTrue(StoneTechnologyRecipeCatalog.kiln(helper.getLevel()).isEmpty(), "kiln viewer recipes leaked");
        helper.assertTrue(
                StoneTechnologyRecipeCatalog.crucible(helper.getLevel()).isEmpty(),
                "crucible viewer recipes leaked"
        );
        helper.assertTrue(StoneTechnologyRecipeCatalog.anvil(manager).isEmpty(), "anvil viewer recipes leaked");
        helper.assertTrue(AnimalPowerRecipeCatalog.grinding(manager).isEmpty(), "grinding viewer recipes leaked");
        helper.assertTrue(AnimalPowerRecipeCatalog.pressing(manager).isEmpty(), "pressing viewer recipes leaked");
        helper.assertTrue(FrameAssemblyRecipeCatalog.recipes(manager).isEmpty(), "frame viewer recipes leaked");
        helper.assertTrue(KnappingRecipeCatalog.recipes(manager, registries).isEmpty(), "knapping viewer recipes leaked");
    }
}
