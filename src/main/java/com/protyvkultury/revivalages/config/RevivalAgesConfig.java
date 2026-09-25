package com.protyvkultury.revivalages.config;

import com.protyvkultury.revivalages.feature.food.spoilage.FoodSpoilageConfig;
import com.protyvkultury.revivalages.feature.food.spoilage.client.FoodSpoilageClientConfig;
import com.protyvkultury.revivalages.feature.inventory.carriedweight.CarriedWeightConfig;
import com.protyvkultury.revivalages.feature.inventory.carriedweight.client.CarriedWeightClientConfig;
import com.protyvkultury.revivalages.feature.inventory.itemsize.ItemSizeConfig;
import com.protyvkultury.revivalages.feature.player.diet.DietConfig;
import com.protyvkultury.revivalages.feature.player.diet.client.DietClientConfig;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerConfig;
import com.protyvkultury.revivalages.feature.technology.constructionframe.ConstructionFrameConfig;
import com.protyvkultury.revivalages.feature.technology.dryingrack.config.DryingRackClientConfig;
import com.protyvkultury.revivalages.feature.technology.dryingrack.config.DryingRackConfig;
import com.protyvkultury.revivalages.feature.technology.knapping.KnappingConfig;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyClientConfig;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.world.structuralintegrity.StructuralIntegrityConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Owns the single configuration file used by Revival Ages. */
public final class RevivalAgesConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    static {
        ItemSizeConfig.bootstrap();
        CarriedWeightConfig.bootstrap();
        CarriedWeightClientConfig.bootstrap();
        FoodSpoilageConfig.bootstrap();
        FoodSpoilageClientConfig.bootstrap();
        DietConfig.bootstrap();
        DietClientConfig.bootstrap();
        PrimitiveTechnologyConfig.bootstrap();
        PrimitiveTechnologyClientConfig.bootstrap();
        DryingRackConfig.bootstrap();
        DryingRackClientConfig.bootstrap();
        KnappingConfig.bootstrap();
        ConstructionFrameConfig.bootstrap();
        AnimalPowerConfig.bootstrap();
        StructuralIntegrityConfig.bootstrap();
        SPEC = BUILDER.build();
    }

    private RevivalAgesConfig() {
    }

    public static ModConfigSpec.Builder builder() {
        return BUILDER;
    }

    public static boolean isLoaded() {
        return SPEC.isLoaded();
    }
}
