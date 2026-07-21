package com.protyvkultury.revivalages.feature.technology.primitive;

import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public final class PrimitiveMaterialEvents {

    private PrimitiveMaterialEvents() {
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player)
                || !event.getEntity().getType().is(PrimitiveTags.DROPS_RAW_HIDE)
                || event.getEntity().getRandom().nextDouble() >= PrimitiveTechnologyConfig.RAW_HIDE_DROP_CHANCE.get()) {
            return;
        }
        int count = 1 + event.getEntity().getRandom().nextInt(PrimitiveTechnologyConfig.RAW_HIDE_MAX_DROPS.get());
        event.getDrops().add(new ItemEntity(
                event.getEntity().level(),
                event.getEntity().getX(),
                event.getEntity().getY(),
                event.getEntity().getZ(),
                new ItemStack(PrimitiveMaterialsFeature.RAW_HIDE.get(), count)
        ));
    }
}
