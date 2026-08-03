package com.protyvkultury.revivalages.core.process;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A codec-backed declaration of a reusable process constraint, modifier, or
 * environmental hazard. Rule-specific values remain in their canonical recipe
 * fields or server configuration.
 */
public record ProcessRule(ProcessRuleType type, ProcessRulePolicy policy) {

    private static final Codec<ProcessRuleType> TYPE_CODEC = Codec.STRING.xmap(
            ProcessRuleType::bySerializedName,
            ProcessRuleType::getSerializedName);
    private static final Codec<ProcessRulePolicy> POLICY_CODEC = Codec.STRING.xmap(
            ProcessRulePolicy::bySerializedName,
            ProcessRulePolicy::getSerializedName);

    public static final Codec<ProcessRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TYPE_CODEC.fieldOf("type").forGetter(ProcessRule::type),
            POLICY_CODEC.optionalFieldOf("policy").forGetter(rule ->
                    rule.policy() == rule.type().defaultPolicy()
                            ? Optional.empty()
                            : Optional.of(rule.policy()))
    ).apply(instance, (type, policy) -> new ProcessRule(type, policy.orElse(type.defaultPolicy()))));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessRule> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(index -> ProcessRuleType.values()[index], ProcessRuleType::ordinal),
            ProcessRule::type,
            ByteBufCodecs.idMapper(index -> ProcessRulePolicy.values()[index], ProcessRulePolicy::ordinal),
            ProcessRule::policy,
            ProcessRule::new
    );

    public ProcessRule {
        if (type.kind() == ProcessRuleKind.GATE && policy == null) {
            policy = type.defaultPolicy();
        }
        if (type.kind() != ProcessRuleKind.GATE) {
            policy = ProcessRulePolicy.PAUSE;
        }
    }

    public static ProcessRule of(ProcessRuleType type) {
        return new ProcessRule(type, type.defaultPolicy());
    }
}
