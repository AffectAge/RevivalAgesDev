package com.protyvkultury.revivalages.feature.technology.bucket.item;

import com.protyvkultury.revivalages.feature.technology.bucket.PrimitiveBucketFeature;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStackSimple;

final class PrimitiveBucketFluidHandler extends FluidHandlerItemStackSimple {

    private final int maximumUses;

    PrimitiveBucketFluidHandler(
            Supplier<DataComponentType<SimpleFluidContent>> componentType,
            ItemStack container,
            int capacity,
            int maximumUses
    ) {
        super(componentType, container, capacity);
        this.maximumUses = maximumUses;
    }

    @Override
    protected void setFluid(FluidStack fluid) {
        super.setFluid(fluid);
        if (!fluid.isEmpty()) {
            container.set(DataComponents.MAX_STACK_SIZE, 1);
        }
    }

    @Override
    protected void setContainerToEmpty() {
        super.setContainerToEmpty();
        int uses = container.getOrDefault(PrimitiveBucketFeature.BUCKET_USES.get(), maximumUses) - 1;
        if (uses <= 0) {
            container.shrink(1);
            return;
        }
        container.set(PrimitiveBucketFeature.BUCKET_USES.get(), uses);
        container.remove(DataComponents.MAX_STACK_SIZE);
    }
}
