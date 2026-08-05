package com.protyvkultury.revivalages.gametest;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalMachineKind;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerTags;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalWorkArea;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalWorkerController;
import com.protyvkultury.revivalages.feature.technology.animalpower.block.AnimalMachineBlock;
import com.protyvkultury.revivalages.feature.technology.animalpower.blockentity.AnimalMachineBlockEntity;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingRecipe;
import com.protyvkultury.revivalages.feature.technology.animalpower.recipe.PressingRecipe;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(RevivalAges.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AnimalPowerGameTests {

    private static final BlockPos MACHINE = new BlockPos(4, 2, 4);

    private AnimalPowerGameTests() {
    }

    @GameTest(template = "animal_power_empty")
    public static void referenceGrindstoneAreaAcceptsCentralPlatform(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        buildReferenceWorkArea(helper, false);
        helper.setBlock(MACHINE, AnimalPowerFeature.HORSE_GRINDSTONE.get());

        if (!AnimalWorkArea.isValid(helper.getLevel(), helper.absolutePos(MACHINE), false)) {
            helper.fail("The Horse Power grindstone work area was rejected", MACHINE);
        }
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void occupiedOuterRingInvalidatesWorkArea(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        buildReferenceWorkArea(helper, false);
        helper.setBlock(MACHINE, AnimalPowerFeature.HORSE_GRINDSTONE.get());
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.STONE);

        if (AnimalWorkArea.isValid(helper.getLevel(), helper.absolutePos(MACHINE), false)) {
            helper.fail("An occupied Horse Power work-area cell was accepted", MACHINE);
        }
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void referenceTallMachineAreaRequiresOuterHeadroom(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        buildReferenceWorkArea(helper, true);
        helper.setBlock(MACHINE, AnimalPowerFeature.HORSE_PRESS.get());
        helper.setBlock(MACHINE.above(), AnimalPowerFeature.HORSE_PRESS.get());

        if (!AnimalWorkArea.isValid(helper.getLevel(), helper.absolutePos(MACHINE), true)) {
            helper.fail("The Horse Power tall-machine work area was rejected", MACHINE);
            return;
        }
        helper.setBlock(new BlockPos(1, 3, 1), Blocks.STONE);
        if (AnimalWorkArea.isValid(helper.getLevel(), helper.absolutePos(MACHINE), true)) {
            helper.fail("Blocked outer headroom was accepted for a tall machine", MACHINE);
        }
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void configuredWorkerTagContainsEveryDefaultWorker(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        List<EntityType<?>> workers = List.of(
                EntityType.HORSE,
                EntityType.DONKEY,
                EntityType.MULE,
                EntityType.LLAMA,
                EntityType.CAMEL
        );
        if (!workers.stream().allMatch(type -> type.is(AnimalPowerTags.WORKERS))) {
            helper.fail("The default animal-power worker tag is incomplete");
        }
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty", timeoutTicks = 20)
    public static void breakingUpperHalfRemovesTallMachine(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        buildReferenceWorkArea(helper, true);
        helper.setBlock(
                MACHINE,
                AnimalPowerFeature.HORSE_PRESS.get().defaultBlockState()
                        .setValue(AnimalMachineBlock.HALF, DoubleBlockHalf.LOWER)
        );
        helper.setBlock(
                MACHINE.above(),
                AnimalPowerFeature.HORSE_PRESS.get().defaultBlockState()
                        .setValue(AnimalMachineBlock.HALF, DoubleBlockHalf.UPPER)
        );

        helper.getLevel().destroyBlock(helper.absolutePos(MACHINE.above()), false);
        helper.runAfterDelay(1, () -> {
            helper.assertBlockPresent(Blocks.AIR, MACHINE);
            helper.assertBlockPresent(Blocks.AIR, MACHINE.above());
            helper.succeed();
        });
    }

    @GameTest(template = "animal_power_empty")
    public static void processingRecipeStreamCodecsRoundTrip(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        ServerLevel level = helper.getLevel();
        GrindingRecipe grinding = level.getRecipeManager()
                .getAllRecipesFor(AnimalPowerFeature.GRINDING_TYPE.get())
                .getFirst()
                .value();
        PressingRecipe pressing = level.getRecipeManager()
                .getAllRecipesFor(AnimalPowerFeature.PRESSING_TYPE.get())
                .getFirst()
                .value();

        RegistryFriendlyByteBuf grindingBuffer =
                new RegistryFriendlyByteBuf(
                        Unpooled.buffer(),
                        level.registryAccess(),
                        ConnectionType.NEOFORGE
                );
        RegistryFriendlyByteBuf pressingBuffer =
                new RegistryFriendlyByteBuf(
                        Unpooled.buffer(),
                        level.registryAccess(),
                        ConnectionType.NEOFORGE
                );
        RegistryFriendlyByteBuf combinedPressingBuffer =
                new RegistryFriendlyByteBuf(
                        Unpooled.buffer(),
                        level.registryAccess(),
                        ConnectionType.NEOFORGE
                );
        try {
            AnimalPowerFeature.GRINDING_SERIALIZER.get().streamCodec().encode(grindingBuffer, grinding);
            GrindingRecipe decodedGrinding =
                    AnimalPowerFeature.GRINDING_SERIALIZER.get().streamCodec().decode(grindingBuffer);
            helper.assertValueEqual(decodedGrinding.inputCount(), grinding.inputCount(), "grinding input count");
            helper.assertValueEqual(decodedGrinding.workPoints(), grinding.workPoints(), "grinding work points");
            helper.assertValueEqual(decodedGrinding.machines(), grinding.machines(), "grinding machine variants");

            AnimalPowerFeature.PRESSING_SERIALIZER.get().streamCodec().encode(pressingBuffer, pressing);
            PressingRecipe decodedPressing =
                    AnimalPowerFeature.PRESSING_SERIALIZER.get().streamCodec().decode(pressingBuffer);
            helper.assertValueEqual(decodedPressing.inputCount(), pressing.inputCount(), "pressing input count");
            ItemStack decodedItem = decodedPressing.itemResult();
            ItemStack originalItem = pressing.itemResult();
            helper.assertTrue(
                    decodedItem.getCount() == originalItem.getCount()
                            && ItemStack.isSameItemSameComponents(decodedItem, originalItem),
                    "pressing item result changed during stream-codec round trip"
            );
            FluidStack decodedFluid = decodedPressing.fluidResult();
            FluidStack originalFluid = pressing.fluidResult();
            helper.assertTrue(
                    decodedFluid.getAmount() == originalFluid.getAmount()
                            && (decodedFluid.isEmpty() && originalFluid.isEmpty()
                            || FluidStack.isSameFluidSameComponents(decodedFluid, originalFluid)),
                    "pressing fluid result changed during stream-codec round trip"
            );

            PressingRecipe combined = new PressingRecipe(
                    Ingredient.of(Items.WHEAT_SEEDS),
                    1,
                    new ItemStack(Items.DIRT),
                    new FluidStack(Fluids.WATER, 1000));
            AnimalPowerFeature.PRESSING_SERIALIZER.get().streamCodec().encode(combinedPressingBuffer, combined);
            PressingRecipe decodedCombined =
                    AnimalPowerFeature.PRESSING_SERIALIZER.get().streamCodec().decode(combinedPressingBuffer);
            helper.assertTrue(decodedCombined.hasItemResult() && decodedCombined.hasFluidResult(),
                    "combined pressing result lost an output during stream-codec round trip");
        } finally {
            grindingBuffer.release();
            pressingBuffer.release();
            combinedPressingBuffer.release();
        }
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void pressingCodecAcceptsCombinedAndRejectsMissingResults(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        var operations = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
        var bothResults = JsonParser.parseString("""
                {
                  "ingredient": { "item": "minecraft:wheat_seeds" },
                  "input_count": 1,
                  "result": { "id": "minecraft:dirt", "count": 1 },
                  "fluid_result": { "id": "minecraft:water", "amount": 1000 }
                }
                """);
        var noResults = JsonParser.parseString("""
                {
                  "ingredient": { "item": "minecraft:wheat_seeds" },
                  "input_count": 1
                }
                """);
        helper.assertTrue(
                AnimalPowerFeature.PRESSING_SERIALIZER.get()
                        .codec()
                        .codec()
                        .parse(operations, bothResults)
                        .result()
                        .isPresent(),
                "pressing codec rejected combined result kinds"
        );
        helper.assertTrue(
                AnimalPowerFeature.PRESSING_SERIALIZER.get()
                        .codec()
                        .codec()
                        .parse(operations, noResults)
                        .error()
                        .isPresent(),
                "pressing codec accepted a recipe without a result"
        );
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void everyDefaultWorkerAttachesAndDetaches(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos machinePos = helper.absolutePos(MACHINE);
        player.setPos(machinePos.getX() + 0.5D, machinePos.getY(), machinePos.getZ() + 0.5D);
        List<EntityType<? extends Mob>> types = List.of(
                EntityType.HORSE,
                EntityType.DONKEY,
                EntityType.MULE,
                EntityType.LLAMA,
                EntityType.CAMEL
        );

        for (EntityType<? extends Mob> type : types) {
            Mob mob = helper.spawn(type, MACHINE.offset(1, 0, 0));
            mob.setLeashedTo(player, true);
            AnimalWorkerController controller = new AnimalWorkerController();
            helper.assertTrue(
                    controller.attach(level, machinePos, AnimalMachineKind.GRINDSTONE, player),
                    "eligible worker did not attach"
            );
            helper.assertValueEqual(
                    controller.workerId().orElseThrow(),
                    mob.getUUID(),
                    "wrong worker UUID was stored"
            );
            helper.assertFalse(mob.isLeashed(), "attached worker kept the player's vanilla tether");
            helper.assertTrue(mob.hasRestriction(), "attached worker did not receive a machine home");
            helper.assertValueEqual(mob.getRestrictCenter(), machinePos, "worker home was placed incorrectly");
            helper.assertValueEqual(mob.getRestrictRadius(), 3.0F, "worker home radius changed");
            helper.assertTrue(
                    controller.releaseToPlayer(level, machinePos, player),
                    "worker was not returned to the interacting player"
            );
            helper.assertTrue(controller.workerId().isEmpty(), "worker did not detach safely");
            helper.assertTrue(mob.isLeashed() && player.equals(mob.getLeashHolder()),
                    "worker was not returned on its existing lead");
            helper.assertFalse(mob.hasRestriction(), "released worker retained the machine home");
            mob.discard();
        }
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void emptyHandReturnsWorkerToPlayer(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        helper.setBlock(MACHINE, AnimalPowerFeature.HORSE_GRINDSTONE.get());
        ServerLevel level = helper.getLevel();
        BlockPos machinePos = helper.absolutePos(MACHINE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(machinePos.getX() + 0.5D, machinePos.getY(), machinePos.getZ() + 0.5D);
        Mob worker = helper.spawn(EntityType.HORSE, MACHINE.offset(1, 0, 0));
        worker.setLeashedTo(player, true);
        AnimalMachineBlockEntity machine = (AnimalMachineBlockEntity) helper.getBlockEntity(MACHINE);
        helper.assertTrue(machine.attachWorker(player), "worker did not attach to the machine");

        helper.assertValueEqual(
                helper.getBlockState(MACHINE).useWithoutItem(level, player, hit(machinePos, Direction.UP)),
                net.minecraft.world.InteractionResult.CONSUME,
                "empty-hand interaction did not release the worker"
        );
        helper.assertTrue(machine.workerId().isEmpty(), "empty-hand interaction retained the worker UUID");
        helper.assertTrue(worker.isLeashed() && player.equals(worker.getLeashHolder()),
                "empty-hand interaction did not return the lead to the player");
        helper.assertFalse(worker.hasRestriction(), "empty-hand interaction retained the machine home");
        worker.discard();
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty", timeoutTicks = 20)
    public static void grindstoneWorkerNavigatesOnLowerRoute(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        buildReferenceWorkArea(helper, false);
        ServerLevel level = helper.getLevel();
        BlockPos machinePos = helper.absolutePos(MACHINE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(machinePos.getX() + 0.5D, machinePos.getY(), machinePos.getZ() + 0.5D);
        Mob worker = helper.spawn(EntityType.HORSE, MACHINE.offset(-2, -1, -2));
        worker.setLeashedTo(player, true);

        AnimalWorkerController controller = new AnimalWorkerController();
        helper.assertTrue(
                controller.attach(level, machinePos, AnimalMachineKind.GRINDSTONE, player),
                "worker did not attach to the grindstone"
        );
        helper.runAfterDelay(2, () -> {
            helper.assertFalse(
                    controller.tick(level, machinePos, AnimalMachineKind.GRINDSTONE, true),
                    "worker unexpectedly started inside a waypoint"
            );
            helper.assertFalse(
                    worker.getNavigation().isDone(),
                    "worker did not receive a path on the grindstone's lower route"
            );
            worker.discard();
            player.discard();
            helper.succeed();
        });
    }

    @GameTest(template = "animal_power_empty")
    public static void waypointAndMissingEntityStatePersist(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos machinePos = helper.absolutePos(MACHINE);
        player.setPos(machinePos.getX() + 0.5D, machinePos.getY(), machinePos.getZ() + 0.5D);
        Mob worker = helper.spawn(EntityType.HORSE, MACHINE.offset(-3, -1, -3));
        worker.setLeashedTo(player, true);

        AnimalWorkerController controller = new AnimalWorkerController();
        helper.assertTrue(
                controller.attach(level, machinePos, AnimalMachineKind.GRINDSTONE, player),
                "worker did not attach"
        );
        UUID workerId = worker.getUUID();
        int originalWaypoint = controller.waypointIndex();
        helper.assertTrue(
                controller.tick(level, machinePos, AnimalMachineKind.GRINDSTONE, true),
                "reached waypoint was not counted"
        );
        helper.assertTrue(controller.waypointIndex() != originalWaypoint, "waypoint did not advance");

        CompoundTag saved = new CompoundTag();
        controller.save(saved);
        AnimalWorkerController restored = new AnimalWorkerController();
        restored.load(saved);
        helper.assertValueEqual(restored.workerId().orElseThrow(), workerId, "worker UUID persistence");
        helper.assertValueEqual(
                restored.waypointIndex(),
                controller.waypointIndex(),
                "waypoint persistence"
        );

        worker.discard();
        helper.assertFalse(
                restored.tick(level, machinePos, AnimalMachineKind.GRINDSTONE, true),
                "missing worker unexpectedly produced work"
        );
        helper.assertValueEqual(
                restored.workerId().orElseThrow(),
                workerId,
                "missing worker was detached instead of retained for retry"
        );
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void pressFluidHandlerIsDrainOnly(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        helper.setBlock(MACHINE, AnimalPowerFeature.HORSE_PRESS.get());
        if (!(helper.getBlockEntity(MACHINE) instanceof AnimalMachineBlockEntity press)) {
            helper.fail("Press block entity was not created", MACHINE);
            return;
        }
        IFluidHandler output = press.fluidOutputHandler();
        helper.assertValueEqual(
                output.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE),
                0,
                "output handler accepted fluid insertion"
        );
        press.fluidTank().fill(
                new FluidStack(Fluids.WATER, 1000),
                IFluidHandler.FluidAction.EXECUTE
        );
        helper.assertValueEqual(
                output.drain(250, IFluidHandler.FluidAction.EXECUTE).getAmount(),
                250,
                "output handler did not allow draining"
        );
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void pressInteractionTransfersFluidToHeldContainer(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        helper.setBlock(MACHINE, AnimalPowerFeature.HORSE_PRESS.get());
        AnimalMachineBlockEntity press = (AnimalMachineBlockEntity) helper.getBlockEntity(MACHINE);
        press.fluidTank().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        BlockPos machinePos = helper.absolutePos(MACHINE);

        helper.assertValueEqual(
                helper.getBlockState(MACHINE).useItemOn(
                        player.getMainHandItem(),
                        helper.getLevel(),
                        player,
                        net.minecraft.world.InteractionHand.MAIN_HAND,
                        hit(machinePos, Direction.UP)
                ),
                net.minecraft.world.ItemInteractionResult.SUCCESS,
                "press did not handle the held fluid container"
        );
        helper.assertTrue(player.getMainHandItem().is(Items.WATER_BUCKET),
                "press did not fill the held bucket");
        helper.assertTrue(press.fluidTank().isEmpty(), "press did not drain its fluid output");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void automationSeparatesInputAndOutputFaces(GameTestHelper helper) {
        if (!GameTestProfiles.requireEnabledContent(helper)) {
            return;
        }
        helper.setBlock(MACHINE, AnimalPowerFeature.HORSE_GRINDSTONE.get());
        AnimalMachineBlockEntity machine = (AnimalMachineBlockEntity) helper.getBlockEntity(MACHINE);
        GrindingRecipe recipe = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(AnimalPowerFeature.GRINDING_TYPE.get())
                .stream()
                .map(holder -> holder.value())
                .filter(candidate -> candidate.supports(
                        com.protyvkultury.revivalages.feature.technology.animalpower.recipe.GrindingMachine.ANIMAL))
                .findFirst()
                .orElseThrow();
        ItemStack input = recipe.ingredient().getItems()[0].copyWithCount(recipe.inputCount());
        IItemHandler sideHandler = Objects.requireNonNull(machine.itemHandler(Direction.NORTH));
        IItemHandler bottomHandler = Objects.requireNonNull(machine.itemHandler(Direction.DOWN));

        helper.assertValueEqual(sideHandler.getSlots(), 1, "side automation exposed result slots");
        helper.assertValueEqual(bottomHandler.getSlots(), 2, "bottom automation exposed the input slot");
        helper.assertTrue(sideHandler.insertItem(0, input.copy(), false).isEmpty(),
                "side automation could not insert the recipe input");
        helper.assertTrue(machine.item(0).is(input.getItem()), "side automation did not populate the input slot");
        helper.assertTrue(bottomHandler.getStackInSlot(0).isEmpty(),
                "bottom automation exposed the occupied input slot");
        ItemStack rejectedInput = bottomHandler.insertItem(0, input.copy(), true);
        helper.assertTrue(
                rejectedInput.getCount() == input.getCount()
                        && ItemStack.isSameItemSameComponents(rejectedInput, input),
                "bottom automation accepted an input stack"
        );
        helper.succeed();
    }

    private static BlockHitResult hit(BlockPos pos, Direction side) {
        return new BlockHitResult(Vec3.atCenterOf(pos), side, pos, false);
    }

    private static void buildReferenceWorkArea(GameTestHelper helper, boolean tallMachine) {
        for (int x = 1; x <= 7; x++) {
            for (int z = 1; z <= 7; z++) {
                helper.setBlock(new BlockPos(x, 2, z), Blocks.AIR);
                int relativeX = x - MACHINE.getX();
                int relativeZ = z - MACHINE.getZ();
                boolean central = Math.abs(relativeX) <= 1 && Math.abs(relativeZ) <= 1;
                if (tallMachine || central) {
                    helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
                } else {
                    helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
                    helper.setBlock(new BlockPos(x, 1, z), Blocks.AIR);
                }
                helper.setBlock(new BlockPos(x, 3, z), Blocks.AIR);
            }
        }
    }
}
