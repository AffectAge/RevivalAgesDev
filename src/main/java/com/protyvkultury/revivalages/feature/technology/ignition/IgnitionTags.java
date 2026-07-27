package com.protyvkultury.revivalages.feature.technology.ignition;

import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class IgnitionTags {

    public static final TagKey<Fluid> WOOD_TORCH_EXTINGUISHING_FLUIDS = TagKey.create(
            Registries.FLUID,
            ResourceLocation.fromNamespaceAndPath(RevivalAges.MOD_ID, "wood_torch_extinguishing_fluids")
    );

    private IgnitionTags() {
    }
}
