package com.protyvkultury.revivalages.feature.technology.campfire.effect;

import com.protyvkultury.revivalages.feature.technology.campfire.CampfireFeature;
import com.protyvkultury.revivalages.feature.technology.campfire.block.CampfireBlock;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class CampfireEffectEvents {

    private static final Map<UUID, RestState> STATES = new HashMap<>();

    private CampfireEffectEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !PrimitiveTechnologyConfig.CAMPFIRE_EFFECTS_ENABLED.get()) {
            return;
        }
        RestState tracked = STATES.computeIfAbsent(player.getUUID(), ignored -> new RestState(player.position()));
        reduceWellFedExhaustion(player, tracked);
        if (player.tickCount % 20 != 0) {
            tracked.lastExhaustion = player.getFoodData().getExhaustionLevel();
            return;
        }

        ServerLevel level = player.serverLevel();
        boolean activeTime = isActiveTime(level.getDayTime() % 24000L);
        boolean nearCampfire = activeTime && hasLitCampfire(level, player.blockPosition());
        boolean threatened = !level.getEntitiesOfClass(
                Monster.class,
                player.getBoundingBox().inflate(PrimitiveTechnologyConfig.CAMPFIRE_EFFECT_RANGE.get())
        ).isEmpty();

        if (!nearCampfire || threatened) {
            tracked.stationaryTicks = 0;
            tracked.lastPosition = player.position();
            return;
        }

        player.addEffect(new MobEffectInstance(CampfireFeature.COMFORT, 60, 0, true, true));
        double moved = player.position().distanceToSqr(tracked.lastPosition);
        if (moved < 0.0025D && player.onGround() && !player.isHurt()) {
            tracked.stationaryTicks += 20;
        } else {
            tracked.stationaryTicks = 0;
        }
        tracked.lastPosition = player.position();

        int interval = PrimitiveTechnologyConfig.RESTING_LEVEL_INTERVAL.get();
        int amplifier = Math.min(2, tracked.stationaryTicks / interval);
        player.addEffect(new MobEffectInstance(CampfireFeature.RESTING, 60, amplifier, true, true));
        if (tracked.stationaryTicks > 0
                && tracked.stationaryTicks % PrimitiveTechnologyConfig.RESTING_REGEN_INTERVAL.get() == 0) {
            player.heal(1.0F);
        }
        if (amplifier >= 2) {
            int duration = PrimitiveTechnologyConfig.WELL_RESTED_DURATION.get();
            player.addEffect(new MobEffectInstance(CampfireFeature.WELL_RESTED, duration, 0, false, true));
            player.setAbsorptionAmount(Math.max(
                    player.getAbsorptionAmount(),
                    PrimitiveTechnologyConfig.WELL_RESTED_ABSORPTION_HALF_HEARTS.get()
            ));
            if (player.hasEffect(CampfireFeature.WELL_FED)) {
                player.addEffect(new MobEffectInstance(
                        CampfireFeature.FOCUSED,
                        PrimitiveTechnologyConfig.FOCUSED_DURATION.get(),
                        0,
                        false,
                        true
                ));
            }
        }
    }

    @SubscribeEvent
    public static void onFoodFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.hasEffect(CampfireFeature.COMFORT)) {
            return;
        }
        FoodProperties food = event.getItem().get(DataComponents.FOOD);
        if (food == null) {
            return;
        }
        double bonus = PrimitiveTechnologyConfig.COMFORT_FOOD_BONUS.get();
        int nutrition = Math.max(0, (int) Math.round(food.nutrition() * bonus));
        if (nutrition > 0) {
            player.getFoodData().eat(nutrition, food.saturation());
        }
        if (player.getFoodData().getFoodLevel() >= 20 && player.getFoodData().getSaturationLevel() >= 19.0F) {
            player.addEffect(new MobEffectInstance(
                    CampfireFeature.WELL_FED,
                    PrimitiveTechnologyConfig.WELL_FED_DURATION.get(),
                    0,
                    false,
                    true
            ));
        }
    }

    @SubscribeEvent
    public static void onXpChange(PlayerXpEvent.XpChange event) {
        if (event.getAmount() > 0 && event.getEntity().hasEffect(CampfireFeature.FOCUSED)) {
            event.setAmount((int) Math.ceil(event.getAmount()
                    * (1.0D + PrimitiveTechnologyConfig.FOCUSED_XP_BONUS.get())));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        STATES.remove(event.getEntity().getUUID());
    }

    private static void reduceWellFedExhaustion(ServerPlayer player, RestState tracked) {
        float current = player.getFoodData().getExhaustionLevel();
        if (player.hasEffect(CampfireFeature.WELL_FED) && current > tracked.lastExhaustion) {
            float delta = current - tracked.lastExhaustion;
            float retained = (float) (delta
                    * (1.0D - PrimitiveTechnologyConfig.WELL_FED_EXHAUSTION_MODIFIER.get()));
            player.getFoodData().setExhaustion(tracked.lastExhaustion + retained);
        }
        tracked.lastExhaustion = player.getFoodData().getExhaustionLevel();
    }

    private static boolean hasLitCampfire(ServerLevel level, BlockPos center) {
        int range = PrimitiveTechnologyConfig.CAMPFIRE_EFFECT_RANGE.get();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-range, -2, -range), center.offset(range, 2, range))) {
            var state = level.getBlockState(pos);
            if (state.is(CampfireFeature.CAMPFIRE.get()) && state.getValue(CampfireBlock.LIT)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isActiveTime(long time) {
        int start = PrimitiveTechnologyConfig.CAMPFIRE_EFFECT_START_TIME.get();
        int stop = PrimitiveTechnologyConfig.CAMPFIRE_EFFECT_STOP_TIME.get();
        return start <= stop ? time >= start && time <= stop : time >= start || time <= stop;
    }

    private static final class RestState {

        private Vec3 lastPosition;
        private int stationaryTicks;
        private float lastExhaustion;

        private RestState(Vec3 lastPosition) {
            this.lastPosition = lastPosition;
        }
    }
}
