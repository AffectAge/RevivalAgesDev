package com.protyvkultury.revivalages.integration.curios;

import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.api.weight.RegisterCarriedWeightProvidersEvent;
import com.protyvkultury.revivalages.api.weight.WeightResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;

public final class CuriosCarriedWeightIntegration {

    private CuriosCarriedWeightIntegration() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(CuriosCarriedWeightIntegration::registerProviders);
    }

    private static void registerProviders(RegisterCarriedWeightProvidersEvent event) {
        event.registrar().registerPlayerWeightSource(
                RevivalAges.id("curios_equipment_weight"),
                1_000,
                (player, context, lookup) -> CuriosApi.getCuriosInventory(player)
                        .map(handler -> calculate(handler.getEquippedCurios(), context, lookup))
                        .orElse(WeightResult.ZERO)
        );
    }

    private static WeightResult calculate(
            IItemHandler handler,
            com.protyvkultury.revivalages.api.weight.WeightContext context,
            com.protyvkultury.revivalages.api.weight.WeightLookup lookup
    ) {
        WeightResult total = WeightResult.ZERO;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                total = total.add(lookup.getWeight(stack, context).multiply(stack.getCount()));
            }
        }
        return total;
    }
}
