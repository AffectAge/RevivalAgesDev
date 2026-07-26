package com.protyvkultury.revivalages.data;

import com.protyvkultury.revivalages.data.content.ContentAvailabilityDataProvider;
import com.protyvkultury.revivalages.data.itemsize.ItemSizeDataProvider;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/** Single composition point for Revival Ages data providers. */
public final class RevivalAgesDataGenerators {

    private RevivalAgesDataGenerators() {
    }

    public static void gatherData(GatherDataEvent event) {
        if (event.includeServer()) {
            event.createProvider(output -> new ItemSizeDataProvider(
                    output,
                    event.getLookupProvider()
            ));
            event.createProvider(output -> new ContentAvailabilityDataProvider(
                    output,
                    event.getResourceManager(PackType.SERVER_DATA)
            ));
        }
    }
}
