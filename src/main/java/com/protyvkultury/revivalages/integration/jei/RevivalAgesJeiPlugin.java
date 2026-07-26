package com.protyvkultury.revivalages.integration.jei;

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
import com.protyvkultury.revivalages.feature.technology.constructionframe.view.FrameAssemblyRecipeView;
import com.protyvkultury.revivalages.feature.technology.knapping.view.KnappingRecipeCatalog;
import com.protyvkultury.revivalages.feature.technology.knapping.view.KnappingRecipeView;
import com.protyvkultury.revivalages.feature.worldgen.surfacedeposit.SurfaceDepositFeature;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
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
    public static final RecipeType<PrimitiveRecipeView> PIT_BURN =
            RevivalAgesJeiPlugin.primitive("pit_burn");
    public static final RecipeType<PrimitiveRecipeView> BARREL =
            RevivalAgesJeiPlugin.primitive("barrel");
    public static final RecipeType<PrimitiveRecipeView> SOAKING_POT =
            RevivalAgesJeiPlugin.primitive("soaking_pot");
    public static final RecipeType<PrimitiveRecipeView> TANNING_RACK =
            RevivalAgesJeiPlugin.primitive("tanning_rack");
    public static final RecipeType<PrimitiveRecipeView> STONE_SAWMILL =
            RevivalAgesJeiPlugin.primitive("stone_sawmill");
    public static final RecipeType<PrimitiveRecipeView> STONE_OVEN =
            RevivalAgesJeiPlugin.primitive("stone_oven");
    public static final RecipeType<PrimitiveRecipeView> STONE_KILN =
            RevivalAgesJeiPlugin.primitive("stone_kiln");
    public static final RecipeType<PrimitiveRecipeView> STONE_CRUCIBLE =
            RevivalAgesJeiPlugin.primitive("stone_crucible");
    public static final RecipeType<PrimitiveRecipeView> ANVIL =
            RevivalAgesJeiPlugin.primitive("anvil");
    public static final RecipeType<PrimitiveRecipeView> GRINDING =
            RevivalAgesJeiPlugin.primitive("grinding");
    public static final RecipeType<PrimitiveRecipeView> PRESSING =
            RevivalAgesJeiPlugin.primitive("pressing");
    public static final RecipeType<FrameAssemblyRecipeView> FRAME_ASSEMBLY =
            RecipeType.create("revivalages", "frame_assembly", FrameAssemblyRecipeView.class);
    public static final RecipeType<KnappingRecipeView> ROCK_KNAPPING =
            RecipeType.create("revivalages", "rock_knapping", KnappingRecipeView.class);
    public static final RecipeType<KnappingRecipeView> CLAY_KNAPPING =
            RecipeType.create("revivalages", "clay_knapping", KnappingRecipeView.class);
    public static final RecipeType<KnappingRecipeView> LEATHER_KNAPPING =
            RecipeType.create("revivalages", "leather_knapping", KnappingRecipeView.class);
    public static final RecipeType<KnappingRecipeView> HORN_KNAPPING =
            RecipeType.create("revivalages", "horn_knapping", KnappingRecipeView.class);

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
                            PIT_BURN,
                            PrimitiveJeiCategory.Layout.PIT_KILN,
                            "jei.revivalages.category.pit_burn",
                            (Item) PitBurnFeature.LOG_PILE_ITEM.get()),
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
                            (Item) TanningRackFeature.TANNING_RACK_ITEM.get()),
                    new PrimitiveJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(), STONE_SAWMILL,
                            PrimitiveJeiCategory.Layout.STONE_SAWMILL,
                            "jei.revivalages.category.stone_sawmill",
                            (Item) StoneMachineFeature.STONE_SAWMILL_ITEM.get()),
                    new PrimitiveJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(), STONE_OVEN,
                            PrimitiveJeiCategory.Layout.STONE_OVEN,
                            "jei.revivalages.category.stone_oven",
                            (Item) StoneMachineFeature.STONE_OVEN_ITEM.get()),
                    new PrimitiveJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(), STONE_KILN,
                            PrimitiveJeiCategory.Layout.STONE_KILN,
                            "jei.revivalages.category.stone_kiln",
                            (Item) StoneMachineFeature.STONE_KILN_ITEM.get()),
                    new PrimitiveJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(), STONE_CRUCIBLE,
                            PrimitiveJeiCategory.Layout.STONE_CRUCIBLE,
                            "jei.revivalages.category.stone_crucible",
                            (Item) StoneMachineFeature.STONE_CRUCIBLE_ITEM.get()),
                    new PrimitiveJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(), ANVIL,
                            PrimitiveJeiCategory.Layout.ANVIL,
                            "jei.revivalages.category.anvil",
                            (Item) AnvilFeature.ANVIL_ITEM.get()),
                    new PrimitiveJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(), GRINDING,
                            PrimitiveJeiCategory.Layout.GRINDING,
                            "jei.revivalages.category.grinding",
                            (Item) AnimalPowerFeature.HORSE_GRINDSTONE_ITEM.get()),
                    new PrimitiveJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(), PRESSING,
                            PrimitiveJeiCategory.Layout.PRESSING,
                            "jei.revivalages.category.pressing",
                            (Item) AnimalPowerFeature.HORSE_PRESS_ITEM.get()),
                    new FrameAssemblyJeiCategory(registration.getJeiHelpers(), FRAME_ASSEMBLY),
                    new KnappingJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(),
                            ROCK_KNAPPING,
                            SurfaceDepositFeature.ROCK.get()
                    ),
                    new KnappingJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(),
                            CLAY_KNAPPING,
                            net.minecraft.world.item.Items.CLAY_BALL
                    ),
                    new KnappingJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(),
                            LEATHER_KNAPPING,
                            net.minecraft.world.item.Items.LEATHER
                    ),
                    new KnappingJeiCategory(
                            registration.getJeiHelpers().getGuiHelper(),
                            HORN_KNAPPING,
                            net.minecraft.world.item.Items.GOAT_HORN
                    )
                });
    }

    public void registerRecipes(IRecipeRegistration registration) {
        java.util.List<net.minecraft.world.item.ItemStack> disabledItems = registration.getIngredientManager()
                .getAllItemStacks()
                .stream()
                .filter(stack -> net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(stack.getItem()).getNamespace().equals(RevivalAges.MOD_ID))
                .filter(stack -> !ContentAvailability.isItemEnabled(stack.getItem()))
                .toList();
        if (!disabledItems.isEmpty()) {
            registration.getIngredientManager()
                    .removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, disabledItems);
        }
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
        registration.addRecipes(PIT_BURN, PrimitiveRecipeCatalog.pitBurn(recipeManager));
        registration.addRecipes(BARREL, PrimitiveRecipeCatalog.barrel(recipeManager));
        registration.addRecipes(SOAKING_POT, PrimitiveRecipeCatalog.soakingPot(recipeManager));
        registration.addRecipes(TANNING_RACK, PrimitiveRecipeCatalog.tanningRack(recipeManager));
        registration.addRecipes(STONE_SAWMILL, StoneTechnologyRecipeCatalog.sawmill(recipeManager));
        registration.addRecipes(STONE_OVEN, StoneTechnologyRecipeCatalog.oven(Minecraft.getInstance().level));
        registration.addRecipes(STONE_KILN, StoneTechnologyRecipeCatalog.kiln(Minecraft.getInstance().level));
        registration.addRecipes(STONE_CRUCIBLE,
                StoneTechnologyRecipeCatalog.crucible(Minecraft.getInstance().level));
        registration.addRecipes(ANVIL, StoneTechnologyRecipeCatalog.anvil(recipeManager));
        registration.addRecipes(GRINDING, AnimalPowerRecipeCatalog.grinding(recipeManager));
        registration.addRecipes(PRESSING, AnimalPowerRecipeCatalog.pressing(recipeManager));
        registration.addRecipes(FRAME_ASSEMBLY, FrameAssemblyRecipeCatalog.recipes(recipeManager));
        java.util.List<KnappingRecipeView> knapping =
                KnappingRecipeCatalog.recipes(recipeManager, registries);
        registration.addRecipes(
                ROCK_KNAPPING,
                knapping.stream().filter(view -> !java.util.Set.of("clay", "leather", "horn")
                        .contains(view.type().getPath())).toList()
        );
        registration.addRecipes(
                CLAY_KNAPPING,
                knapping.stream().filter(view -> view.type().getPath().equals("clay")).toList()
        );
        registration.addRecipes(
                LEATHER_KNAPPING,
                knapping.stream().filter(view -> view.type().getPath().equals("leather")).toList()
        );
        registration.addRecipes(
                HORN_KNAPPING,
                knapping.stream().filter(view -> view.type().getPath().equals("horn")).toList()
        );
    }

    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        catalyst(registration, ContentKey.CRUDE_DRYING_RACK,
                DryingRackFeature.CRUDE_DRYING_RACK_ITEM.get(), CRUDE_DRYING);
        catalyst(registration, ContentKey.DRYING_RACK,
                DryingRackFeature.DRYING_RACK_ITEM.get(), DRYING);
        if (ContentAvailability.isEnabled(ContentKey.CRUDE_DRYING_RACK)
                && ContentAvailability.isEnabled(ContentKey.DRYING_RACK)) {
            registration.addRecipeCatalyst(DryingRackFeature.DRYING_RACK_ITEM.get(), CRUDE_DRYING);
        }
        catalyst(registration, ContentKey.CAMPFIRE, CampfireFeature.TINDER.get(), CAMPFIRE);
        catalyst(registration, ContentKey.CHOPPING_BLOCK, ChoppingBlockFeature.CHOPPING_BLOCK_ITEM.get(), CHOPPING);
        catalyst(registration, ContentKey.PIT_KILN, PitKilnFeature.PIT_KILN_ITEM.get(), PIT_KILN);
        catalyst(registration, ContentKey.PIT_BURN, PitBurnFeature.LOG_PILE_ITEM.get(), PIT_BURN);
        catalyst(registration, ContentKey.BARREL, BarrelFeature.BARREL_ITEM.get(), BARREL);
        catalyst(registration, ContentKey.SOAKING_POT, SoakingPotFeature.SOAKING_POT_ITEM.get(), SOAKING_POT);
        catalyst(registration, ContentKey.TANNING_RACK, TanningRackFeature.TANNING_RACK_ITEM.get(), TANNING_RACK);
        catalyst(registration, ContentKey.STONE_SAWMILL, StoneMachineFeature.STONE_SAWMILL_ITEM.get(), STONE_SAWMILL);
        catalyst(registration, ContentKey.STONE_OVEN, StoneMachineFeature.STONE_OVEN_ITEM.get(), STONE_OVEN);
        catalyst(registration, ContentKey.STONE_KILN, StoneMachineFeature.STONE_KILN_ITEM.get(), STONE_KILN);
        catalyst(registration, ContentKey.STONE_CRUCIBLE, StoneMachineFeature.STONE_CRUCIBLE_ITEM.get(), STONE_CRUCIBLE);
        catalyst(registration, ContentKey.ANVIL, AnvilFeature.ANVIL_ITEM.get(), ANVIL);
        catalyst(registration, ContentKey.HAND_GRINDSTONE, AnimalPowerFeature.HAND_GRINDSTONE_ITEM.get(), GRINDING);
        catalyst(registration, ContentKey.HORSE_GRINDSTONE, AnimalPowerFeature.HORSE_GRINDSTONE_ITEM.get(), GRINDING);
        catalyst(registration, ContentKey.HORSE_CHOPPING_BLOCK,
                AnimalPowerFeature.HORSE_CHOPPING_BLOCK_ITEM.get(), CHOPPING);
        catalyst(registration, ContentKey.HORSE_PRESS, AnimalPowerFeature.HORSE_PRESS_ITEM.get(), PRESSING);
        catalyst(registration, ContentKey.CONSTRUCTION_FRAME,
                ConstructionFrameFeature.CONSTRUCTION_FRAME_ITEM.get(), FRAME_ASSEMBLY);
    }

    private static void catalyst(
            IRecipeCatalystRegistration registration,
            ContentKey content,
            ItemLike item,
            RecipeType<?>... recipeTypes
    ) {
        if (ContentAvailability.isEnabled(content)) {
            registration.addRecipeCatalyst(item, recipeTypes);
        }
    }
}
