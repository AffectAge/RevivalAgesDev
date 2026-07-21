package com.protyvkultury.revivalages.feature.technology.dryingrack.environment;

import com.protyvkultury.revivalages.feature.technology.dryingrack.config.DryingRackConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

public final class DryingEnvironmentCalculator {

    private DryingEnvironmentCalculator() {
    }

    public static double calculate(Level level, BlockPos pos, boolean normalRack, SeasonProvider seasonProvider) {
        return snapshot(level, pos, normalRack, seasonProvider).speed();
    }

    public static DryingEnvironmentSnapshot snapshot(
            Level level,
            BlockPos pos,
            boolean normalRack,
            SeasonProvider seasonProvider
    ) {
        Holder<Biome> biome = level.getBiome(pos);
        boolean canRain = biome.value().hasPrecipitation();
        DryingEnvironmentBase base;
        double baseSpeed;
        List<DryingEnvironmentModifier> modifiers = new ArrayList<>();

        if (canRain && level.isRainingAt(pos.above())) {
            base = DryingEnvironmentBase.DIRECT_RAIN;
            baseSpeed = DryingRackConfig.DIRECT_RAIN_SPEED.get();
        } else if ((canRain && level.isRaining()) || biome.is(Tags.Biomes.IS_WET)) {
            base = DryingEnvironmentBase.INDIRECT_RAIN;
            baseSpeed = DryingRackConfig.INDIRECT_RAIN_SPEED.get();
        } else if (level.dimension() == Level.NETHER) {
            base = DryingEnvironmentBase.NETHER;
            baseSpeed = DryingRackConfig.NETHER_SPEED.get();
        } else {
            base = DryingEnvironmentBase.DEFAULT;
            baseSpeed = DryingRackConfig.DEFAULT_SPEED.get();
            addIfActive(modifiers, DryingEnvironmentModifierType.HOT,
                    biome.is(Tags.Biomes.IS_HOT) ? DryingRackConfig.HOT_BONUS.get() : 0.0D);
            addIfActive(modifiers, DryingEnvironmentModifierType.DRY,
                    biome.is(Tags.Biomes.IS_DRY) ? DryingRackConfig.DRY_BONUS.get() : 0.0D);
            addIfActive(modifiers, DryingEnvironmentModifierType.COLD,
                    biome.is(Tags.Biomes.IS_COLD) ? DryingRackConfig.COLD_PENALTY.get() : 0.0D);
            addIfActive(modifiers, DryingEnvironmentModifierType.WET,
                    biome.is(Tags.Biomes.IS_WET) ? DryingRackConfig.WET_PENALTY.get() : 0.0D);
        }

        addIfActive(
                modifiers,
                DryingEnvironmentModifierType.NEARBY_HEAT,
                countHeatSources(level, pos) * DryingRackConfig.FIRE_BONUS.get()
        );
        long dayTime = level.getDayTime() % 24_000L;
        if (!level.isRaining() && level.canSeeSky(pos.above()) && dayTime > 3_000L && dayTime < 9_000L) {
            addIfActive(modifiers, DryingEnvironmentModifierType.DAYLIGHT, DryingRackConfig.DAYLIGHT_BONUS.get());
        }
        SeasonType season = seasonProvider.seasonAt(level, pos);
        DryingEnvironmentModifierType seasonModifier = modifierForSeason(season);
        if (seasonModifier != null) {
            addIfActive(modifiers, seasonModifier, DryingRackConfig.seasonBonus(season));
        }
        double rackMultiplier = normalRack
                ? DryingRackConfig.NORMAL_MULTIPLIER.get()
                : DryingRackConfig.CRUDE_MULTIPLIER.get();
        return new DryingEnvironmentSnapshot(base, baseSpeed, modifiers, rackMultiplier);
    }

    private static void addIfActive(
            List<DryingEnvironmentModifier> modifiers,
            DryingEnvironmentModifierType type,
            double amount
    ) {
        if (amount != 0.0D) {
            modifiers.add(new DryingEnvironmentModifier(type, amount));
        }
    }

    private static DryingEnvironmentModifierType modifierForSeason(SeasonType season) {
        return switch (season) {
            case SPRING -> DryingEnvironmentModifierType.SPRING;
            case SUMMER -> DryingEnvironmentModifierType.SUMMER;
            case AUTUMN -> DryingEnvironmentModifierType.AUTUMN;
            case WINTER -> DryingEnvironmentModifierType.WINTER;
            case NONE -> null;
        };
    }

    private static int countHeatSources(Level level, BlockPos origin) {
        int radius = DryingRackConfig.FIRE_RADIUS.get();
        int count = 0;
        for (BlockPos cursor : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius),
                origin.offset(radius, radius, radius))) {
            BlockState state = level.getBlockState(cursor);
            if (state.is(BlockTags.FIRE)
                    || state.is(Blocks.LAVA)
                    || state.is(Blocks.LAVA_CAULDRON)
                    || CampfireBlock.isLitCampfire(state)) {
                count++;
            }
        }
        return count;
    }
}
