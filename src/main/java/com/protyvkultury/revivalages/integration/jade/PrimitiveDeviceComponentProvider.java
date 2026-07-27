package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.barrel.block.BarrelBlock;
import com.protyvkultury.revivalages.feature.technology.barrel.blockentity.BarrelBlockEntity;
import com.protyvkultury.revivalages.feature.technology.campfire.blockentity.CampfireBlockEntity;
import com.protyvkultury.revivalages.feature.technology.choppingblock.block.ChoppingBlock;
import com.protyvkultury.revivalages.feature.technology.choppingblock.blockentity.ChoppingBlockEntity;
import com.protyvkultury.revivalages.feature.technology.pitkiln.block.PitKilnBlock;
import com.protyvkultury.revivalages.feature.technology.pitkiln.blockentity.PitKilnBlockEntity;
import com.protyvkultury.revivalages.api.size.SizeApi;
import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.soakingpot.blockentity.SoakingPotBlockEntity;
import com.protyvkultury.revivalages.feature.technology.tanningrack.blockentity.TanningRackBlockEntity;
import com.protyvkultury.revivalages.feature.technology.pitburn.PitBurnFeature;
import com.protyvkultury.revivalages.feature.technology.pitburn.blockentity.PitBurnBlockEntity;
import com.protyvkultury.revivalages.feature.technology.ignition.block.WoodTorchBlock;
import com.protyvkultury.revivalages.feature.technology.ignition.blockentity.WoodTorchBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
        if (DisabledContentComponentProvider.isDisabled(accessor)) {
            return;
        }
        switch (accessor.getBlockEntity()) {
            case CampfireBlockEntity campfire -> appendCampfire(tooltip, campfire);
            case ChoppingBlockEntity chopping -> appendChopping(tooltip, accessor, chopping);
            case PitKilnBlockEntity kiln -> appendPitKiln(tooltip, accessor, kiln);
            case BarrelBlockEntity barrel -> appendBarrel(tooltip, accessor, barrel);
            case SoakingPotBlockEntity pot -> appendSoakingPot(tooltip, accessor, pot);
            case TanningRackBlockEntity rack -> appendTanningRack(tooltip, accessor, rack);
            case PitBurnBlockEntity burn -> appendPitBurn(tooltip, accessor, burn);
            case WoodTorchBlockEntity torch -> appendWoodTorch(tooltip, accessor, torch);
            default -> {
            }
        }
    }

    private static void appendCampfire(ITooltip tooltip, CampfireBlockEntity campfire) {
        appendItemProgress(tooltip, campfire.cookingStack(), campfire.recipeOutput(), campfire.progress());
        String state = campfire.isDead() ? "dead" : campfire.isLit() ? "lit" : "unlit";
        tooltip.add(Component.translatable("jade.revivalages.campfire.state." + state));
        tooltip.add(Component.translatable("jade.revivalages.campfire.fuel", campfire.fuelLevel(), 8));
        tooltip.add(Component.translatable("jade.revivalages.campfire.ash", campfire.ashLevel(), 8));
        if (campfire.isLit()) {
            tooltip.add(Component.translatable("jade.revivalages.campfire.burn_time", formatSeconds(campfire.burnTime())));
        }
        if (!campfire.isDead() && !campfire.hasTinder()) {
            tooltip.add(Component.translatable("jade.revivalages.campfire.blocked.no_tinder"));
        } else if (!campfire.isDead() && campfire.ashLevel() >= 8) {
            tooltip.add(Component.translatable("jade.revivalages.campfire.blocked.ash"));
        }
        if (campfire.isCompleted()) {
            tooltip.add(Component.translatable("jade.revivalages.primitive.ready"));
        }
    }

    private static void appendChopping(ITooltip tooltip, BlockAccessor accessor, ChoppingBlockEntity chopping) {
        appendItemProgress(tooltip, chopping.input(), chopping.recipeOutput(), chopping.progress());
        tooltip.add(Component.translatable("jade.revivalages.chopping.damage", accessor.getBlockState().getValue(ChoppingBlock.DAMAGE), 5));
        tooltip.add(Component.translatable("jade.revivalages.chopping.chips", chopping.sawdust()));
        if (!chopping.output().isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.primitive.ready_item", chopping.output().getHoverName()));
        }
    }

    private static void appendPitKiln(ITooltip tooltip, BlockAccessor accessor, PitKilnBlockEntity kiln) {
        appendItemProgress(tooltip, kiln.input(), kiln.recipeOutput(), kiln.progress());
        String stage = accessor.getBlockState().getValue(PitKilnBlock.STAGE).getSerializedName();
        tooltip.add(Component.translatable("jade.revivalages.pit_kiln.stage", Component.translatable("jade.revivalages.pit_kiln.stage." + stage)));
        boolean valid = kiln.isStructureValid();
        tooltip.add(Component.translatable("jade.revivalages.pit_kiln.structure." + (valid ? "valid" : "invalid")));
        if (!valid && kiln.invalidStructureTicks() > 0) {
            tooltip.add(Component.translatable("jade.revivalages.pit_kiln.invalid_grace",
                    kiln.invalidStructureTicks(), kiln.maximumInvalidStructureTicks()));
        }
        tooltip.add(Component.translatable("jade.revivalages.pit_kiln.logs", kiln.logCount(), 3));
        if (!kiln.input().isEmpty()) {
            tooltip.add(Component.translatable(
                    "jade.revivalages.pit_kiln.item_size",
                    Component.translatable(
                            "size.revivalages." + SizeApi.getSize(kiln.input()).getSerializedName()
                    ),
                    kiln.maximumInputCount(kiln.input())
            ));
        }
        if (!kiln.displayOutput().isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.primitive.ready_item", kiln.displayOutput().getHoverName()));
        }
    }

    private static void appendBarrel(ITooltip tooltip, BlockAccessor accessor, BarrelBlockEntity barrel) {
        FluidStack output = barrel.recipeOutput();
        appendBarrelProcess(tooltip, barrel, output);
        boolean sealed = accessor.getBlockState().getValue(BarrelBlock.SEALED);
        tooltip.add(Component.translatable("jade.revivalages.barrel.state." + (sealed ? "sealed" : "open")));
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
        appendFluidIfPresent(tooltip, barrel.fluidTank().getFluid(), barrel.fluidTank().getCapacity());
        if (!output.isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.barrel.result", output.getHoverName(), output.getAmount()));
        } else if (sealed) {
            for (ItemStack item : barrel.itemsForView()) {
                if (!item.isEmpty()) {
                    tooltip.add(Component.translatable("jade.revivalages.primitive.no_recipe", item.getHoverName()));
                    break;
                }
            }
        }
    }

    private static void appendSoakingPot(ITooltip tooltip, BlockAccessor accessor, SoakingPotBlockEntity pot) {
        appendItemProgress(tooltip, pot.input(), pot.recipeOutput(), pot.progress());
        appendFluidIfPresent(tooltip, pot.fluidTank().getFluid(), pot.fluidTank().getCapacity());
        if (pot.requiresCampfire()) {
            boolean heated = accessor.getLevel().getBlockState(accessor.getPosition().below())
                    .getOptionalValue(com.protyvkultury.revivalages.feature.technology.campfire.block.CampfireBlock.LIT)
                    .orElse(false);
            if (!heated) {
                tooltip.add(Component.translatable("jade.revivalages.soaking_pot.heat.required"));
            }
        }
        if (!pot.output().isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.primitive.ready_item", pot.output().getHoverName()));
        }
    }

    private static void appendTanningRack(ITooltip tooltip, BlockAccessor accessor, TanningRackBlockEntity rack) {
        appendItemProgress(tooltip, rack.input(), rack.recipeOutput(), rack.progress());
        boolean sky = accessor.getLevel().canSeeSky(accessor.getPosition());
        boolean day = accessor.getLevel().isDay();
        boolean raining = accessor.getLevel().isRainingAt(accessor.getPosition().above());
        tooltip.add(Component.translatable("jade.revivalages.tanning.sky." + (sky ? "clear" : "blocked")));
        tooltip.add(Component.translatable("jade.revivalages.tanning.time." + (day ? "day" : "night")));
        if (PrimitiveTechnologyConfig.TANNING_RACK_RAIN_RUIN_TICKS.get() >= 0
                && (raining || rack.rainTicks() > 0)) {
            tooltip.add(Component.translatable("jade.revivalages.tanning.rain", rack.rainTicks(), PrimitiveTechnologyConfig.TANNING_RACK_RAIN_RUIN_TICKS.get()));
        }
        if (!rack.output().isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.primitive.ready_item", rack.output().getHoverName()));
        }
    }

    private static void appendPitBurn(ITooltip tooltip, BlockAccessor accessor, PitBurnBlockEntity burn) {
        if (accessor.getBlockState().is(PitBurnFeature.ASH_PILE.get())) {
            tooltip.add(Component.translatable("jade.revivalages.pit_burn.ash"));
            return;
        }
        boolean valid = burn.isStructureValid();
        tooltip.add(Component.translatable("jade.revivalages.pit_burn.structure." + (valid ? "valid" : "invalid")));
        if (!valid && burn.invalidStructureTicks() > 0) {
            tooltip.add(Component.translatable("jade.revivalages.pit_burn.invalid_grace",
                    burn.invalidStructureTicks(), burn.maximumInvalidStructureTicks()));
        }
        tooltip.add(Component.translatable("jade.revivalages.pit_burn.stages", burn.completedStages(), burn.stages()));
        appendItemProgress(tooltip, new ItemStack(PitBurnFeature.LOG_PILE_ITEM.get()), burn.recipeOutput(), burn.progress());
    }

    private static void appendWoodTorch(ITooltip tooltip, BlockAccessor accessor, WoodTorchBlockEntity torch) {
        String state = accessor.getBlockState().getValue(WoodTorchBlock.STATE).getSerializedName();
        tooltip.add(Component.translatable("jade.revivalages.wood_torch.state." + state));
        if (torch.remainingTicks() >= 0 && state.equals("lit")) {
            tooltip.add(Component.translatable("jade.revivalages.wood_torch.remaining", formatSeconds(torch.remainingTicks())));
        }
    }

    private static void appendFluidIfPresent(ITooltip tooltip, FluidStack fluid, int capacity) {
        if (!fluid.isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.primitive.fluid", fluid.getHoverName(), fluid.getAmount(), capacity));
        }
    }

    private static void appendBarrelProcess(
            ITooltip tooltip,
            BarrelBlockEntity barrel,
            FluidStack result
    ) {
        if (result.isEmpty()) {
            return;
        }
        IElementHelper elements = IElementHelper.get();
        List<IElement> line = new ArrayList<>();
        for (ItemStack input : barrel.itemsForView()) {
            if (!input.isEmpty()) {
                line.add(elements.item(input));
            }
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
        line.add(elements.progress((float) Math.clamp(barrel.progress(), 0.0D, 1.0D)));
        line.add(elements.spacer(2, 0));
        line.add(elements.fluid(JadeFluidObject.of(
                result.getFluid(),
                result.getAmount(),
                result.getComponentsPatch()
        )));
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
        line.add(elements.progress((float) Math.clamp(progress, 0.0D, 1.0D)));
        line.add(elements.spacer(2, 0));
        line.add(elements.item(output));
        tooltip.add(line);
    }

    private static void appendProgress(ITooltip tooltip, double progress) {
        tooltip.add(List.of(IElementHelper.get().progress((float) Math.clamp(progress, 0.0D, 1.0D))));
    }

    private static String formatSeconds(int ticks) {
        return String.format(Locale.ROOT, "%.1f s", ticks / 20.0D);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
