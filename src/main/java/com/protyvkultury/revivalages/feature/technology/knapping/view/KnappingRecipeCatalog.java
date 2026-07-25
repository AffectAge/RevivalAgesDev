package com.protyvkultury.revivalages.feature.technology.knapping.view;

import com.protyvkultury.revivalages.feature.technology.knapping.KnappingConfig;
import com.protyvkultury.revivalages.feature.technology.knapping.KnappingFeature;
import com.protyvkultury.revivalages.feature.technology.knapping.KnappingType;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;

public final class KnappingRecipeCatalog {

    private KnappingRecipeCatalog() {
    }

    public static List<KnappingRecipeView> recipes(RecipeManager manager, RegistryAccess registries) {
        if (!KnappingConfig.enabled()) {
            return List.of();
        }
        var types = registries.registryOrThrow(KnappingFeature.KNAPPING_TYPES);
        return manager.getAllRecipesFor(KnappingFeature.RECIPE_TYPE.get()).stream()
                .map(holder -> {
                    KnappingType type = types.get(holder.value().knappingType());
                    if (type == null) {
                        return null;
                    }
                    return new KnappingRecipeView(
                            holder.id(),
                            holder.value().knappingType(),
                            type.input(),
                            holder.value().ingredient().orElse(Ingredient.EMPTY),
                            holder.value().pattern(),
                            holder.value().result(),
                            type.hasOffTexture(),
                            type.viewerIcon()
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
