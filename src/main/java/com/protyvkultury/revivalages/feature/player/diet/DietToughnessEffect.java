package com.protyvkultury.revivalages.feature.player.diet;

import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

final class DietToughnessEffect extends MobEffect {

    DietToughnessEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xD8B66A);
        addAttributeModifier(
                Attributes.MAX_HEALTH,
                RevivalAges.id("effect.diet_toughness_health"),
                4.0D,
                AttributeModifier.Operation.ADD_VALUE
        );
        addAttributeModifier(
                Attributes.ARMOR_TOUGHNESS,
                RevivalAges.id("effect.diet_toughness_armor"),
                2.0D,
                AttributeModifier.Operation.ADD_VALUE
        );
        addAttributeModifier(
                Attributes.ATTACK_SPEED,
                RevivalAges.id("effect.diet_toughness_attack_speed"),
                0.1D,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}
