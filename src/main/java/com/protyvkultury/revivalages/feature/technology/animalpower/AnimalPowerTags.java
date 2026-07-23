package com.protyvkultury.revivalages.feature.technology.animalpower;

import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class AnimalPowerTags {

    public static final TagKey<EntityType<?>> WORKERS =
            TagKey.create(Registries.ENTITY_TYPE, RevivalAges.id("animal_power_workers"));

    private AnimalPowerTags() {
    }
}
