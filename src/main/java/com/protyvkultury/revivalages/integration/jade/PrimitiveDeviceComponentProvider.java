package com.protyvkultury.revivalages.integration.jade;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.technology.barrel.block.BarrelBlock;
import com.protyvkultury.revivalages.feature.technology.barrel.blockentity.BarrelBlockEntity;
import com.protyvkultury.revivalages.feature.technology.campfire.blockentity.CampfireBlockEntity;
import com.protyvkultury.revivalages.feature.technology.choppingblock.block.ChoppingBlock;
import com.protyvkultury.revivalages.feature.technology.choppingblock.blockentity.ChoppingBlockEntity;
import com.protyvkultury.revivalages.feature.technology.pitkiln.block.PitKilnBlock;
import com.protyvkultury.revivalages.feature.technology.pitkiln.blockentity.PitKilnBlockEntity;
import com.protyvkultury.revivalages.feature.technology.primitive.config.PrimitiveTechnologyConfig;
import com.protyvkultury.revivalages.feature.technology.soakingpot.blockentity.SoakingPotBlockEntity;
import com.protyvkultury.revivalages.feature.technology.tanningrack.blockentity.TanningRackBlockEntity;
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

/** Client-side, synced-state view for every primitive processing block. */
public enum PrimitiveDeviceComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = RevivalAges.id("primitive_devices");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        switch (accessor.getBlockEntity()) {
            case CampfireBlockEntity campfire -> appendCampfire(tooltip, campfire);
            case ChoppingBlockEntity chopping -> appendChopping(tooltip, accessor, chopping);
            case PitKilnBlockEntity kiln -> appendPitKiln(tooltip, accessor, kiln);
            case BarrelBlockEntity barrel -> appendBarrel(tooltip, accessor, barrel);
            case SoakingPotBlockEntity pot -> appendSoakingPot(tooltip, accessor, pot);
            case TanningRackBlockEntity rack -> appendTanningRack(tooltip, accessor, rack);
            default -> {
            }
        }
    }

    private static void appendCampfire(ITooltip tooltip, CampfireBlockEntity campfire) {
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
        appendItemProgress(tooltip, campfire.cookingStack(), campfire.recipeOutput(), campfire.progress());
        if (campfire.isCompleted()) {
            tooltip.add(Component.translatable("jade.revivalages.primitive.ready"));
        }
    }

    private static void appendChopping(ITooltip tooltip, BlockAccessor accessor, ChoppingBlockEntity chopping) {
        tooltip.add(Component.translatable("jade.revivalages.chopping.damage", accessor.getBlockState().getValue(ChoppingBlock.DAMAGE), 5));
        tooltip.add(Component.translatable("jade.revivalages.chopping.chips", chopping.sawdust()));
        appendItemProgress(tooltip, chopping.input(), chopping.recipeOutput(), chopping.progress());
    }

    private static void appendPitKiln(ITooltip tooltip, BlockAccessor accessor, PitKilnBlockEntity kiln) {
        String stage = accessor.getBlockState().getValue(PitKilnBlock.STAGE).getSerializedName();
        tooltip.add(Component.translatable("jade.revivalages.pit_kiln.stage", Component.translatable("jade.revivalages.pit_kiln.stage." + stage)));
        boolean valid = kiln.isStructureValid();
        tooltip.add(Component.translatable("jade.revivalages.pit_kiln.structure." + (valid ? "valid" : "invalid")));
        if (!valid && kiln.invalidStructureTicks() > 0) {
            tooltip.add(Component.translatable("jade.revivalages.pit_kiln.invalid_grace",
                    kiln.invalidStructureTicks(), kiln.maximumInvalidStructureTicks()));
        }
        tooltip.add(Component.translatable("jade.revivalages.pit_kiln.logs", kiln.logCount(), 3));
        appendItemProgress(tooltip, kiln.input(), kiln.recipeOutput(), kiln.progress());
        if (!kiln.displayOutput().isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.primitive.ready_item", kiln.displayOutput().getHoverName()));
        }
    }

    private static void appendBarrel(ITooltip tooltip, BlockAccessor accessor, BarrelBlockEntity barrel) {
        boolean sealed = accessor.getBlockState().getValue(BarrelBlock.SEALED);
        tooltip.add(Component.translatable("jade.revivalages.barrel.state." + (sealed ? "sealed" : "open")));
        appendFluid(tooltip, barrel.fluidTank().getFluid(), barrel.fluidTank().getCapacity());
        FluidStack output = barrel.recipeOutput();
        if (!output.isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.barrel.result", output.getHoverName(), output.getAmount()));
            appendProgress(tooltip, barrel.progress());
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
        appendFluid(tooltip, pot.fluidTank().getFluid(), pot.fluidTank().getCapacity());
        if (pot.requiresCampfire()) {
            boolean heated = accessor.getLevel().getBlockState(accessor.getPosition().below())
                    .getOptionalValue(com.protyvkultury.revivalages.feature.technology.campfire.block.CampfireBlock.LIT)
                    .orElse(false);
            tooltip.add(Component.translatable("jade.revivalages.soaking_pot.heat." + (heated ? "ready" : "required")));
        }
        appendItemProgress(tooltip, pot.input(), pot.recipeOutput(), pot.progress());
        if (!pot.output().isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.primitive.ready_item", pot.output().getHoverName()));
        }
    }

    private static void appendTanningRack(ITooltip tooltip, BlockAccessor accessor, TanningRackBlockEntity rack) {
        boolean sky = accessor.getLevel().canSeeSky(accessor.getPosition());
        boolean day = accessor.getLevel().isDay();
        boolean raining = accessor.getLevel().isRainingAt(accessor.getPosition().above());
        tooltip.add(Component.translatable("jade.revivalages.tanning.sky." + (sky ? "clear" : "blocked")));
        tooltip.add(Component.translatable("jade.revivalages.tanning.time." + (day ? "day" : "night")));
        if (PrimitiveTechnologyConfig.TANNING_RACK_RAIN_RUIN_TICKS.get() >= 0
                && (raining || rack.rainTicks() > 0)) {
            tooltip.add(Component.translatable("jade.revivalages.tanning.rain", rack.rainTicks(), PrimitiveTechnologyConfig.TANNING_RACK_RAIN_RUIN_TICKS.get()));
        }
        appendItemProgress(tooltip, rack.input(), rack.recipeOutput(), rack.progress());
        if (!rack.output().isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.primitive.ready_item", rack.output().getHoverName()));
        }
    }

    private static void appendFluid(ITooltip tooltip, FluidStack fluid, int capacity) {
        if (fluid.isEmpty()) {
            tooltip.add(Component.translatable("jade.revivalages.primitive.fluid.empty", capacity));
        } else {
            tooltip.add(Component.translatable("jade.revivalages.primitive.fluid", fluid.getHoverName(), fluid.getAmount(), capacity));
        }
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
