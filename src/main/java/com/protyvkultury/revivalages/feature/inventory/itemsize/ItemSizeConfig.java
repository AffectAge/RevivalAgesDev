package com.protyvkultury.revivalages.feature.inventory.itemsize;

import com.protyvkultury.revivalages.api.size.Size;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ItemSizeConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.EnumValue<Size> CHEST_MAXIMUM_SIZE;
    public static final ModConfigSpec.EnumValue<Size> BUNDLE_MAXIMUM_SIZE;
    public static final ModConfigSpec.EnumValue<Size> PIT_KILN_BATCHABLE_MAXIMUM_SIZE;
    public static final ModConfigSpec.IntValue PIT_KILN_BATCH_SIZE;
    public static final ModConfigSpec.IntValue PIT_KILN_OVERSIZED_BATCH_SIZE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CONTAINER_OVERRIDES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ENABLED = builder
                .comment(
                        "Enables Item Size tooltips and storage restrictions.",
                        "Changing this value requires a server restart."
                )
                .define("itemSize.enabled", true);
        CHEST_MAXIMUM_SIZE = builder
                .comment("Largest item size accepted by normal and trapped chests.")
                .defineEnum("itemSize.chestMaximumSize", Size.LARGE);
        BUNDLE_MAXIMUM_SIZE = builder
                .comment("Largest item size accepted by bundles.")
                .defineEnum("itemSize.bundleMaximumSize", Size.NORMAL);
        PIT_KILN_BATCHABLE_MAXIMUM_SIZE = builder
                .comment("Largest item size that uses the normal Pit Kiln batch capacity.")
                .defineEnum("itemSize.pitKiln.batchableMaximumSize", Size.LARGE);
        PIT_KILN_BATCH_SIZE = builder
                .comment("Maximum Pit Kiln input count for items at or below the batchable size.")
                .defineInRange("itemSize.pitKiln.batchSize", 4, 1, 64);
        PIT_KILN_OVERSIZED_BATCH_SIZE = builder
                .comment("Maximum Pit Kiln input count for items above the batchable size.")
                .defineInRange("itemSize.pitKiln.oversizedBatchSize", 1, 1, 64);
        CONTAINER_OVERRIDES = builder
                .comment(
                        "Optional adapter overrides in the form block|namespace:id=size or item|namespace:id=size.",
                        "An entry only has an effect when that container has an Item Size adapter."
                )
                .defineListAllowEmpty(
                        "itemSize.containerOverrides",
                        List.of(),
                        () -> "block|minecraft:chest=large",
                        ItemSizeConfig::validOverride
                );
        SPEC = builder.build();
    }

    private ItemSizeConfig() {
    }

    public static boolean configuredEnabled() {
        return SPEC.isLoaded() ? ENABLED.get() : ENABLED.getDefault();
    }

    private static boolean validOverride(Object value) {
        return value instanceof String text && ItemSizeSettings.parseOverride(text) != null;
    }
}
