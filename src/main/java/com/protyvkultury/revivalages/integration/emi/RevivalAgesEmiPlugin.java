package com.protyvkultury.revivalages.integration.emi;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingBlockFeature;
import com.protyvkultury.revivalages.feature.technology.dryingrack.DryingRackFeature;
import com.protyvkultury.revivalages.feature.technology.dryingrack.view.DryingRecipeCatalog;
import com.protyvkultury.revivalages.feature.technology.dryingrack.view.DryingRecipeView;
import com.protyvkultury.revivalages.feature.technology.pitkiln.PitKilnFeature;
import com.protyvkultury.revivalages.feature.technology.pitburn.PitBurnFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeCatalog;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeView;
import com.protyvkultury.revivalages.feature.technology.soakingpot.SoakingPotFeature;
import com.protyvkultury.revivalages.feature.technology.tanningrack.TanningRackFeature;
import com.protyvkultury.revivalages.feature.technology.anvil.AnvilFeature;
import com.protyvkultury.revivalages.feature.technology.stonemachine.StoneMachineFeature;
import com.protyvkultury.revivalages.feature.technology.stonemachine.view.StoneTechnologyRecipeCatalog;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature;
import com.protyvkultury.revivalages.feature.technology.animalpower.view.AnimalPowerRecipeCatalog;
import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameFeature;
import com.protyvkultury.revivalages.feature.technology.constructionframe.view.FrameAssemblyRecipeCatalog;
import com.protyvkultury.revivalages.feature.technology.knapping.view.KnappingRecipeCatalog;
import com.protyvkultury.revivalages.feature.worldgen.surfacedeposit.SurfaceDepositFeature;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;

@EmiEntrypoint
public final class RevivalAgesEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory CRUDE_DRYING =
            new EmiRecipeCategory(
                    RevivalAges.id("crude_drying"),
                    (EmiRenderable)
                            EmiStack.of((ItemLike) ((ItemLike) DryingRackFeature.CRUDE_DRYING_RACK_ITEM.get())));
    public static final EmiRecipeCategory DRYING =
            new EmiRecipeCategory(
                    RevivalAges.id("drying"),
                    (EmiRenderable)
                            EmiStack.of((ItemLike) ((ItemLike) DryingRackFeature.DRYING_RACK_ITEM.get())));
    public static final EmiRecipeCategory CAMPFIRE =
            RevivalAgesEmiPlugin.category("campfire", (ItemLike) CampfireFeature.TINDER.get());
    public static final EmiRecipeCategory CHOPPING_BLOCK =
            RevivalAgesEmiPlugin.category(
                    "chopping_block", (ItemLike) ChoppingBlockFeature.CHOPPING_BLOCK_ITEM.get());
    public static final EmiRecipeCategory ANIMAL_CHOPPING =
            RevivalAgesEmiPlugin.category(
                    "animal_chopping", (ItemLike) AnimalPowerFeature.HORSE_CHOPPING_BLOCK_ITEM.get());
    public static final EmiRecipeCategory PIT_KILN =
            RevivalAgesEmiPlugin.category("pit_kiln", (ItemLike) PitKilnFeature.PIT_KILN_ITEM.get());
    public static final EmiRecipeCategory PIT_BURN =
            RevivalAgesEmiPlugin.category("pit_burn", (ItemLike) PitBurnFeature.LOG_PILE_ITEM.get());
    public static final EmiRecipeCategory BARREL =
            RevivalAgesEmiPlugin.category("barrel", (ItemLike) BarrelFeature.BARREL_ITEM.get());
    public static final EmiRecipeCategory SOAKING_POT =
            RevivalAgesEmiPlugin.category(
                    "soaking_pot", (ItemLike) SoakingPotFeature.SOAKING_POT_ITEM.get());
    public static final EmiRecipeCategory TANNING_RACK =
            RevivalAgesEmiPlugin.category(
                    "tanning_rack", (ItemLike) TanningRackFeature.TANNING_RACK_ITEM.get());
    public static final EmiRecipeCategory STONE_SAWMILL =
            RevivalAgesEmiPlugin.category("stone_sawmill", StoneMachineFeature.STONE_SAWMILL_ITEM.get());
    public static final EmiRecipeCategory STONE_OVEN =
            RevivalAgesEmiPlugin.category("stone_oven", StoneMachineFeature.STONE_OVEN_ITEM.get());
    public static final EmiRecipeCategory STONE_KILN =
            RevivalAgesEmiPlugin.category("stone_kiln", StoneMachineFeature.STONE_KILN_ITEM.get());
    public static final EmiRecipeCategory STONE_CRUCIBLE =
            RevivalAgesEmiPlugin.category("stone_crucible", StoneMachineFeature.STONE_CRUCIBLE_ITEM.get());
    public static final EmiRecipeCategory ANVIL =
            RevivalAgesEmiPlugin.category("anvil", AnvilFeature.ANVIL_ITEM.get());
    public static final EmiRecipeCategory HAND_GRINDING =
            RevivalAgesEmiPlugin.category("hand_grinding", AnimalPowerFeature.HAND_GRINDSTONE_ITEM.get());
    public static final EmiRecipeCategory ANIMAL_GRINDING =
            RevivalAgesEmiPlugin.category("animal_grinding", AnimalPowerFeature.HORSE_GRINDSTONE_ITEM.get());
    public static final EmiRecipeCategory PRESSING =
            RevivalAgesEmiPlugin.category("pressing", AnimalPowerFeature.HORSE_PRESS_ITEM.get());
    public static final EmiRecipeCategory FRAME_ASSEMBLY =
            RevivalAgesEmiPlugin.category(
                    "frame_assembly",
                    ConstructionFrameFeature.CONSTRUCTION_FRAME_ITEM.get()
            );
    public static final EmiRecipeCategory ROCK_KNAPPING =
            RevivalAgesEmiPlugin.category("rock_knapping", SurfaceDepositFeature.COBBLESTONE_SPLITTER.get());
    public static final EmiRecipeCategory CLAY_KNAPPING =
            RevivalAgesEmiPlugin.category("clay_knapping", net.minecraft.world.item.Items.CLAY_BALL);
    public static final EmiRecipeCategory LEATHER_KNAPPING =
            RevivalAgesEmiPlugin.category("leather_knapping", net.minecraft.world.item.Items.LEATHER);
    public static final EmiRecipeCategory HORN_KNAPPING =
            RevivalAgesEmiPlugin.category("horn_knapping", net.minecraft.world.item.Items.GOAT_HORN);

    private static EmiRecipeCategory category(String id, ItemLike icon) {
        return new EmiRecipeCategory(RevivalAges.id(id), (EmiRenderable) EmiStack.of((ItemLike) icon));
    }

    public void register(EmiRegistry registry) {
        registry.removeEmiStacks(stack -> {
            ItemStack itemStack = stack.getItemStack();
            return !itemStack.isEmpty()
                    && BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getNamespace().equals(RevivalAges.MOD_ID)
                    && !ContentAvailability.isItemEnabled(itemStack.getItem());
        });
        registry.addCategory(CRUDE_DRYING);
        registry.addCategory(DRYING);
        registry.addCategory(CAMPFIRE);
        if (ContentAvailability.isEnabled(ContentKey.CHOPPING_BLOCK)) {
            registry.addCategory(CHOPPING_BLOCK);
        }
        if (ContentAvailability.isEnabled(ContentKey.HORSE_CHOPPING_BLOCK)) {
            registry.addCategory(ANIMAL_CHOPPING);
        }
        registry.addCategory(PIT_KILN);
        registry.addCategory(PIT_BURN);
        registry.addCategory(BARREL);
        registry.addCategory(SOAKING_POT);
        registry.addCategory(TANNING_RACK);
        registry.addCategory(STONE_SAWMILL);
        registry.addCategory(STONE_OVEN);
        registry.addCategory(STONE_KILN);
        registry.addCategory(STONE_CRUCIBLE);
        registry.addCategory(ANVIL);
        if (ContentAvailability.isEnabled(ContentKey.HAND_GRINDSTONE)) {
            registry.addCategory(HAND_GRINDING);
        }
        if (ContentAvailability.isEnabled(ContentKey.HORSE_GRINDSTONE)) {
            registry.addCategory(ANIMAL_GRINDING);
        }
        registry.addCategory(PRESSING);
        registry.addCategory(FRAME_ASSEMBLY);
        registry.addCategory(ROCK_KNAPPING);
        registry.addCategory(CLAY_KNAPPING);
        registry.addCategory(LEATHER_KNAPPING);
        registry.addCategory(HORN_KNAPPING);
        workstation(registry, ContentKey.CRUDE_DRYING_RACK, CRUDE_DRYING,
                DryingRackFeature.CRUDE_DRYING_RACK_ITEM.get());
        if (ContentAvailability.isEnabled(ContentKey.CRUDE_DRYING_RACK)
                && ContentAvailability.isEnabled(ContentKey.DRYING_RACK)) {
            registry.addWorkstation(CRUDE_DRYING, EmiStack.of(DryingRackFeature.DRYING_RACK_ITEM.get()));
        }
        workstation(registry, ContentKey.DRYING_RACK, DRYING, DryingRackFeature.DRYING_RACK_ITEM.get());
        workstation(registry, ContentKey.CAMPFIRE, CAMPFIRE, CampfireFeature.TINDER.get());
        workstation(registry, ContentKey.CHOPPING_BLOCK, CHOPPING_BLOCK, ChoppingBlockFeature.CHOPPING_BLOCK_ITEM.get());
        workstation(registry, ContentKey.PIT_KILN, PIT_KILN, PitKilnFeature.PIT_KILN_ITEM.get());
        workstation(registry, ContentKey.PIT_BURN, PIT_BURN, PitBurnFeature.LOG_PILE_ITEM.get());
        workstation(registry, ContentKey.BARREL, BARREL, BarrelFeature.BARREL_ITEM.get());
        workstation(registry, ContentKey.SOAKING_POT, SOAKING_POT, SoakingPotFeature.SOAKING_POT_ITEM.get());
        workstation(registry, ContentKey.TANNING_RACK, TANNING_RACK, TanningRackFeature.TANNING_RACK_ITEM.get());
        workstation(registry, ContentKey.STONE_SAWMILL, STONE_SAWMILL, StoneMachineFeature.STONE_SAWMILL_ITEM.get());
        workstation(registry, ContentKey.STONE_OVEN, STONE_OVEN, StoneMachineFeature.STONE_OVEN_ITEM.get());
        workstation(registry, ContentKey.STONE_KILN, STONE_KILN, StoneMachineFeature.STONE_KILN_ITEM.get());
        workstation(registry, ContentKey.STONE_CRUCIBLE, STONE_CRUCIBLE,
                StoneMachineFeature.STONE_CRUCIBLE_ITEM.get());
        workstation(registry, ContentKey.ANVIL, ANVIL, AnvilFeature.ANVIL_ITEM.get());
        workstation(registry, ContentKey.HAND_GRINDSTONE, HAND_GRINDING, AnimalPowerFeature.HAND_GRINDSTONE_ITEM.get());
        workstation(registry, ContentKey.HORSE_GRINDSTONE, ANIMAL_GRINDING,
                AnimalPowerFeature.HORSE_GRINDSTONE_ITEM.get());
        workstation(registry, ContentKey.HORSE_CHOPPING_BLOCK, ANIMAL_CHOPPING,
                AnimalPowerFeature.HORSE_CHOPPING_BLOCK_ITEM.get());
        workstation(registry, ContentKey.HORSE_PRESS, PRESSING, AnimalPowerFeature.HORSE_PRESS_ITEM.get());
        workstation(registry, ContentKey.CONSTRUCTION_FRAME, FRAME_ASSEMBLY,
                ConstructionFrameFeature.CONSTRUCTION_FRAME_ITEM.get());
        DryingRecipeCatalog.crude(registry.getRecipeManager())
                .forEach(
                        view ->
                                registry.addRecipe(
                                        (EmiRecipe)
                                                new DryingEmiRecipe(CRUDE_DRYING, "crude", (DryingRecipeView) view)));
        DryingRecipeCatalog.normal(registry.getRecipeManager())
                .forEach(
                        view ->
                                registry.addRecipe(
                                        (EmiRecipe) new DryingEmiRecipe(DRYING, "normal", (DryingRecipeView) view)));
        RecipeManager manager = registry.getRecipeManager();
        RegistryAccess registries = Minecraft.getInstance().level.registryAccess();
        PrimitiveRecipeCatalog.campfire(manager, (HolderLookup.Provider) registries)
                .forEach(
                        view ->
                                registry.addRecipe(
                                        (EmiRecipe)
                                                new PrimitiveEmiRecipe(
                                                        CAMPFIRE,
                                                        PrimitiveEmiRecipe.Layout.CAMPFIRE,
                                                        (PrimitiveRecipeView) view)));
        PrimitiveRecipeCatalog.choppingBlock(manager)
                .forEach(
                        view ->
                                registry.addRecipe(
                                        (EmiRecipe)
                                                new PrimitiveEmiRecipe(
                                                        CHOPPING_BLOCK,
                                                        PrimitiveEmiRecipe.Layout.CHOPPING,
                                                        "chopping_block",
                                                        (PrimitiveRecipeView) view)));
        AnimalPowerRecipeCatalog.animalChopping(manager).forEach(view -> registry.addRecipe(
                new PrimitiveEmiRecipe(
                        ANIMAL_CHOPPING, PrimitiveEmiRecipe.Layout.CHOPPING, "animal_chopping", view)));
        PrimitiveRecipeCatalog.pitKiln(manager)
                .forEach(
                        view ->
                                registry.addRecipe(
                                        (EmiRecipe)
                                                new PrimitiveEmiRecipe(
                                                        PIT_KILN,
                                                        PrimitiveEmiRecipe.Layout.PIT_KILN,
                                                        (PrimitiveRecipeView) view)));
        PrimitiveRecipeCatalog.barrel(manager)
                .forEach(
                        view ->
                                registry.addRecipe(
                                        (EmiRecipe)
                                                new PrimitiveEmiRecipe(
                                                        BARREL, PrimitiveEmiRecipe.Layout.BARREL, (PrimitiveRecipeView) view)));
        PrimitiveRecipeCatalog.soakingPot(manager)
                .forEach(
                        view ->
                                registry.addRecipe(
                                        (EmiRecipe)
                                                new PrimitiveEmiRecipe(
                                                        SOAKING_POT,
                                                        PrimitiveEmiRecipe.Layout.SOAKING_POT,
                                                        (PrimitiveRecipeView) view)));
        PrimitiveRecipeCatalog.tanningRack(manager)
                .forEach(
                        view ->
                                registry.addRecipe(
                                        (EmiRecipe)
                                                new PrimitiveEmiRecipe(
                                                        TANNING_RACK,
                                                        PrimitiveEmiRecipe.Layout.TANNING_RACK,
                                                        (PrimitiveRecipeView) view)));
        PrimitiveRecipeCatalog.pitBurn(manager)
                .forEach(view -> registry.addRecipe(
                        new PrimitiveEmiRecipe(PIT_BURN, PrimitiveEmiRecipe.Layout.PIT_BURN, view)));
        StoneTechnologyRecipeCatalog.sawmill(manager).forEach(view -> registry.addRecipe(
                new PrimitiveEmiRecipe(STONE_SAWMILL, PrimitiveEmiRecipe.Layout.STONE_SAWMILL, view)));
        StoneTechnologyRecipeCatalog.oven(Minecraft.getInstance().level).forEach(view -> registry.addRecipe(
                new PrimitiveEmiRecipe(STONE_OVEN, PrimitiveEmiRecipe.Layout.STONE_OVEN, view)));
        StoneTechnologyRecipeCatalog.kiln(Minecraft.getInstance().level).forEach(view -> registry.addRecipe(
                new PrimitiveEmiRecipe(STONE_KILN, PrimitiveEmiRecipe.Layout.STONE_KILN, view)));
        StoneTechnologyRecipeCatalog.crucible(Minecraft.getInstance().level).forEach(view -> registry.addRecipe(
                new PrimitiveEmiRecipe(STONE_CRUCIBLE, PrimitiveEmiRecipe.Layout.STONE_CRUCIBLE, view)));
        StoneTechnologyRecipeCatalog.anvil(manager).forEach(view -> registry.addRecipe(
                new PrimitiveEmiRecipe(ANVIL, PrimitiveEmiRecipe.Layout.ANVIL, view)));
        AnimalPowerRecipeCatalog.handGrinding(manager).forEach(view -> registry.addRecipe(
                new PrimitiveEmiRecipe(HAND_GRINDING, PrimitiveEmiRecipe.Layout.GRINDING, "hand_grinding", view)));
        AnimalPowerRecipeCatalog.animalGrinding(manager).forEach(view -> registry.addRecipe(
                new PrimitiveEmiRecipe(
                        ANIMAL_GRINDING, PrimitiveEmiRecipe.Layout.GRINDING, "animal_grinding", view)));
        AnimalPowerRecipeCatalog.pressing(manager).forEach(view -> registry.addRecipe(
                new PrimitiveEmiRecipe(PRESSING, PrimitiveEmiRecipe.Layout.PRESSING, view)));
        FrameAssemblyRecipeCatalog.recipes(manager).forEach(view -> registry.addRecipe(
                new FrameAssemblyEmiRecipe(FRAME_ASSEMBLY, view)));
        KnappingRecipeCatalog.recipes(manager, registries).forEach(view -> {
            EmiRecipeCategory category = switch (view.type().getPath()) {
                case "clay" -> CLAY_KNAPPING;
                case "leather" -> LEATHER_KNAPPING;
                case "horn" -> HORN_KNAPPING;
                default -> ROCK_KNAPPING;
            };
            registry.addRecipe(new KnappingEmiRecipe(category, view));
        });
    }

    private static void workstation(
            EmiRegistry registry,
            ContentKey content,
            EmiRecipeCategory category,
            ItemLike item
    ) {
        if (ContentAvailability.isEnabled(content)) {
            registry.addWorkstation(category, EmiStack.of(item));
        }
    }

}
