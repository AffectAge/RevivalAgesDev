package com.protyvkultury.revivalages.gametest;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameFeature;
import com.protyvkultury.revivalages.feature.technology.constructionframe.blockentity.ConstructionFrameBlockEntity;
import com.protyvkultury.revivalages.feature.technology.constructionframe.recipe.FrameAssemblyRecipe;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.connection.ConnectionType;

@GameTestHolder(RevivalAges.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ConstructionFrameGameTests {

    private static final BlockPos FRAME = new BlockPos(4, 2, 4);

    private ConstructionFrameGameTests() {
    }

    @GameTest(template = "animal_power_empty")
    public static void everyCellStoresExactlyOneItem(GameTestHelper helper) {
        helper.setBlock(FRAME, ConstructionFrameFeature.CONSTRUCTION_FRAME.get());
        if (!(helper.getBlockEntity(FRAME) instanceof ConstructionFrameBlockEntity frame)) {
            helper.fail("Construction Frame block entity was not created", FRAME);
            return;
        }
        for (int slot = 0; slot < 27; slot++) {
            ItemStack source = new ItemStack(Items.STONE, 2);
            helper.assertTrue(frame.insert(slot, source, false), "cell rejected first item");
            helper.assertValueEqual(source.getCount(), 1, "cell consumed more than one item");
            helper.assertFalse(frame.insert(slot, source, false), "occupied cell accepted another item");
        }
        helper.assertValueEqual(frame.occupiedCells(), 27, "occupancy count");
        for (int slot = 0; slot < 27; slot++) {
            helper.assertTrue(frame.extract(slot).is(Items.STONE), "cell returned the wrong item");
        }
        helper.assertValueEqual(frame.occupiedCells(), 0, "occupancy count after extraction");
        helper.succeed();
    }

    @GameTest(template = "animal_power_empty")
    public static void frameRecipeStreamCodecRoundTrips(GameTestHelper helper) {
        FrameAssemblyRecipe recipe = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(ConstructionFrameFeature.RECIPE_TYPE.get())
                .getFirst()
                .value();
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(
                Unpooled.buffer(),
                helper.getLevel().registryAccess(),
                ConnectionType.NEOFORGE
        );
        try {
            ConstructionFrameFeature.RECIPE_SERIALIZER.get().streamCodec().encode(buffer, recipe);
            FrameAssemblyRecipe decoded =
                    ConstructionFrameFeature.RECIPE_SERIALIZER.get().streamCodec().decode(buffer);
            helper.assertValueEqual(decoded.getIngredients().size(), 27, "ingredient count");
            helper.assertTrue(
                    ItemStack.isSameItemSameComponents(decoded.result(), recipe.result()),
                    "result changed during stream-codec round trip"
            );
            helper.assertValueEqual(
                    decoded.woodVariantSource(),
                    recipe.woodVariantSource(),
                    "wood variant source"
            );
        } finally {
            buffer.release();
        }
        helper.succeed();
    }
}
