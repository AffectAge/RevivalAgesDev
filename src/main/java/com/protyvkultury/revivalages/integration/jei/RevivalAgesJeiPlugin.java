package com.protyvkultury.revivalages.integration.jei;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingBlockFeature;
import com.protyvkultury.revivalages.feature.technology.dryingrack.DryingRackFeature;
import com.protyvkultury.revivalages.feature.technology.dryingrack.view.DryingRecipeCatalog;
import com.protyvkultury.revivalages.feature.technology.dryingrack.view.DryingRecipeView;
import com.protyvkultury.revivalages.feature.technology.pitkiln.PitKilnFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeCatalog;
import com.protyvkultury.revivalages.feature.technology.primitive.view.PrimitiveRecipeView;
import com.protyvkultury.revivalages.feature.technology.soakingpot.SoakingPotFeature;
import com.protyvkultury.revivalages.feature.technology.tanningrack.TanningRackFeature;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;

@JeiPlugin
public final class RevivalAgesJeiPlugin implements IModPlugin {
    public static final RecipeType<DryingRecipeView> CRUDE_DRYING =
            RecipeType.create((String) "revivalages", (String) "crude_drying", DryingRecipeView.class);
    public static final RecipeType<DryingRecipeView> DRYING =
            RecipeType.create((String) "revivalages", (String) "drying", DryingRecipeView.class);
    public static final RecipeType<PrimitiveRecipeView> CAMPFIRE =
            RevivalAgesJeiPlugin.primitive("campfire");
    public static final RecipeType<PrimitiveRecipeView> CHOPPING =
            RevivalAgesJeiPlugin.primitive("chopping");
    public static final RecipeType<PrimitiveRecipeView> PIT_KILN =
            RevivalAgesJeiPlugin.primitive("pit_kiln");
    public static final RecipeType<PrimitiveRecipeView> BARREL =
            RevivalAgesJeiPlugin.primitive("barrel");
    public static final RecipeType<PrimitiveRecipeView> SOAKING_POT =
            RevivalAgesJeiPlugin.primitive("soaking_pot");
    public static final RecipeType<PrimitiveRecipeView> TANNING_RACK =
            RevivalAgesJeiPlugin.primitive("tanning_rack");

    private static RecipeType<PrimitiveRecipeView> primitive(String path) {
        return RecipeType.create((String) "revivalages", (String) path, PrimitiveRecipeView.class);
    }

    public ResourceLocation getPluginUid() {
        return RevivalAges.id("jei");
    }

    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new IRecipeCategory[] {
                    new DryingJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(),
                            CRUDE_DRYING,
                            "jei.revivalages.category.crude_drying",
                            (Item) DryingRackFeature.CRUDE_DRYING_RACK_ITEM.get()),
                    new DryingJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(),
                            DRYING,
                            "jei.revivalages.category.drying",
                            (Item) DryingRackFeature.DRYING_RACK_ITEM.get()),
                    new PrimitiveJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(),
                            CAMPFIRE,
                            PrimitiveJeiCategory.Layout.CAMPFIRE,
                            "jei.revivalages.category.campfire",
                            (Item) CampfireFeature.TINDER.get()),
                    new PrimitiveJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(),
                            CHOPPING,
                            PrimitiveJeiCategory.Layout.CHOPPING,
                            "jei.revivalages.category.chopping",
                            (Item) ChoppingBlockFeature.CHOPPING_BLOCK_ITEM.get()),
                    new PrimitiveJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(),
                            PIT_KILN,
                            PrimitiveJeiCategory.Layout.PIT_KILN,
                            "jei.revivalages.category.pit_kiln",
                            (Item) PitKilnFeature.PIT_KILN_ITEM.get()),
                    new PrimitiveJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(),
                            BARREL,
                            PrimitiveJeiCategory.Layout.BARREL,
                            "jei.revivalages.category.barrel",
                            (Item) BarrelFeature.BARREL_ITEM.get()),
                    new PrimitiveJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(),
                            SOAKING_POT,
                            PrimitiveJeiCategory.Layout.SOAKING_POT,
                            "jei.revivalages.category.soaking_pot",
                            (Item) SoakingPotFeature.SOAKING_POT_ITEM.get()),
                    new PrimitiveJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(),
                            TANNING_RACK,
                            PrimitiveJeiCategory.Layout.TANNING_RACK,
                            "jei.revivalages.category.tanning_rack",
                            (Item) TanningRackFeature.TANNING_RACK_ITEM.get())
                });
    }

    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(CRUDE_DRYING, DryingRecipeCatalog.crude(recipeManager));
        registration.addRecipes(DRYING, DryingRecipeCatalog.normal(recipeManager));
        RegistryAccess registries = Minecraft.getInstance().level.registryAccess();
        registration.addRecipes(
                CAMPFIRE,
                PrimitiveRecipeCatalog.campfire(recipeManager, (HolderLookup.Provider) registries));
        registration.addRecipes(CHOPPING, PrimitiveRecipeCatalog.chopping(recipeManager));
        registration.addRecipes(PIT_KILN, PrimitiveRecipeCatalog.pitKiln(recipeManager));
        registration.addRecipes(BARREL, PrimitiveRecipeCatalog.barrel(recipeManager));
        registration.addRecipes(SOAKING_POT, PrimitiveRecipeCatalog.soakingPot(recipeManager));
        registration.addRecipes(TANNING_RACK, PrimitiveRecipeCatalog.tanningRack(recipeManager));
    }

    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                (ItemLike) DryingRackFeature.CRUDE_DRYING_RACK_ITEM.get(), new RecipeType[] {CRUDE_DRYING});
        registration.addRecipeCatalyst(
                (ItemLike) DryingRackFeature.DRYING_RACK_ITEM.get(),
                new RecipeType[] {CRUDE_DRYING, DRYING});
        registration.addRecipeCatalyst(
                (ItemLike) CampfireFeature.TINDER.get(), new RecipeType[] {CAMPFIRE});
        registration.addRecipeCatalyst(
                (ItemLike) ChoppingBlockFeature.CHOPPING_BLOCK_ITEM.get(), new RecipeType[] {CHOPPING});
        registration.addRecipeCatalyst(
                (ItemLike) PitKilnFeature.PIT_KILN_ITEM.get(), new RecipeType[] {PIT_KILN});
        registration.addRecipeCatalyst(
                (ItemLike) BarrelFeature.BARREL_ITEM.get(), new RecipeType[] {BARREL});
        registration.addRecipeCatalyst(
                (ItemLike) SoakingPotFeature.SOAKING_POT_ITEM.get(), new RecipeType[] {SOAKING_POT});
        registration.addRecipeCatalyst(
                (ItemLike) TanningRackFeature.TANNING_RACK_ITEM.get(), new RecipeType[] {TANNING_RACK});
    }
}
