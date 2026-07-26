package com.protyvkultury.revivalages.feature.food.spoilage.mixin;

import com.protyvkultury.revivalages.api.food.FoodFreshnessApi;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FoodFurnaceMixin {

    @Unique
    private static final ThreadLocal<List<ItemStack>> REVIVALAGES$INPUTS = new ThreadLocal<>();

    @Inject(method = "burn", at = @At("HEAD"))
    private static void revivalages$captureFreshness(
            RegistryAccess registries,
            RecipeHolder<?> recipe,
            NonNullList<ItemStack> items,
            int maximumStackSize,
            AbstractFurnaceBlockEntity furnace,
            CallbackInfoReturnable<Boolean> callback
    ) {
        REVIVALAGES$INPUTS.set(List.of(items.get(0).copy(), items.get(2).copy()));
    }

    @Inject(method = "burn", at = @At("RETURN"))
    private static void revivalages$inheritFreshness(
            RegistryAccess registries,
            RecipeHolder<?> recipe,
            NonNullList<ItemStack> items,
            int maximumStackSize,
            AbstractFurnaceBlockEntity furnace,
            CallbackInfoReturnable<Boolean> callback
    ) {
        List<ItemStack> inputs = REVIVALAGES$INPUTS.get();
        REVIVALAGES$INPUTS.remove();
        if (callback.getReturnValueZ() && inputs != null) {
            FoodFreshnessApi.transformOutput(items.get(2), inputs, recipe.id());
        }
    }
}
