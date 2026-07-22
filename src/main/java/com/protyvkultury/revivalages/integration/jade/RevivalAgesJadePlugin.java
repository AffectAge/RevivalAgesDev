package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.feature.technology.dryingrack.block.AbstractDryingRackBlock;
import com.protyvkultury.revivalages.feature.technology.barrel.block.BarrelBlock;
import com.protyvkultury.revivalages.feature.technology.campfire.block.CampfireBlock;
import com.protyvkultury.revivalages.feature.technology.choppingblock.block.ChoppingBlock;
import com.protyvkultury.revivalages.feature.technology.pitkiln.block.PitKilnBlock;
import com.protyvkultury.revivalages.feature.technology.soakingpot.block.SoakingPotBlock;
import com.protyvkultury.revivalages.feature.technology.tanningrack.block.TanningRackBlock;
import com.protyvkultury.revivalages.feature.technology.stonemachine.block.StoneMachineBlock;
import com.protyvkultury.revivalages.feature.technology.anvil.block.AnvilBlock;
import com.protyvkultury.revivalages.feature.technology.pitburn.block.ActivePileBlock;
import com.protyvkultury.revivalages.feature.technology.pitburn.block.AshPileBlock;
import com.protyvkultury.revivalages.feature.technology.ignition.block.WoodTorchBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class RevivalAgesJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(DryingRackComponentProvider.INSTANCE, AbstractDryingRackBlock.class);
        registration.registerBlockComponent(PrimitiveDeviceComponentProvider.INSTANCE, CampfireBlock.class);
        registration.registerBlockComponent(PrimitiveDeviceComponentProvider.INSTANCE, ChoppingBlock.class);
        registration.registerBlockComponent(PrimitiveDeviceComponentProvider.INSTANCE, PitKilnBlock.class);
        registration.registerBlockComponent(PrimitiveDeviceComponentProvider.INSTANCE, BarrelBlock.class);
        registration.registerBlockComponent(PrimitiveDeviceComponentProvider.INSTANCE, SoakingPotBlock.class);
        registration.registerBlockComponent(PrimitiveDeviceComponentProvider.INSTANCE, TanningRackBlock.class);
        registration.registerBlockComponent(StoneMachineComponentProvider.INSTANCE, StoneMachineBlock.class);
        registration.registerBlockComponent(AnvilComponentProvider.INSTANCE, AnvilBlock.class);
        registration.registerBlockComponent(PrimitiveDeviceComponentProvider.INSTANCE, ActivePileBlock.class);
        registration.registerBlockComponent(PrimitiveDeviceComponentProvider.INSTANCE, AshPileBlock.class);
        registration.registerBlockComponent(PrimitiveDeviceComponentProvider.INSTANCE, WoodTorchBlock.class);
    }
}
