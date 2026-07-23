package com.protyvkultury.revivalages.integration.emi;

import com.protyvkultury.revivalages.RevivalAges;
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
    public static final EmiRecipeCategory CHOPPING =
            RevivalAgesEmiPlugin.category(
                    "chopping", (ItemLike) ChoppingBlockFeature.CHOPPING_BLOCK_ITEM.get());
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
    public static final EmiRecipeCategory GRINDING =
            RevivalAgesEmiPlugin.category("grinding", AnimalPowerFeature.HORSE_GRINDSTONE_ITEM.get());
    public static final EmiRecipeCategory PRESSING =
            RevivalAgesEmiPlugin.category("pressing", AnimalPowerFeature.HORSE_PRESS_ITEM.get());

    private static EmiRecipeCategory category(String id, ItemLike icon) {
        return new EmiRecipeCategory(RevivalAges.id(id), (EmiRenderable) EmiStack.of((ItemLike) icon));
    }

    public void register(EmiRegistry registry) {
        registry.addCategory(CRUDE_DRYING);
        registry.addCategory(DRYING);
        registry.addCategory(CAMPFIRE);
        registry.addCategory(CHOPPING);
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
        registry.addCategory(GRINDING);
        registry.addCategory(PRESSING);
        registry.addWorkstation(
                CRUDE_DRYING,
                (EmiIngredient)
                        EmiStack.of((ItemLike) ((ItemLike) DryingRackFeature.CRUDE_DRYING_RACK_ITEM.get())));
        registry.addWorkstation(
                CRUDE_DRYING,
                (EmiIngredient)
                        EmiStack.of((ItemLike) ((ItemLike) DryingRackFeature.DRYING_RACK_ITEM.get())));
        registry.addWorkstation(
                DRYING,
                (EmiIngredient)
                        EmiStack.of((ItemLike) ((ItemLike) DryingRackFeature.DRYING_RACK_ITEM.get())));
        registry.addWorkstation(
                CAMPFIRE,
                (EmiIngredient) EmiStack.of((ItemLike) ((ItemLike) CampfireFeature.TINDER.get())));
        registry.addWorkstation(
                CHOPPING,
                (EmiIngredient)
                        EmiStack.of((ItemLike) ((ItemLike) ChoppingBlockFeature.CHOPPING_BLOCK_ITEM.get())));
        registry.addWorkstation(
                PIT_KILN,
                (EmiIngredient) EmiStack.of((ItemLike) ((ItemLike) PitKilnFeature.PIT_KILN_ITEM.get())));
        registry.addWorkstation(PIT_BURN, EmiStack.of(PitBurnFeature.LOG_PILE_ITEM.get()));
        registry.addWorkstation(
                BARREL,
                (EmiIngredient) EmiStack.of((ItemLike) ((ItemLike) BarrelFeature.BARREL_ITEM.get())));
        registry.addWorkstation(
                SOAKING_POT,
                (EmiIngredient)
                        EmiStack.of((ItemLike) ((ItemLike) SoakingPotFeature.SOAKING_POT_ITEM.get())));
        registry.addWorkstation(
                TANNING_RACK,
                (EmiIngredient)
                        EmiStack.of((ItemLike) ((ItemLike) TanningRackFeature.TANNING_RACK_ITEM.get())));
        registry.addWorkstation(STONE_SAWMILL, EmiStack.of(StoneMachineFeature.STONE_SAWMILL_ITEM.get()));
        registry.addWorkstation(STONE_OVEN, EmiStack.of(StoneMachineFeature.STONE_OVEN_ITEM.get()));
        registry.addWorkstation(STONE_KILN, EmiStack.of(StoneMachineFeature.STONE_KILN_ITEM.get()));
        registry.addWorkstation(STONE_CRUCIBLE, EmiStack.of(StoneMachineFeature.STONE_CRUCIBLE_ITEM.get()));
        registry.addWorkstation(ANVIL, EmiStack.of(AnvilFeature.ANVIL_ITEM.get()));
        registry.addWorkstation(GRINDING, EmiStack.of(AnimalPowerFeature.HAND_GRINDSTONE_ITEM.get()));
        registry.addWorkstation(GRINDING, EmiStack.of(AnimalPowerFeature.HORSE_GRINDSTONE_ITEM.get()));
        registry.addWorkstation(CHOPPING, EmiStack.of(AnimalPowerFeature.HORSE_CHOPPING_BLOCK_ITEM.get()));
        registry.addWorkstation(PRESSING, EmiStack.of(AnimalPowerFeature.HORSE_PRESS_ITEM.get()));
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
        PrimitiveRecipeCatalog.chopping(manager)
                .forEach(
                        view ->
                                registry.addRecipe(
                                        (EmiRecipe)
                                                new PrimitiveEmiRecipe(
                                                        CHOPPING,
                                                        PrimitiveEmiRecipe.Layout.CHOPPING,
                                                        (PrimitiveRecipeView) view)));
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
                        new PrimitiveEmiRecipe(PIT_BURN, PrimitiveEmiRecipe.Layout.PIT_KILN, view)));
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
        AnimalPowerRecipeCatalog.grinding(manager).forEach(view -> registry.addRecipe(
                new PrimitiveEmiRecipe(GRINDING, PrimitiveEmiRecipe.Layout.GRINDING, view)));
        AnimalPowerRecipeCatalog.pressing(manager).forEach(view -> registry.addRecipe(
                new PrimitiveEmiRecipe(PRESSING, PrimitiveEmiRecipe.Layout.PRESSING, view)));
    }
}
