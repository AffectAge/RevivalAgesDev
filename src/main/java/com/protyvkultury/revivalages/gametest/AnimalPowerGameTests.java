package com.protyvkultury.revivalages.gametest;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.protyvkultury.revivalages.RevivalAges;
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
import java.util.UUID;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
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
    public static void symmetricWorkAreaAcceptsEveryEdge(GameTestHelper helper) {
        buildFloor(helper);
        helper.setBlock(MACHINE, AnimalPowerFeature.HORSE_GRINDSTONE.get());

        if (!AnimalWorkArea.isValid(helper.getLevel(), helper.absolutePos(MACHINE), false)) {
            helper.fail("A solid, clear 7x7x2 area was rejected", MACHINE);
        }
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void missingCornerInvalidatesWorkArea(GameTestHelper helper) {
        buildFloor(helper);
        helper.setBlock(MACHINE, AnimalPowerFeature.HORSE_GRINDSTONE.get());
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.AIR);

        if (AnimalWorkArea.isValid(helper.getLevel(), helper.absolutePos(MACHINE), false)) {
            helper.fail("A missing perimeter floor block was accepted", MACHINE);
        }
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void configuredWorkerTagContainsEveryDefaultWorker(GameTestHelper helper) {
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
        buildFloor(helper);
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
        } finally {
            grindingBuffer.release();
            pressingBuffer.release();
        }
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void pressingCodecRejectsMutuallyExclusiveResultViolation(GameTestHelper helper) {
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
                        .error()
                        .isPresent(),
                "pressing codec accepted both result kinds"
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
            helper.assertTrue(controller.attach(level, machinePos, player), "eligible worker did not attach");
            helper.assertValueEqual(
                    controller.workerId().orElseThrow(),
                    mob.getUUID(),
                    "wrong worker UUID was stored"
            );
            controller.detach(level, machinePos, true);
            helper.assertTrue(controller.workerId().isEmpty(), "worker did not detach safely");
            mob.discard();
        }
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void waypointAndMissingEntityStatePersist(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos machinePos = helper.absolutePos(MACHINE);
        player.setPos(machinePos.getX() + 0.5D, machinePos.getY(), machinePos.getZ() + 0.5D);
        Mob worker = helper.spawn(EntityType.HORSE, MACHINE.offset(-3, 0, -3));
        worker.setLeashedTo(player, true);

        AnimalWorkerController controller = new AnimalWorkerController();
        helper.assertTrue(controller.attach(level, machinePos, player), "worker did not attach");
        UUID workerId = worker.getUUID();
        int originalWaypoint = controller.waypointIndex();
        helper.assertTrue(controller.tick(level, machinePos, true), "reached waypoint was not counted");
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
                restored.tick(level, machinePos, true),
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

    private static void buildFloor(GameTestHelper helper) {
        for (int x = 1; x <= 7; x++) {
            for (int z = 1; z <= 7; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
                helper.setBlock(new BlockPos(x, 2, z), Blocks.AIR);
                helper.setBlock(new BlockPos(x, 3, z), Blocks.AIR);
            }
        }
    }
}
