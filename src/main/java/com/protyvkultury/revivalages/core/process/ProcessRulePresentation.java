package com.protyvkultury.revivalages.core.process;

import com.protyvkultury.revivalages.RevivalAges;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

/** Immutable atlas and localization metadata shared by every process-rule presentation adapter. */
public record ProcessRulePresentation(int u, int v, String tooltipKey, String statusKey) {

    public static final ResourceLocation ATLAS = RevivalAges.id("textures/gui/process_rules.png");
    public static final int ATLAS_WIDTH = 64;
    public static final int ATLAS_HEIGHT = 48;
    public static final int ICON_SIZE = 16;

    private static final Map<ProcessRuleType, ProcessRulePresentation> PRESENTATIONS = createPresentations();

    public static ProcessRulePresentation of(ProcessRuleType type) {
        ProcessRulePresentation presentation = PRESENTATIONS.get(type);
        if (presentation == null) {
            throw new IllegalStateException("Missing process-rule presentation for " + type.getSerializedName());
        }
        return presentation;
    }

    public static void validate() {
        EnumSet<ProcessRuleType> missing = EnumSet.allOf(ProcessRuleType.class);
        missing.removeAll(PRESENTATIONS.keySet());
        if (!missing.isEmpty() || PRESENTATIONS.size() != ProcessRuleType.values().length) {
            throw new IllegalStateException("Incomplete process-rule atlas metadata: " + missing);
        }
        java.util.HashSet<Long> cells = new java.util.HashSet<>();
        for (ProcessRulePresentation value : PRESENTATIONS.values()) {
            if (value.u < 0 || value.v < 0 || value.u + ICON_SIZE > ATLAS_WIDTH || value.v + ICON_SIZE > ATLAS_HEIGHT
                    || !cells.add(((long) value.u << 32) | Integer.toUnsignedLong(value.v))) {
                throw new IllegalStateException("Invalid or duplicate process-rule atlas cell");
            }
        }
    }

    /** Static viewer text. Live machine state remains synchronized separately for Jade. */
    public static List<Component> viewerTooltip(ProcessRuleView rule) {
        java.util.ArrayList<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(Component.translatable(of(rule.rule().type()).tooltipKey()));
        switch (rule.rule().type()) {
            case ATTACHED_WORKER -> {
                tooltip.add(Component.translatable("gui.revivalages.process_rule.attached_worker.animals"));
                tooltip.add(Component.translatable("gui.revivalages.process_rule.attached_worker.lead"));
            }
            case VALID_WORK_AREA -> {
                tooltip.add(Component.translatable("gui.revivalages.process_rule.valid_work_area.square"));
                tooltip.add(Component.translatable("gui.revivalages.process_rule.valid_work_area.floor"));
                tooltip.add(Component.translatable("gui.revivalages.process_rule.valid_work_area.headroom"));
            }
            default -> {
            }
        }
        if (rule.hasHazardFailure()) {
            tooltip.add(Component.translatable("gui.revivalages.process_rule.weather_exposure.failure", outcomeNames(rule)));
        }
        if (rule.hasChanceOutcome()) {
            tooltip.add(Component.translatable("gui.revivalages.process_rule.random_outcome.chance",
                    String.format(Locale.ROOT, "%.0f%%", rule.chance() * 100.0D)));
            String outcomeKey = "gui.revivalages.process_rule.random_outcome."
                    + rule.outcomeMode().serializedName();
            tooltip.add(switch (rule.outcomeMode()) {
                case PER_STAGE, PER_ATTEMPT -> Component.translatable(outcomeKey, rule.stages());
                default -> Component.translatable(outcomeKey);
            });
            tooltip.add(Component.translatable("gui.revivalages.process_rule.random_outcome.results", outcomeNames(rule)));
        }
        return List.copyOf(tooltip);
    }

    private static Component outcomeNames(ProcessRuleView rule) {
        return Component.literal(rule.outcomeResults().stream()
                .map(ItemStack::getHoverName)
                .map(Component::getString)
                .distinct()
                .collect(java.util.stream.Collectors.joining(", ")));
    }

    private static Map<ProcessRuleType, ProcessRulePresentation> createPresentations() {
        EnumMap<ProcessRuleType, ProcessRulePresentation> result = new EnumMap<>(ProcessRuleType.class);
        add(result, ProcessRuleType.LIT_BLOCK_BELOW, 0, 0);
        add(result, ProcessRuleType.OPEN_SKY, 16, 0);
        add(result, ProcessRuleType.WEATHER_EXPOSURE, 32, 0);
        add(result, ProcessRuleType.DRYING_ENVIRONMENT, 48, 0);
        add(result, ProcessRuleType.SEALED_MACHINE, 0, 16);
        add(result, ProcessRuleType.INSTALLED_TOOL, 16, 16);
        add(result, ProcessRuleType.FUELLED_AND_LIT, 32, 16);
        add(result, ProcessRuleType.ATTACHED_WORKER, 48, 16);
        add(result, ProcessRuleType.VALID_WORK_AREA, 0, 32);
        add(result, ProcessRuleType.VALID_STRUCTURE, 16, 32);
        add(result, ProcessRuleType.REQUIRED_MANUAL_TOOL, 32, 32);
        add(result, ProcessRuleType.RANDOM_OUTCOME, 48, 32);
        return Map.copyOf(result);
    }

    private static void add(Map<ProcessRuleType, ProcessRulePresentation> result, ProcessRuleType type, int u, int v) {
        String base = "gui.revivalages.process_rule." + type.getSerializedName();
        result.put(type, new ProcessRulePresentation(u, v, base, base + ".status"));
    }
}
