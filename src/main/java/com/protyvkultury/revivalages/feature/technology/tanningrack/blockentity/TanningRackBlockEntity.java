package com.protyvkultury.revivalages.feature.technology.tanningrack.blockentity;

import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
import com.protyvkultury.revivalages.core.particle.ProgressParticleHelper;
import com.protyvkultury.revivalages.core.process.ProcessRule;
import com.protyvkultury.revivalages.core.process.ProcessRuleEngine;
import com.protyvkultury.revivalages.core.process.ProcessRuleEvaluation;
import com.protyvkultury.revivalages.core.process.ProcessRuleState;
import com.protyvkultury.revivalages.core.process.ProcessRuleType;
import com.protyvkultury.revivalages.feature.content.ContentAvailability;
import com.protyvkultury.revivalages.feature.content.ContentKey;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.tanningrack.TanningRackFeature;
import com.protyvkultury.revivalages.feature.technology.tanningrack.recipe.TanningRackRecipe;
import java.util.Optional;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class TanningRackBlockEntity extends BlockEntity {

    private static final List<ProcessRule> PROCESS_RULES = List.of(
            ProcessRule.of(ProcessRuleType.OPEN_SKY),
            ProcessRule.of(ProcessRuleType.WEATHER_EXPOSURE)
    );

    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private int elapsedTicks;
    private int totalTicks;
    private final ProcessRuleState ruleState = new ProcessRuleState();
    /** Server-evaluated environmental snapshot used by client presentation only. */
    private boolean openSky = true;
    private boolean raining;
    private boolean daytime = true;
    private TanningRackRecipe activeRecipe;

    public TanningRackBlockEntity(BlockPos pos, BlockState state) {
        super(TanningRackFeature.BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TanningRackBlockEntity rack) {
        if (!ContentAvailability.isEnabled(ContentKey.TANNING_RACK)) {
            return;
        }
        ItemStack materializedInput = FoodFreshnessApi.materialize(rack.input);
        ItemStack materializedOutput = FoodFreshnessApi.materialize(rack.output);
        if (materializedInput != rack.input || materializedOutput != rack.output) {
            boolean inputChanged = materializedInput != rack.input;
            rack.input = materializedInput;
            rack.output = materializedOutput;
            if (inputChanged) {
                rack.elapsedTicks = 0;
                rack.totalTicks = 0;
                rack.ruleState.clear();
            }
            rack.resolveRecipe();
            rack.sync();
        }
        if (rack.resolveRecipe()) {
            rack.sync();
        }
        if (rack.activeRecipe == null || rack.input.isEmpty() || !rack.output.isEmpty()) {
            return;
        }
        boolean currentOpenSky = level.canSeeSky(pos);
        boolean currentRaining = level.isRainingAt(pos.above());
        boolean currentDaytime = level.isDay();
        if (rack.openSky != currentOpenSky || rack.raining != currentRaining || rack.daytime != currentDaytime) {
            rack.openSky = currentOpenSky;
            rack.raining = currentRaining;
            rack.daytime = currentDaytime;
            rack.sync();
        }
        ProcessRuleEvaluation rules = ProcessRuleEngine.evaluate(
                PROCESS_RULES,
                type -> type != ProcessRuleType.OPEN_SKY || rack.openSky,
                ignored -> 1.0D
        );
        if (!rules.canAdvance()) {
            if (rules.resetProgress() && (rack.elapsedTicks != 0 || rack.totalTicks != 0)) {
                rack.elapsedTicks = 0;
                rack.totalTicks = 0;
                rack.sync();
            }
            return;
        }
        int rainLimit = PrimitiveTechnologyConfig.TANNING_RACK_RAIN_RUIN_TICKS.get();
        if (rainLimit >= 0 && rack.raining) {
            boolean hasRainFailure = !rack.activeRecipe.rainFailure().isEmpty();
            if (hasRainFailure) {
                int rainTicks = rack.ruleState.incrementUntil(ProcessRuleType.WEATHER_EXPOSURE, rainLimit);
                if (rainTicks >= rainLimit) {
                    rack.output = rack.activeRecipe.rainFailure();
                    rack.input = ItemStack.EMPTY;
                    rack.elapsedTicks = 0;
                    rack.totalTicks = 0;
                    rack.ruleState.clear();
                    rack.activeRecipe = null;
                    rack.sync();
                } else {
                    rack.setChanged();
                }
            }
            return;
        }
        long time = level.getDayTime() % 24000L;
        if (time > 12000L) {
            return;
        }
        rack.totalTicks = Math.max(1, (int) Math.round(rack.activeRecipe.processingTime()
                * PrimitiveTechnologyConfig.TANNING_RACK_DURATION_MULTIPLIER.get()));
        rack.elapsedTicks++;
        if (rack.elapsedTicks >= rack.totalTicks) {
            ItemStack result = rack.activeRecipe.result();
            FoodFreshnessApi.copyOldest(result, java.util.List.of(rack.input.copy()));
            rack.output = result;
            rack.input = ItemStack.EMPTY;
            rack.elapsedTicks = 0;
            rack.totalTicks = 0;
            rack.activeRecipe = null;
            rack.sync();
        } else {
            rack.setChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TanningRackBlockEntity rack) {
        long time = level.getDayTime() % 24000L;
        if (ContentAvailability.isEnabled(ContentKey.TANNING_RACK)
                && rack.activeRecipe != null
                && !rack.input.isEmpty()
                && rack.openSky
                && (PrimitiveTechnologyConfig.TANNING_RACK_RAIN_RUIN_TICKS.get() < 0
                || !rack.raining)
                && time <= 12000L
                && PrimitiveTechnologyConfig.PROGRESS_PARTICLES.get()
                && level.getGameTime() % 40L == 0L) {
            ProgressParticleHelper.spawn(
                    level,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.85D,
                    pos.getZ() + 0.5D,
                    0.5D,
                    0.25D,
                    0.5D
            );
        }
    }

    public ItemStack input() {
        return input;
    }

    public ItemStack output() {
        return output;
    }

    public ItemStack recipeOutput() {
        return activeRecipe == null ? ItemStack.EMPTY : activeRecipe.result();
    }

    public ItemStack rainFailureOutput() {
        return activeRecipe == null ? ItemStack.EMPTY : activeRecipe.rainFailure();
    }

    public double progress() {
        return totalTicks <= 0 ? 0.0D : Math.min(1.0D, elapsedTicks / (double) totalTicks);
    }

    public int rainTicks() {
        return ruleState.counter(ProcessRuleType.WEATHER_EXPOSURE);
    }

    public boolean openSky() {
        return openSky;
    }

    public boolean raining() {
        return raining;
    }

    public boolean daytime() {
        return daytime;
    }

    public boolean canInsert(ItemStack stack) {
        return input.isEmpty() && output.isEmpty() && findRecipe(stack).isPresent();
    }

    public void insert(ItemStack source, boolean infinite) {
        if (!canInsert(source)) {
            return;
        }
        input = source.copyWithCount(1);
        if (!infinite) {
            source.shrink(1);
        }
        elapsedTicks = 0;
        totalTicks = 0;
        ruleState.clear();
        resolveRecipe();
        sync();
    }

    public ItemStack extractInput() {
        ItemStack result = input;
        input = ItemStack.EMPTY;
        elapsedTicks = 0;
        totalTicks = 0;
        ruleState.clear();
        activeRecipe = null;
        sync();
        return result;
    }

    public ItemStack extractOutput() {
        ItemStack result = output;
        output = ItemStack.EMPTY;
        resolveRecipe();
        sync();
        return result;
    }

    public void dropContents() {
        if (level != null && !level.isClientSide) {
            Block.popResource(level, worldPosition, input);
            Block.popResource(level, worldPosition, output);
        }
    }

    private Optional<RecipeHolder<TanningRackRecipe>> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(TanningRackFeature.RECIPE_TYPE.get(), new SingleRecipeInput(stack), level);
    }

    private boolean resolveRecipe() {
        activeRecipe = findRecipe(input).map(RecipeHolder::value).orElse(null);
        if (level != null
                && !input.isEmpty()
                && activeRecipe == null
                && (elapsedTicks != 0 || totalTicks != 0 || rainTicks() != 0)) {
            elapsedTicks = 0;
            totalTicks = 0;
            ruleState.clear();
            return true;
        }
        return false;
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        input = ItemStack.parseOptional(registries, tag.getCompound("Input"));
        output = ItemStack.parseOptional(registries, tag.getCompound("Output"));
        elapsedTicks = tag.getInt("ElapsedTicks");
        totalTicks = tag.getInt("TotalTicks");
        if (tag.contains("ProcessRules")) {
            ruleState.load(tag.getCompound("ProcessRules"));
        } else {
            ruleState.setCounter(ProcessRuleType.WEATHER_EXPOSURE, tag.getInt("RainTicks"));
        }
        openSky = tag.getBoolean("OpenSky");
        raining = tag.getBoolean("Raining");
        daytime = tag.getBoolean("Daytime");
        resolveRecipe();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!input.isEmpty()) {
            tag.put("Input", input.save(registries));
        }
        if (!output.isEmpty()) {
            tag.put("Output", output.save(registries));
        }
        tag.putInt("ElapsedTicks", elapsedTicks);
        tag.putInt("TotalTicks", totalTicks);
        tag.put("ProcessRules", ruleState.save());
        tag.putBoolean("OpenSky", openSky);
        tag.putBoolean("Raining", raining);
        tag.putBoolean("Daytime", daytime);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

}
