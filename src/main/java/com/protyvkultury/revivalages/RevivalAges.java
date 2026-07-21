package com.protyvkultury.revivalages;

import com.mojang.logging.LogUtils;
import com.protyvkultury.revivalages.feature.ModFeatures;
import com.protyvkultury.revivalages.feature.technology.dryingrack.environment.DryingRackSeasonService;
import com.protyvkultury.revivalages.integration.DryingRackSeasonIntegrations;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(RevivalAges.MOD_ID)
public final class RevivalAges {

    public static final String MOD_ID = "revivalages";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RevivalAges(IEventBus modBus, ModContainer modContainer) {
        ModFeatures.register(modBus, modContainer);
        DryingRackSeasonService.install(DryingRackSeasonIntegrations.createProvider());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
