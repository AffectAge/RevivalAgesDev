package com.protyvkultury.revivalages.feature.inventory.carriedweight;

import com.protyvkultury.revivalages.RevivalAges;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CarriedWeightSettingsPayload(
        CarriedWeightSettings.Snapshot snapshot
) implements CustomPacketPayload {

    public static final Type<CarriedWeightSettingsPayload> TYPE =
            new Type<>(RevivalAges.id("carried_weight_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CarriedWeightSettingsPayload> STREAM_CODEC =
            StreamCodec.of(CarriedWeightSettingsPayload::encode, CarriedWeightSettingsPayload::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CarriedWeightSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> CarriedWeightSettings.acceptRemote(payload.snapshot()));
    }

    private static void encode(RegistryFriendlyByteBuf buffer, CarriedWeightSettingsPayload payload) {
        CarriedWeightSettings.Snapshot snapshot = payload.snapshot;
        buffer.writeBoolean(snapshot.enabled());
        buffer.writeVarInt(snapshot.updateIntervalTicks());
        buffer.writeVarInt(snapshot.maximumRecursionDepth());
        buffer.writeDouble(snapshot.baseCapacity());
        buffer.writeDouble(snapshot.pocketCapacity());
        writeFormula(buffer, snapshot.formula());
        writePenalties(buffer, snapshot.penalties());
    }

    private static CarriedWeightSettingsPayload decode(RegistryFriendlyByteBuf buffer) {
        boolean enabled = buffer.readBoolean();
        int interval = readInt(buffer, 1, 200, "update interval");
        int depth = readInt(buffer, 0, 32, "recursion depth");
        double baseCapacity = readPositive(buffer, 1_000_000_000.0D, "base capacity");
        double pocketCapacity = readPositive(buffer, 1_000_000_000.0D, "pocket capacity");
        return new CarriedWeightSettingsPayload(new CarriedWeightSettings.Snapshot(
                enabled,
                interval,
                depth,
                baseCapacity,
                pocketCapacity,
                readFormula(buffer),
                readPenalties(buffer)
        ));
    }

    private static void writeFormula(RegistryFriendlyByteBuf buffer, WeightFormulaSettings settings) {
        buffer.writeDouble(settings.bucketWeight());
        buffer.writeDouble(settings.bottleWeight());
        buffer.writeDouble(settings.blockWeight());
        buffer.writeDouble(settings.ingotWeight());
        buffer.writeDouble(settings.nuggetWeight());
        buffer.writeDouble(settings.itemWeight());
        buffer.writeDouble(settings.technicalWeight());
        buffer.writeDouble(settings.containerContentsMultiplier());
        buffer.writeDouble(settings.stackMultiplierCoefficient());
        buffer.writeDouble(settings.commonRarityMultiplier());
        buffer.writeDouble(settings.uncommonRarityMultiplier());
        buffer.writeDouble(settings.rareRarityMultiplier());
        buffer.writeDouble(settings.epicRarityMultiplier());
        buffer.writeDouble(settings.fastFoodThresholdSeconds());
        buffer.writeDouble(settings.fireResistantMultiplier());
        buffer.writeDouble(settings.toolDurabilityDivisor());
        buffer.writeDouble(settings.toolDurabilityWeight());
        buffer.writeDouble(settings.armorDurabilityDivisor());
        buffer.writeDouble(settings.armorDurabilityWeight());
        buffer.writeDouble(settings.armorProtectionWeight());
        buffer.writeDouble(settings.blockHardnessWeight());
        buffer.writeDouble(settings.blockResistanceWeight());
        buffer.writeDouble(settings.blockResistanceWeightCap());
        buffer.writeDouble(settings.transparentBlockMultiplier());
        buffer.writeDouble(settings.blockEntityWeight());
        buffer.writeDouble(settings.slabMultiplier());
        buffer.writeDouble(settings.stairsMultiplier());
    }

    private static WeightFormulaSettings readFormula(RegistryFriendlyByteBuf buffer) {
        return new WeightFormulaSettings(
                readNonNegative(buffer, 1_000_000_000.0D, "bucket weight"),
                readNonNegative(buffer, 1_000_000_000.0D, "bottle weight"),
                readNonNegative(buffer, 1_000_000_000.0D, "block weight"),
                readNonNegative(buffer, 1_000_000_000.0D, "ingot weight"),
                readNonNegative(buffer, 1_000_000_000.0D, "nugget weight"),
                readNonNegative(buffer, 1_000_000_000.0D, "item weight"),
                readNonNegative(buffer, 1_000_000_000.0D, "technical weight"),
                readNonNegative(buffer, 100.0D, "container multiplier"),
                readNonNegative(buffer, 10_000.0D, "stack coefficient"),
                readNonNegative(buffer, 100.0D, "common rarity"),
                readNonNegative(buffer, 100.0D, "uncommon rarity"),
                readNonNegative(buffer, 100.0D, "rare rarity"),
                readNonNegative(buffer, 100.0D, "epic rarity"),
                readNonNegative(buffer, 60.0D, "fast food threshold"),
                readNonNegative(buffer, 100.0D, "fire resistant multiplier"),
                readPositive(buffer, 1_000_000_000.0D, "tool durability divisor"),
                readNonNegative(buffer, 1_000_000.0D, "tool durability weight"),
                readPositive(buffer, 1_000_000_000.0D, "armor durability divisor"),
                readNonNegative(buffer, 1_000_000.0D, "armor durability weight"),
                readNonNegative(buffer, 1_000_000.0D, "armor protection weight"),
                readNonNegative(buffer, 1_000_000.0D, "block hardness weight"),
                readNonNegative(buffer, 1_000_000.0D, "block resistance weight"),
                readNonNegative(buffer, 1_000_000_000.0D, "block resistance cap"),
                readNonNegative(buffer, 100.0D, "transparent multiplier"),
                readNonNegative(buffer, 1_000_000.0D, "block entity weight"),
                readNonNegative(buffer, 100.0D, "slab multiplier"),
                readNonNegative(buffer, 100.0D, "stairs multiplier")
        );
    }

    private static void writePenalties(RegistryFriendlyByteBuf buffer, WeightPenaltySettings settings) {
        buffer.writeBoolean(settings.realisticMode());
        buffer.writeDouble(settings.strength());
        buffer.writeVarInt(settings.overloadStepPercent());
        buffer.writeVarInt(settings.maximumOverloadLevel());
        buffer.writeVarInt(settings.effectDurationTicks());
        buffer.writeDouble(settings.overloadBasePenalty());
        buffer.writeDouble(settings.overloadDamageBasePenalty());
        buffer.writeDouble(settings.overloadLevelPenalty());
        buffer.writeDouble(settings.maximumAttributePenalty());
        buffer.writeDouble(settings.realisticStartFraction());
        buffer.writeDouble(settings.realisticSpeedRelief());
        buffer.writeDouble(settings.realisticAttackSpeedRelief());
        buffer.writeDouble(settings.realisticDamageRelief());
        buffer.writeDouble(settings.overloadJumpMultiplier());
        buffer.writeDouble(settings.overloadMinimumJumpMultiplier());
        buffer.writeDouble(settings.realisticMinimumJumpMultiplier());
        buffer.writeDouble(settings.realisticJumpBonus());
    }

    private static WeightPenaltySettings readPenalties(RegistryFriendlyByteBuf buffer) {
        return new WeightPenaltySettings(
                buffer.readBoolean(),
                readNonNegative(buffer, 100.0D, "penalty strength"),
                readInt(buffer, 1, 100, "overload step"),
                readInt(buffer, 1, 255, "maximum overload level"),
                readInt(buffer, 1, 20_000, "effect duration"),
                readFraction(buffer, "overload base penalty"),
                readFraction(buffer, "overload damage penalty"),
                readFraction(buffer, "overload level penalty"),
                readFraction(buffer, "maximum attribute penalty"),
                readFraction(buffer, "realistic start"),
                readFraction(buffer, "realistic speed relief"),
                readFraction(buffer, "realistic attack speed relief"),
                readFraction(buffer, "realistic damage relief"),
                readNonNegative(buffer, 10.0D, "overload jump multiplier"),
                readFraction(buffer, "overload minimum jump"),
                readFraction(buffer, "realistic minimum jump"),
                readNonNegative(buffer, 10.0D, "realistic jump bonus")
        );
    }

    private static int readInt(RegistryFriendlyByteBuf buffer, int minimum, int maximum, String field) {
        int value = buffer.readVarInt();
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException("Invalid Carried Weight " + field);
        }
        return value;
    }

    private static double readFraction(RegistryFriendlyByteBuf buffer, String field) {
        return readNonNegative(buffer, 1.0D, field);
    }

    private static double readPositive(RegistryFriendlyByteBuf buffer, double maximum, String field) {
        double value = readNonNegative(buffer, maximum, field);
        if (value <= 0.0D) {
            throw new IllegalArgumentException("Invalid Carried Weight " + field);
        }
        return value;
    }

    private static double readNonNegative(RegistryFriendlyByteBuf buffer, double maximum, String field) {
        double value = buffer.readDouble();
        if (!Double.isFinite(value) || value < 0.0D || value > maximum) {
            throw new IllegalArgumentException("Invalid Carried Weight " + field);
        }
        return value;
    }
}
