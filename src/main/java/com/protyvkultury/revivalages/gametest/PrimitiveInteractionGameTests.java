package com.protyvkultury.revivalages.gametest;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import com.protyvkultury.revivalages.feature.technology.barrel.block.BarrelBlock;
import com.protyvkultury.revivalages.feature.technology.barrel.blockentity.BarrelBlockEntity;
import com.protyvkultury.revivalages.feature.technology.barrel.storage.StorageBarrelBlock;
import com.protyvkultury.revivalages.feature.technology.barrel.storage.StorageBarrelBlockEntity;
import com.protyvkultury.revivalages.feature.technology.ignition.IgnitionFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.blockentity.CampfireBlockEntity;
import com.protyvkultury.revivalages.feature.technology.dryingrack.DryingRackFeature;
import com.protyvkultury.revivalages.feature.technology.dryingrack.blockentity.DryingRackBlockEntity;
import com.protyvkultury.revivalages.feature.technology.pitkiln.PitKilnFeature;
import com.protyvkultury.revivalages.feature.technology.pitkiln.blockentity.PitKilnBlockEntity;
import com.protyvkultury.revivalages.feature.technology.soakingpot.SoakingPotFeature;
import com.protyvkultury.revivalages.feature.technology.soakingpot.blockentity.SoakingPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.capabilities.Capabilities;

@GameTestHolder(RevivalAges.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PrimitiveInteractionGameTests {

    private static final BlockPos MACHINE = new BlockPos(4, 2, 4);

    private PrimitiveInteractionGameTests() {
    }

    @GameTest(template = "animal_power_empty")
    public static void barrelLidUsesOnlyTheTopFaceAndCanBeRemoved(GameTestHelper helper) {
        if (!ContentAvailability.isEnabled(ContentKey.BARREL)) {
            helper.succeed();
            return;
        }

        helper.setBlock(MACHINE, BarrelFeature.BARREL.get());
        if (!(helper.getBlockEntity(MACHINE) instanceof BarrelBlockEntity barrel)) {
            helper.fail("barrel block entity was not created", MACHINE);
            return;
        }

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack lid = new ItemStack(BarrelFeature.BARREL_LID.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, lid);
        BlockPos absolute = helper.absolutePos(MACHINE);

        BlockState state = helper.getBlockState(MACHINE);
        state.useItemOn(lid, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit(absolute, Direction.NORTH));
        helper.assertFalse(state.getValue(BarrelBlock.SEALED), "side click sealed the barrel");
        helper.assertValueEqual(lid.getCount(), 1, "side click consumed the lid");

        state.useItemOn(lid, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit(absolute, Direction.UP));
        helper.assertTrue(helper.getBlockState(MACHINE).getValue(BarrelBlock.SEALED), "top click did not seal");
        helper.assertTrue(lid.isEmpty(), "top click did not consume the lid");

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        ItemInteractionResult sideResult = helper.getBlockState(MACHINE).useItemOn(
                ItemStack.EMPTY,
                helper.getLevel(),
                player,
                InteractionHand.MAIN_HAND,
                hit(absolute, Direction.NORTH)
        );
        helper.assertValueEqual(sideResult, ItemInteractionResult.CONSUME, "side click reached empty-hand use");
        helper.assertTrue(helper.getBlockState(MACHINE).getValue(BarrelBlock.SEALED), "side click removed the lid");

        ItemInteractionResult topResult = helper.getBlockState(MACHINE).useItemOn(
                ItemStack.EMPTY,
                helper.getLevel(),
                player,
                InteractionHand.MAIN_HAND,
                hit(absolute, Direction.UP)
        );
        helper.assertValueEqual(
                topResult,
                ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION,
                "top click did not reach empty-hand use"
        );
        helper.getBlockState(MACHINE).useWithoutItem(helper.getLevel(), player, hit(absolute, Direction.UP));
        helper.assertFalse(helper.getBlockState(MACHINE).getValue(BarrelBlock.SEALED), "top click did not unseal");
        helper.assertValueEqual(
                player.getInventory().countItem(BarrelFeature.BARREL_LID.get()),
                1,
                "removed lid was not returned"
        );
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void heldIgniterDoesNotReachPitKilnInventoryInteraction(GameTestHelper helper) {
        if (!ContentAvailability.isEnabled(ContentKey.PIT_KILN)
                || !ContentAvailability.isEnabled(ContentKey.FLINT_AND_TINDER)) {
            helper.succeed();
            return;
        }
        helper.setBlock(MACHINE, PitKilnFeature.PIT_KILN.get());
        if (!(helper.getBlockEntity(MACHINE) instanceof PitKilnBlockEntity kiln)) {
            helper.fail("pit kiln block entity was not created", MACHINE);
            return;
        }
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack igniter = new ItemStack(IgnitionFeature.FLINT_AND_TINDER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, igniter);
        ItemInteractionResult result = helper.getBlockState(MACHINE).useItemOn(
                igniter,
                helper.getLevel(),
                player,
                InteractionHand.MAIN_HAND,
                hit(helper.absolutePos(MACHINE), Direction.UP)
        );
        helper.assertValueEqual(
                result,
                ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION,
                "held igniter reached the pit kiln inventory interaction"
        );
        helper.assertValueEqual(kiln.logCount(), 0, "held igniter removed a pit kiln log");
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void sealedStorageBarrelBlocksCachedAutomation(GameTestHelper helper) {
        if (!ContentAvailability.isEnabled(ContentKey.STORAGE_BARREL)) {
            helper.succeed();
            return;
        }
        helper.setBlock(MACHINE, BarrelFeature.STORAGE_BARREL.get());
        if (!(helper.getBlockEntity(MACHINE) instanceof StorageBarrelBlockEntity barrel)) {
            helper.fail("storage barrel block entity was not created", MACHINE);
            return;
        }
        BlockPos absolute = helper.absolutePos(MACHINE);
        var cachedHandler = helper.getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK,
                absolute,
                Direction.UP
        );
        helper.assertTrue(cachedHandler != null, "open storage barrel did not expose automation");
        ItemStack remainder = cachedHandler.insertItem(0, new ItemStack(net.minecraft.world.item.Items.APPLE), false);
        helper.assertTrue(remainder.isEmpty(), "open storage barrel rejected an arbitrary item");
        helper.assertTrue(barrel.seal(), "storage barrel did not seal");
        helper.assertTrue(
                helper.getBlockState(MACHINE).getValue(StorageBarrelBlock.SEALED),
                "storage barrel sealed state was not stored"
        );
        ItemStack blocked = cachedHandler.insertItem(1, new ItemStack(net.minecraft.world.item.Items.STONE), false);
        helper.assertValueEqual(blocked.getCount(), 1, "cached automation inserted into a sealed storage barrel");
        helper.assertTrue(
                cachedHandler.extractItem(0, 1, false).isEmpty(),
                "cached automation extracted from a sealed storage barrel"
        );
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void normalDryingRackAcceptsManualInteractionOnlyFromAbove(GameTestHelper helper) {
        if (!ContentAvailability.isEnabled(ContentKey.DRYING_RACK)) {
            helper.succeed();
            return;
        }
        helper.setBlock(MACHINE, DryingRackFeature.DRYING_RACK.get());
        if (!(helper.getBlockEntity(MACHINE) instanceof DryingRackBlockEntity rack)) {
            helper.fail("drying rack block entity was not created", MACHINE);
            return;
        }
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack dirt = new ItemStack(net.minecraft.world.item.Items.DIRT, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, dirt);
        BlockPos absolute = helper.absolutePos(MACHINE);

        helper.getBlockState(MACHINE).useItemOn(
                dirt,
                helper.getLevel(),
                player,
                InteractionHand.MAIN_HAND,
                hit(absolute, Direction.NORTH));
        helper.assertTrue(rack.getItem(3).isEmpty(), "side interaction inserted into the drying rack");
        helper.assertValueEqual(dirt.getCount(), 2, "side interaction consumed the held item");

        helper.getBlockState(MACHINE).useItemOn(
                dirt,
                helper.getLevel(),
                player,
                InteractionHand.MAIN_HAND,
                hit(absolute, Direction.UP));
        helper.assertValueEqual(rack.getItem(3).getItem(), net.minecraft.world.item.Items.DIRT,
                "top interaction did not insert the selected item");
        helper.assertValueEqual(dirt.getCount(), 1, "top interaction consumed the wrong amount");
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void soakingPotUsesTopInteractionAndClearsCampfireCookingSlot(GameTestHelper helper) {
        if (!ContentAvailability.isEnabled(ContentKey.SOAKING_POT)
                || !ContentAvailability.isEnabled(ContentKey.CAMPFIRE)) {
            helper.succeed();
            return;
        }
        BlockPos campfirePos = MACHINE.below();
        helper.setBlock(campfirePos, CampfireFeature.CAMPFIRE.get());
        if (!(helper.getBlockEntity(campfirePos) instanceof CampfireBlockEntity campfire)) {
            helper.fail("campfire block entity was not created", campfirePos);
            return;
        }
        ItemStack cooking = new ItemStack(net.minecraft.world.item.Items.BEEF);
        campfire.insertCookingStack(cooking, true);
        helper.setBlock(MACHINE, SoakingPotFeature.SOAKING_POT.get());
        if (!(helper.getBlockEntity(MACHINE) instanceof SoakingPotBlockEntity pot)) {
            helper.fail("soaking pot block entity was not created", MACHINE);
            return;
        }
        helper.assertTrue(campfire.cookingStack().isEmpty(),
                "placing the soaking pot did not clear the campfire cooking slot");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack water = new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET);
        player.setItemInHand(InteractionHand.MAIN_HAND, water);
        BlockPos absolute = helper.absolutePos(MACHINE);
        helper.getBlockState(MACHINE).useItemOn(
                water,
                helper.getLevel(),
                player,
                InteractionHand.MAIN_HAND,
                hit(absolute, Direction.NORTH));
        helper.assertValueEqual(pot.fluidTank().getFluidAmount(), 0,
                "side interaction filled the soaking pot");
        helper.getBlockState(MACHINE).useItemOn(
                water,
                helper.getLevel(),
                player,
                InteractionHand.MAIN_HAND,
                hit(absolute, Direction.UP));
        helper.assertValueEqual(pot.fluidTank().getFluidAmount(), 1000,
                "top interaction did not fill the soaking pot");
        helper.succeed();
    }

    private static BlockHitResult hit(BlockPos pos, Direction direction) {
        double x = pos.getX() + 0.5D + direction.getStepX() * 0.5D;
        double y = pos.getY() + 0.5D + direction.getStepY() * 0.5D;
        double z = pos.getZ() + 0.5D + direction.getStepZ() * 0.5D;
        return new BlockHitResult(new Vec3(x, y, z), direction, pos, false);
    }
}
