package com.protyvkultury.revivalages.feature.inventory.carriedweight;

import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

final class CarriedWeightPenaltyService {

    private static final ResourceLocation MOVEMENT_SPEED_MODIFIER =
            RevivalAges.id("carried_weight_movement_speed");
    private static final ResourceLocation ATTACK_SPEED_MODIFIER =
            RevivalAges.id("carried_weight_attack_speed");
    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER =
            RevivalAges.id("carried_weight_attack_damage");

    private CarriedWeightPenaltyService() {
    }

    static void apply(ServerPlayer player, CarriedWeightState state) {
        WeightPenaltySettings settings = CarriedWeightSettings.snapshot().penalties();
        if (!CarriedWeightSettings.enabled()
                || settings.strength() <= 0.0D
                || player.isCreative()
                || player.isSpectator()) {
            clear(player);
            return;
        }
        if (state.overloaded()) {
            int level = overloadLevel(player, state, settings);
            player.addEffect(new MobEffectInstance(
                    CarriedWeightFeature.OVERLOADED,
                    settings.effectDurationTicks(),
                    level - 1,
                    true,
                    false,
                    false
            ));
            applyModifiers(player, WeightPenaltyMath.overloadPenalties(level, settings));
            return;
        }

        player.removeEffect(CarriedWeightFeature.OVERLOADED);
        if (settings.realisticMode()
                && state.currentWeight() > state.capacity() * settings.realisticStartFraction()) {
            applyModifiers(
                    player,
                    WeightPenaltyMath.realisticPenalties(
                            state.currentWeight(),
                            state.capacity(),
                            settings
                    )
            );
        } else {
            removeModifiers(player);
        }
    }

    static void clear(ServerPlayer player) {
        player.removeEffect(CarriedWeightFeature.OVERLOADED);
        removeModifiers(player);
    }

    static void onJump(Player player) {
        if (!CarriedWeightSettings.enabled()
                || player.isCreative()
                || player.isSpectator()) {
            return;
        }
        CarriedWeightState state = CarriedWeightFeature.state(player);
        WeightPenaltySettings settings = CarriedWeightSettings.snapshot().penalties();
        int level = overloadLevel(player, state, settings);
        Vec3 movement = player.getDeltaMovement();
        double adjusted = WeightPenaltyMath.jumpVelocity(
                movement.y,
                state.currentWeight(),
                state.capacity(),
                level,
                settings
        );
        if (Double.compare(movement.y, adjusted) != 0) {
            player.setDeltaMovement(movement.x, adjusted, movement.z);
            player.hasImpulse = true;
        }
    }

    private static int overloadLevel(
            Player player,
            CarriedWeightState state,
            WeightPenaltySettings settings
    ) {
        int strength = player.hasEffect(MobEffects.DAMAGE_BOOST)
                ? player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1
                : 0;
        int haste = player.hasEffect(MobEffects.DIG_SPEED)
                ? player.getEffect(MobEffects.DIG_SPEED).getAmplifier() + 1
                : 0;
        return WeightPenaltyMath.overloadLevel(
                state.currentWeight(),
                state.capacity(),
                strength,
                haste,
                settings
        );
    }

    private static void applyModifiers(
            ServerPlayer player,
            WeightPenaltyMath.Penalties penalties
    ) {
        replace(
                player,
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_SPEED_MODIFIER,
                penalties.movementSpeed()
        );
        replace(
                player,
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_MODIFIER,
                penalties.attackSpeed()
        );
        replace(
                player,
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_MODIFIER,
                penalties.attackDamage()
        );
    }

    private static void replace(
            ServerPlayer player,
            net.minecraft.core.Holder<Attribute> attribute,
            ResourceLocation id,
            double penalty
    ) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.addOrUpdateTransientModifier(new AttributeModifier(
                id,
                -Math.max(0.0D, penalty),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        ));
    }

    private static void removeModifiers(ServerPlayer player) {
        remove(player, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_MODIFIER);
        remove(player, Attributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER);
        remove(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER);
    }

    private static void remove(
            ServerPlayer player,
            net.minecraft.core.Holder<Attribute> attribute,
            ResourceLocation id
    ) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }
}
