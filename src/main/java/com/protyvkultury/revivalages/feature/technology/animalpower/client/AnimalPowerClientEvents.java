package com.protyvkultury.revivalages.feature.technology.animalpower.client;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.animalpower.AnimalPowerFeature;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

public final class AnimalPowerClientEvents {

    private AnimalPowerClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(AnimalPowerClientEvents::registerRenderers);
        modBus.addListener(AnimalPowerClientEvents::registerAdditionalModels);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                AnimalPowerFeature.HAND_GRINDSTONE_BLOCK_ENTITY.get(),
                AnimalPowerRenderers.HandGrindstone::new
        );
        event.registerBlockEntityRenderer(
                AnimalPowerFeature.ANIMAL_MACHINE_BLOCK_ENTITY.get(),
                AnimalPowerRenderers.AnimalMachine::new
        );
    }

    private static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(standaloneModel("hand_grindstone_rotor"));
        event.register(standaloneModel("horse_grindstone_rotor"));
        event.register(standaloneModel("horse_chopping_blade"));
        event.register(standaloneModel("horse_press_platen"));
    }

    static ModelResourceLocation standaloneModel(String path) {
        return new ModelResourceLocation(
                RevivalAges.id("block/" + path),
                ModelResourceLocation.STANDALONE_VARIANT
        );
    }
}
