package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.core.process.ProcessRulePresentation;
import com.protyvkultury.revivalages.core.process.ProcessRuleType;
import com.protyvkultury.revivalages.feature.technology.barrel.block.BarrelBlock;
import com.protyvkultury.revivalages.feature.technology.barrel.blockentity.BarrelBlockEntity;
import com.protyvkultury.revivalages.feature.technology.campfire.blockentity.CampfireBlockEntity;
import com.protyvkultury.revivalages.feature.technology.choppingblock.block.ChoppingBlock;
import com.protyvkultury.revivalages.feature.technology.choppingblock.blockentity.ChoppingBlockEntity;
import com.protyvkultury.revivalages.feature.technology.pitkiln.block.PitKilnBlock;
import com.protyvkultury.revivalages.feature.technology.pitkiln.block.PitKilnStage;
import com.protyvkultury.revivalages.feature.technology.pitkiln.blockentity.PitKilnBlockEntity;
import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.primitive.PrimitiveMaterialsFeature;
import com.protyvkultury.revivalages.feature.technology.soakingpot.blockentity.SoakingPotBlockEntity;
import com.protyvkultury.revivalages.feature.technology.tanningrack.blockentity.TanningRackBlockEntity;
import com.protyvkultury.revivalages.feature.technology.pitburn.PitBurnFeature;
import com.protyvkultury.revivalages.feature.technology.pitburn.blockentity.PitBurnBlockEntity;
import com.protyvkultury.revivalages.feature.technology.ignition.block.WoodTorchBlock;
import com.protyvkultury.revivalages.feature.technology.ignition.blockentity.WoodTorchBlockEntity;
import com.protyvkultury.revivalages.feature.technology.ignition.WoodTorchSettings;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.fluid.JadeFluidObject;

/** Client-side, synced-state view for every primitive processing block. */
public enum PrimitiveDeviceComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = RevivalAges.id("primitive_devices");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockState().is(Blocks.FIRE)) {
            if (accessor.getLevel().getBlockEntity(accessor.getPosition().below())
                    instanceof PitKilnBlockEntity kiln
                    && kiln.getBlockState().getValue(PitKilnBlock.STAGE) == PitKilnStage.ACTIVE) {
                appendPitKiln(tooltip, accessor, kiln, true);
            }
            return;
        }
        switch (accessor.getBlockEntity()) {
            case CampfireBlockEntity campfire -> appendCampfire(tooltip, accessor, campfire);
            case ChoppingBlockEntity chopping -> appendChopping(tooltip, accessor, chopping);
            case PitKilnBlockEntity kiln -> appendPitKiln(tooltip, accessor, kiln, false);
            case BarrelBlockEntity barrel -> appendBarrel(tooltip, accessor, barrel);
            case SoakingPotBlockEntity pot -> appendSoakingPot(tooltip, accessor, pot);
            case TanningRackBlockEntity rack -> appendTanningRack(tooltip, accessor, rack);
            case PitBurnBlockEntity burn -> appendPitBurn(tooltip, accessor, burn);
            case WoodTorchBlockEntity torch -> appendWoodTorch(tooltip, accessor, torch);
            default -> {
            }
        }
    }

    private static void appendCampfire(ITooltip tooltip, BlockAccessor accessor, CampfireBlockEntity campfire) {
        ItemStack input = campfire.cookingInput();
        ItemStack output = campfire.recipeOutput();
        if (campfire.isBurned()) {
            appendItemProgress(tooltip, output.isEmpty() ? input : output, campfire.cookingStack(), 1.0D);
        } else if (campfire.isCompleted()) {
            appendItemProgress(tooltip, campfire.cookingStack(),
                    new ItemStack(PrimitiveMaterialsFeature.BURNED_FOOD.get()),
                    campfire.burnProgressAt(accessor.getLevel().getGameTime()));
        } else {
            appendItemProgress(tooltip, input.isEmpty() ? campfire.cookingStack() : input,
                    output, campfire.progressAt(accessor.getLevel().getGameTime()));
        }
        String state = campfire.isDead() ? "dead" : campfire.isLit() ? "lit" : "unlit";
        tooltip.add(Component.translatable("jade.revivalages.campfire.state." + state)
                .withStyle(campfire.isLit() ? ChatFormatting.GREEN : ChatFormatting.RED));
        tooltip.add(Component.translatable("jade.revivalages.campfire.fuel", campfire.fuelLevel(), 8));
        tooltip.add(Component.translatable("jade.revivalages.campfire.ash", campfire.ashLevel(), 8));
        if (campfire.isLit()) {
            tooltip.add(Component.translatable("jade.revivalages.campfire.burn_time",
                    wholeSeconds(campfire.remainingFuelTicksAt(accessor.getLevel().getGameTime()))));
        }
        if (!campfire.isDead() && !campfire.hasTinder()) {
            tooltip.add(Component.translatable("jade.revivalages.campfire.blocked.no_tinder"));
        } else if (!campfire.isDead() && campfire.ashLevel() >= 8) {
            tooltip.add(Component.translatable("jade.revivalages.campfire.blocked.ash"));
        }
        if (campfire.isBurned()) {
            tooltip.add(Component.translatable("jade.revivalages.campfire.burned")
                    .withStyle(ChatFormatting.RED));
        } else if (campfire.isCompleted()) {
            tooltip.add(Component.translatable("jade.revivalages.primitive.ready")
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    private static void appendChopping(ITooltip tooltip, BlockAccessor accessor, ChoppingBlockEntity chopping) {
        appendItemProgress(tooltip, chopping.input(), chopping.recipeOutput(), chopping.progress());
        tooltip.add(Component.translatable("jade.revivalages.chopping.damage", accessor.getBlockState().getValue(ChoppingBlock.DAMAGE), 5));
        long remainingChops = chopping.remainingChops();
        tooltip.add(remainingChops < 0
                ? Component.translatable("jade.revivalages.chopping.unlimited")
                : Component.translatable("jade.revivalages.chopping.remaining_chops", remainingChops));
        tooltip.add(Component.translatable("jade.revivalages.chopping.chips", chopping.sawdust()));
        if (!chopping.output().isEmpty()) {
            tooltip.add(List.of(IElementHelper.get().item(chopping.output())));
        }
    }

    private static void appendPitKiln(ITooltip tooltip, BlockAccessor accessor, PitKilnBlockEntity kiln,
            boolean fireTarget) {
        PitKilnStage stage = kiln.getBlockState().getValue(PitKilnBlock.STAGE);
        if (!kiln.input().isEmpty()) {
            appendItemProgress(tooltip, kiln.input(), kiln.recipeOutput(),
                    stage == PitKilnStage.ACTIVE ? kiln.progressAt(accessor.getLevel().getGameTime()) : 0.0D);
        }
        if (stage == PitKilnStage.ACTIVE) {
            if (kiln.isStructureValid()) {
                tooltip.add(Component.translatable("jade.revivalages.pit_kiln.firing")
                        .withStyle(ChatFormatting.GREEN));
            } else if (fireTarget) {
                tooltip.add(Component.translatable("jade.revivalages.pit_kiln.structure.invalid")
                        .withStyle(ChatFormatting.RED));
            }
        }
        if (fireTarget) {
            return;
        }
        boolean valid = kiln.isStructureValid();
        tooltip.add(Component.translatable("jade.revivalages.pit_kiln.structure." + (valid ? "valid" : "invalid"))
                .withStyle(valid ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (!valid && kiln.invalidStructureTicks() > 0) {
            tooltip.add(Component.translatable("jade.revivalages.pit_kiln.invalid_grace",
                    kiln.invalidStructureTicks(), kiln.maximumInvalidStructureTicks()));
        }
        if (PrimitiveTechnologyConfig.PIT_KILN_RAIN_EXTINGUISHES.get() && kiln.rainTicks() > 0) {
            appendRule(tooltip, ProcessRuleType.WEATHER_EXPOSURE, true);
            tooltip.add(Component.translatable("jade.revivalages.pit_kiln.rain", kiln.rainTicks(),
                    PrimitiveTechnologyConfig.PIT_KILN_RAIN_EXTINGUISH_TICKS.get()));
        }
        if (stage == PitKilnStage.EMPTY && !kiln.input().isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.pit_kiln.add_thatch")
                    .withStyle(ChatFormatting.RED));
        } else if (stage == PitKilnStage.THATCH || stage == PitKilnStage.WOOD) {
            tooltip.add(Component.translatable("jade.revivalages.pit_kiln.add_logs_ignite")
                    .withStyle(ChatFormatting.RED));
        }
        if (!kiln.displayOutput().isEmpty()) {
            tooltip.add(List.of(IElementHelper.get().item(kiln.displayOutput())));
        }
    }

    private static void appendBarrel(ITooltip tooltip, BlockAccessor accessor, BarrelBlockEntity barrel) {
        FluidStack output = barrel.recipeOutput();
        ItemStack itemOutput = barrel.recipeItemOutput();
        appendBarrelProcess(tooltip, barrel, output, itemOutput, accessor.getLevel().getGameTime());
        boolean sealed = accessor.getBlockState().getValue(BarrelBlock.SEALED);
        if (!output.isEmpty() || !itemOutput.isEmpty()) {
            if (barrel.recipeRequiresSeal() && !sealed) {
                tooltip.add(Component.translatable(
                        ProcessRulePresentation.of(ProcessRuleType.SEALED_MACHINE).statusKey())
                        .withStyle(ChatFormatting.RED));
            } else if (barrel.isOutputBlocked()) {
                tooltip.add(Component.translatable("jade.revivalages.barrel.output_blocked")
                        .withStyle(ChatFormatting.RED));
            } else {
                tooltip.add(Component.translatable("jade.revivalages.barrel.processing")
                        .withStyle(ChatFormatting.GREEN));
            }
        }
        tooltip.add(Component.translatable("jade.revivalages.barrel.state." + (sealed ? "sealed" : "open"))
                .withStyle(sealed ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (sealed) {
            long nearest = java.util.Arrays.stream(barrel.itemsForView())
                    .filter(stack -> FoodFreshnessApi.profile(stack).isPresent())
                    .mapToLong(FoodFreshnessApi::remaining)
                    .min()
                    .orElse(Long.MAX_VALUE);
            if (nearest != Long.MAX_VALUE) {
                tooltip.add(Component.translatable(
                        "jade.revivalages.barrel.preservation",
                        Math.max(0L, nearest / 20L)
                ));
            }
        }
        if (!barrel.output().isEmpty()) {
            tooltip.add(List.of(IElementHelper.get().item(barrel.output())));
        }
        if (output.isEmpty() && itemOutput.isEmpty() && sealed) {
            for (ItemStack item : barrel.itemsForView()) {
                if (!item.isEmpty()) {
                    tooltip.add(Component.translatable("jade.revivalages.primitive.no_recipe", item.getHoverName()));
                    break;
                }
            }
        }
    }

    private static void appendSoakingPot(ITooltip tooltip, BlockAccessor accessor, SoakingPotBlockEntity pot) {
        appendItemProgress(tooltip, pot.input(), pot.recipeOutput(),
                pot.progressAt(accessor.getLevel().getGameTime()));
        if (pot.processRules().stream().anyMatch(rule -> rule.type() == ProcessRuleType.LIT_BLOCK_BELOW)
                && !pot.isRuleSatisfied(ProcessRuleType.LIT_BLOCK_BELOW)) {
            tooltip.add(Component.translatable("jade.revivalages.soaking_pot.no_heat")
                    .withStyle(ChatFormatting.RED));
        }
        if (!pot.output().isEmpty()) {
            tooltip.add(List.of(IElementHelper.get().item(pot.output())));
        }
    }

    private static void appendTanningRack(ITooltip tooltip, BlockAccessor accessor, TanningRackBlockEntity rack) {
        appendItemProgress(tooltip, rack.input(), rack.recipeOutput(),
                rack.progressAt(accessor.getLevel().getGameTime()));
        tooltip.add(Component.translatable("jade.revivalages.tanning.sky." + (rack.openSky() ? "clear" : "blocked"))
                .withStyle(rack.openSky() ? ChatFormatting.GREEN : ChatFormatting.RED));
        tooltip.add(Component.translatable("jade.revivalages.tanning.time." + (rack.daytime() ? "day" : "night"))
                .withStyle(rack.daytime() ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (PrimitiveTechnologyConfig.TANNING_RACK_RAIN_RUIN_TICKS.get() >= 0
                && (rack.raining() || rack.rainTicks() > 0)) {
            appendRule(tooltip, ProcessRuleType.WEATHER_EXPOSURE, rack.raining());
            tooltip.add(Component.translatable("jade.revivalages.tanning.rain", rack.rainTicks(), PrimitiveTechnologyConfig.TANNING_RACK_RAIN_RUIN_TICKS.get()));
            if (!rack.rainFailureOutput().isEmpty()) {
                tooltip.add(Component.translatable(
                        "gui.revivalages.process_rule.weather_exposure.failure", rack.rainFailureOutput().getHoverName()));
            }
        }
        if (!rack.output().isEmpty()) {
            tooltip.add(List.of(IElementHelper.get().item(rack.output())));
        }
    }

    private static void appendPitBurn(ITooltip tooltip, BlockAccessor accessor, PitBurnBlockEntity burn) {
        if (accessor.getBlockState().is(PitBurnFeature.ASH_PILE.get())) {
            tooltip.add(Component.translatable("jade.revivalages.pit_burn.ash"));
            return;
        }
        boolean valid = burn.isStructureValid();
        appendRule(tooltip, ProcessRuleType.VALID_STRUCTURE, !valid);
        tooltip.add(Component.translatable("jade.revivalages.pit_burn.structure." + (valid ? "valid" : "invalid")));
        if (!valid && burn.invalidStructureTicks() > 0) {
            tooltip.add(Component.translatable("jade.revivalages.pit_burn.invalid_grace",
                    burn.invalidStructureTicks(), burn.maximumInvalidStructureTicks()));
        }
        tooltip.add(Component.translatable("jade.revivalages.pit_burn.stages", burn.completedStages(), burn.stages()));
        appendItemProgress(tooltip, new ItemStack(PitBurnFeature.LOG_PILE_ITEM.get()), burn.recipeOutput(),
                burn.progressAt(accessor.getLevel().getGameTime()));
    }

    private static void appendWoodTorch(ITooltip tooltip, BlockAccessor accessor, WoodTorchBlockEntity torch) {
        String state = accessor.getBlockState().getValue(WoodTorchBlock.STATE).getSerializedName();
        tooltip.add(Component.translatable("jade.revivalages.wood_torch.state." + state)
                .withStyle(state.equals("lit") ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (state.equals("lit") && !WoodTorchSettings.clientSnapshot().burnsUp()) {
            tooltip.add(Component.translatable("jade.revivalages.wood_torch.unlimited"));
        } else if (torch.remainingTicks() >= 0 && state.equals("lit")) {
            tooltip.add(Component.translatable("jade.revivalages.wood_torch.remaining",
                    wholeSeconds(torch.remainingTicksAt(accessor.getLevel().getGameTime()))));
        }
    }

    private static void appendRule(ITooltip tooltip, ProcessRuleType type, boolean blocked) {
        ProcessRulePresentation presentation = ProcessRulePresentation.of(type);
        tooltip.add(Component.translatable(blocked ? presentation.statusKey() : presentation.tooltipKey()));
    }

    private static void appendBarrelProcess(
            ITooltip tooltip,
            BarrelBlockEntity barrel,
            FluidStack result,
            ItemStack itemResult,
            long gameTime
    ) {
        IElementHelper elements = IElementHelper.get();
        List<IElement> line = new ArrayList<>();
        for (ItemStack input : barrel.itemsForView()) {
            if (!input.isEmpty()) {
                line.add(elements.item(input));
            }
        }
        if (result.isEmpty() && itemResult.isEmpty()) {
            if (!line.isEmpty()) {
                tooltip.add(line);
            }
            return;
        }
        FluidStack fluid = barrel.fluidTank().getFluid();
        if (!fluid.isEmpty()) {
            line.add(elements.fluid(JadeFluidObject.of(
                    fluid.getFluid(),
                    fluid.getAmount(),
                    fluid.getComponentsPatch()
            )));
        }
        line.add(elements.spacer(2, 0));
        line.add(JadeProgressElement.of(elements, barrel.progressAt(gameTime)));
        line.add(elements.spacer(2, 0));
        if (result.isEmpty()) {
            line.add(elements.item(itemResult));
        } else {
            line.add(elements.fluid(JadeFluidObject.of(
                    result.getFluid(), result.getAmount(), result.getComponentsPatch())));
        }
        tooltip.add(line);
    }

    private static void appendItemProgress(ITooltip tooltip, ItemStack input, ItemStack output, double progress) {
        if (input.isEmpty()) {
            return;
        }
        if (output.isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.primitive.no_recipe", input.getHoverName()));
            return;
        }
        IElementHelper elements = IElementHelper.get();
        List<IElement> line = new ArrayList<>();
        line.add(elements.item(input));
        line.add(elements.spacer(2, 0));
        line.add(JadeProgressElement.of(elements, progress));
        line.add(elements.spacer(2, 0));
        line.add(elements.item(output));
        tooltip.add(line);
    }

    private static void appendProgress(ITooltip tooltip, double progress) {
        tooltip.add(List.of(JadeProgressElement.of(IElementHelper.get(), progress)));
    }

    private static long wholeSeconds(int ticks) {
        return (Math.max(0L, ticks) + 19L) / 20L;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
