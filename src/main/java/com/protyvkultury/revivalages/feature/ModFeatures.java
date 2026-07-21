package com.protyvkultury.revivalages.feature;

import com.protyvkultury.revivalages.feature.core.CoreFeature;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.choppingblock.ChoppingBlockFeature;
import com.protyvkultury.revivalages.feature.technology.pitkiln.PitKilnFeature;
import com.protyvkultury.revivalages.feature.technology.barrel.BarrelFeature;
import com.protyvkultury.revivalages.feature.technology.soakingpot.SoakingPotFeature;
import com.protyvkultury.revivalages.feature.technology.tanningrack.TanningRackFeature;
import com.protyvkultury.revivalages.feature.technology.dryingrack.DryingRackFeature;
import java.util.List;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

public final class ModFeatures {

    private static final List<FeatureModule> FEATURES = List.of(
            new CoreFeature(),
            new PrimitiveMaterialsFeature(),
            new CampfireFeature(),
            new ChoppingBlockFeature(),
            new PitKilnFeature(),
            new BarrelFeature(),
            new SoakingPotFeature(),
            new TanningRackFeature(),
            new DryingRackFeature()
    );

    private ModFeatures() {
    }

    public static void register(IEventBus modBus, ModContainer modContainer) {
        FEATURES.forEach(feature -> feature.register(modBus, modContainer));
    }
}
