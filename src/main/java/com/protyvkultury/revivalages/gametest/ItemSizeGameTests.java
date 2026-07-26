package com.protyvkultury.revivalages.gametest;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.size.Size;
import com.protyvkultury.revivalages.api.size.SizeApi;
import com.protyvkultury.revivalages.feature.inventory.itemsize.ItemSizeConfig;
import com.protyvkultury.revivalages.feature.inventory.itemsize.ItemSizeSettings;
import com.protyvkultury.revivalages.feature.technology.pitkiln.PitKilnFeature;
import com.protyvkultury.revivalages.feature.technology.pitkiln.blockentity.PitKilnBlockEntity;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(RevivalAges.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ItemSizeGameTests {

    private static final BlockPos FIRST = new BlockPos(3, 2, 3);
    private static final BlockPos SECOND = FIRST.east();

    private ItemSizeGameTests() {
    }

    @GameTest(template = "animal_power_empty")
    public static void sizeCodecsOrderingAndOverrideValidation(GameTestHelper helper) {
        helper.assertTrue(Size.TINY.isSmallerThan(Size.VERY_SMALL), "tiny ordering is invalid");
        helper.assertTrue(Size.NORMAL.isEqualOrSmallerThan(Size.NORMAL), "inclusive comparison is invalid");
        helper.assertFalse(Size.VERY_LARGE.isEqualOrSmallerThan(Size.LARGE), "large ordering is invalid");

        var encoded = Size.CODEC.encodeStart(JsonOps.INSTANCE, Size.VERY_LARGE).getOrThrow();
        helper.assertValueEqual(encoded.getAsString(), "very_large", "size codec name");
        helper.assertValueEqual(
                Size.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow(),
                Size.VERY_LARGE,
                "size codec round trip"
        );
        for (Size size : Size.values()) {
            ByteBuf buffer = Unpooled.buffer();
            Size.STREAM_CODEC.encode(buffer, size);
            helper.assertValueEqual(Size.STREAM_CODEC.decode(buffer), size, "size stream codec round trip");
            helper.assertValueEqual(buffer.readableBytes(), 0, "size stream codec trailing bytes");
        }
        helper.assertTrue(ItemSizeSettings.isValidOverride("block|minecraft:chest=normal"),
                "valid block override was rejected");
        helper.assertTrue(ItemSizeSettings.isValidOverride("item|minecraft:bundle=very_small"),
                "valid item override was rejected");
        helper.assertFalse(ItemSizeSettings.isValidOverride("entity|minecraft:chest=large"),
                "invalid override target was accepted");
        helper.assertFalse(ItemSizeSettings.isValidOverride("block|minecraft:chest=oversized"),
                "invalid override size was accepted");
        helper.assertValueEqual(
                ItemSizeConfig.REJECTION_COOLDOWN_TICKS.getDefault(),
                20,
                "rejection feedback cooldown default"
        );
        helper.assertTrue(ItemSizeConfig.REJECTION_ACTIONBAR_ENABLED.getDefault(),
                "action-bar rejection feedback is disabled by default");
        helper.assertTrue(ItemSizeConfig.REJECTION_SOUND_ENABLED.getDefault(),
                "sound rejection feedback is disabled by default");
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void chestRulesCoverSingleDoubleTrappedAndAutomation(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        assertChestRule(helper, FIRST, Blocks.CHEST);
        assertChestRule(helper, FIRST, Blocks.TRAPPED_CHEST);

        BlockPos first = helper.absolutePos(FIRST);
        BlockPos second = helper.absolutePos(SECOND);
        helper.getLevel().setBlock(first, Blocks.CHEST.defaultBlockState(), 3);
        helper.getLevel().setBlock(second, Blocks.CHEST.defaultBlockState(), 3);
        ChestBlockEntity left = requireChest(helper, first);
        ChestBlockEntity right = requireChest(helper, second);
        helper.assertFalse(left.canPlaceItem(0, new ItemStack(Items.OAK_LOG)),
                "double chest accepted a very large item through its first half");
        helper.assertFalse(right.canPlaceItem(0, new ItemStack(Items.OAK_LOG)),
                "double chest accepted a very large item through its second half");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var combined = new net.minecraft.world.CompoundContainer(left, right);
        ChestMenu menu = ChestMenu.sixRows(1, player.getInventory(), combined);
        helper.assertFalse(menu.getSlot(0).mayPlace(new ItemStack(Items.OAK_LOG)),
                "double chest menu accepted a very large item");
        helper.assertTrue(menu.getSlot(0).mayPlace(new ItemStack(Items.BOWL)),
                "double chest menu rejected a small item");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void bundleRulesCoverBothStackingDirections(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        SimpleContainer inventory = new SimpleContainer(1);
        Slot slot = new Slot(inventory, 0, 0, 0);

        ItemStackedOnOtherEvent bundleInSlot = new ItemStackedOnOtherEvent(
                new ItemStack(Items.ANVIL),
                new ItemStack(Items.BUNDLE),
                slot,
                ClickAction.SECONDARY,
                player,
                SlotAccess.NULL
        );
        NeoForge.EVENT_BUS.post(bundleInSlot);
        helper.assertTrue(bundleInSlot.isCanceled(), "bundle accepted a huge carried item");

        ItemStackedOnOtherEvent bundleCarried = new ItemStackedOnOtherEvent(
                new ItemStack(Items.BUNDLE),
                new ItemStack(Items.ANVIL),
                slot,
                ClickAction.SECONDARY,
                player,
                SlotAccess.NULL
        );
        NeoForge.EVENT_BUS.post(bundleCarried);
        helper.assertTrue(bundleCarried.isCanceled(), "carried bundle accepted a huge slot item");

        ItemStackedOnOtherEvent validInput = new ItemStackedOnOtherEvent(
                new ItemStack(Items.BOWL),
                new ItemStack(Items.BUNDLE),
                slot,
                ClickAction.SECONDARY,
                player,
                SlotAccess.NULL
        );
        NeoForge.EVENT_BUS.post(validInput);
        helper.assertFalse(validInput.isCanceled(), "bundle rejected a small item");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void savedOversizedChestStackRemainsExtractable(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        BlockPos position = helper.absolutePos(FIRST);
        helper.getLevel().setBlock(position, Blocks.CHEST.defaultBlockState(), 3);
        ChestBlockEntity chest = requireChest(helper, position);
        chest.setItem(0, new ItemStack(Items.ANVIL));

        var handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, position, Direction.UP);
        helper.assertTrue(handler != null, "chest item capability is missing");
        ItemStack extracted = handler.extractItem(0, 1, false);
        helper.assertTrue(extracted.is(Items.ANVIL), "saved oversized item could not be extracted");
        helper.assertTrue(chest.getItem(0).isEmpty(), "oversized item was duplicated during extraction");
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void pitKilnCapacityUsesEffectiveInputSize(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        BlockPos position = helper.absolutePos(FIRST);
        helper.getLevel().setBlock(position, PitKilnFeature.PIT_KILN.get().defaultBlockState(), 3);
        var blockEntity = helper.getLevel().getBlockEntity(position);
        helper.assertTrue(blockEntity instanceof PitKilnBlockEntity, "pit kiln block entity is missing");
        PitKilnBlockEntity kiln = (PitKilnBlockEntity) blockEntity;

        helper.assertTrue(SizeApi.getSize(new ItemStack(Items.BOWL)) == Size.SMALL,
                "bowl size data did not load");
        helper.assertTrue(SizeApi.getSize(new ItemStack(Items.ANVIL)) == Size.HUGE,
                "anvil size data did not load");
        helper.assertValueEqual(kiln.maximumInputCount(new ItemStack(Items.BOWL)), 4, "batchable capacity");
        helper.assertValueEqual(kiln.maximumInputCount(new ItemStack(Items.ANVIL)), 1, "oversized capacity");
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void disabledItemSizeDoesNotRejectChestInsertion(GameTestHelper helper) {
        if (!GameTestProfiles.requireDisabledContent(helper)) {
            return;
        }
        BlockPos position = helper.absolutePos(FIRST);
        helper.getLevel().setBlock(position, Blocks.CHEST.defaultBlockState(), 3);
        ChestBlockEntity chest = requireChest(helper, position);
        helper.assertTrue(chest.canPlaceItem(0, new ItemStack(Items.ANVIL)),
                "disabled Item Size still rejected chest insertion");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ChestMenu menu = ChestMenu.threeRows(2, player.getInventory(), chest);
        helper.assertTrue(menu.getSlot(0).mayPlace(new ItemStack(Items.OAK_LOG)),
                "disabled Item Size still rejected a chest menu insertion");
        player.discard();
        helper.succeed();
    }

    private static void assertChestRule(GameTestHelper helper, BlockPos relative, net.minecraft.world.level.block.Block block) {
        BlockPos position = helper.absolutePos(relative);
        helper.getLevel().setBlock(position, block.defaultBlockState(), 3);
        ChestBlockEntity chest = requireChest(helper, position);
        helper.assertTrue(chest.canPlaceItem(0, new ItemStack(Items.BOWL)),
                block.getName().getString() + " rejected a small item");
        helper.assertFalse(chest.canPlaceItem(0, new ItemStack(Items.OAK_LOG)),
                block.getName().getString() + " accepted a very large item");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ChestMenu menu = ChestMenu.threeRows(3, player.getInventory(), chest);
        helper.assertFalse(menu.getSlot(0).mayPlace(new ItemStack(Items.OAK_LOG)),
                block.getName().getString() + " menu accepted a very large item");
        helper.assertTrue(menu.getSlot(0).mayPlace(new ItemStack(Items.BOWL)),
                block.getName().getString() + " menu rejected a small item");
        menu.setCarried(new ItemStack(Items.OAK_LOG));
        menu.clicked(0, 0, ClickType.PICKUP, player);
        helper.assertTrue(menu.getCarried().is(Items.OAK_LOG),
                block.getName().getString() + " consumed a rejected click input");
        helper.assertTrue(chest.getItem(0).isEmpty(),
                block.getName().getString() + " inserted a rejected click input");
        player.discard();

        var handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, position, Direction.UP);
        helper.assertTrue(handler != null, block.getName().getString() + " item capability is missing");
        helper.assertTrue(handler.insertItem(0, new ItemStack(Items.OAK_LOG), false).is(Items.OAK_LOG),
                block.getName().getString() + " automation bypassed the size rule");
        helper.assertTrue(handler.insertItem(0, new ItemStack(Items.BOWL), false).isEmpty(),
                block.getName().getString() + " rejected valid automation insertion");
        helper.getLevel().setBlock(position, Blocks.AIR.defaultBlockState(), 3);
    }

    private static ChestBlockEntity requireChest(GameTestHelper helper, BlockPos position) {
        var blockEntity = helper.getLevel().getBlockEntity(position);
        helper.assertTrue(blockEntity instanceof ChestBlockEntity, "chest block entity is missing");
        return (ChestBlockEntity) blockEntity;
    }
}
