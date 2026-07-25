package com.protyvkultury.revivalages.integration.emi;

import com.mojang.blaze3d.platform.InputConstants;
import com.protyvkultury.revivalages.feature.technology.knapping.client.KnappingScreen;
import com.protyvkultury.revivalages.feature.technology.knapping.view.KnappingRecipeView;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

final class KnappingEmiRecipe extends BasicEmiRecipe {

    private final PatternWidget pattern;

    KnappingEmiRecipe(EmiRecipeCategory category, KnappingRecipeView view) {
        super(category, view.id(), view.pattern().width() * 16 + 66, view.pattern().height() * 16 + 10);
        EmiIngredient material = EmiIngredient.of(
                view.effectiveInput().ingredient(),
                view.effectiveInput().count()
        );
        this.inputs = List.of(material);
        this.outputs = List.of(EmiStack.of(view.result()));
        this.pattern = new PatternWidget(view, 5, 5);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.add(pattern);
        Bounds bounds = pattern.getBounds();
        int centerY = bounds.y() + Math.max(0, (bounds.height() - 18) / 2);
        Widget arrow = widgets.addFillingArrow(bounds.right() + 4, centerY, 3000);
        widgets.addSlot(outputs.getFirst(), arrow.getBounds().right() + 4, centerY - 1)
                .recipeContext(this);
    }

    private static final class PatternWidget extends Widget {

        private static final long CYCLE_MILLIS = 1_000L;

        private final KnappingRecipeView view;
        private final int x;
        private final int y;
        private final ItemStack[] candidates;
        private int displayIndex;
        private long lastCycle;
        private @Nullable ItemStack displayed;

        private PatternWidget(KnappingRecipeView view, int x, int y) {
            this.view = view;
            this.x = x;
            this.y = y;
            this.candidates = view.effectiveInput().getItems();
        }

        @Override
        public Bounds getBounds() {
            return new Bounds(x, y, view.pattern().width() * 16, view.pattern().height() * 16);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            cycle();
            graphics.fill(
                    x - 1,
                    y - 1,
                    x + view.pattern().width() * 16 + 1,
                    y + view.pattern().height() * 16 + 1,
                    0xFFAAAAAA
            );
            if (displayed == null) {
                return;
            }
            for (int row = 0; row < view.pattern().height(); row++) {
                for (int column = 0; column < view.pattern().width(); column++) {
                    boolean on = view.pattern().on(column, row);
                    ResourceLocation texture = on
                            ? KnappingScreen.textureFor(displayed, view.type(), true)
                            : view.hasOffTexture()
                                    ? KnappingScreen.textureFor(displayed, view.type(), false)
                                    : null;
                    if (texture != null) {
                        graphics.blit(
                                texture,
                                x + column * 16,
                                y + row * 16,
                                0,
                                0,
                                16,
                                16,
                                16,
                                16
                        );
                    }
                }
            }
        }

        @Override
        public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
            return displayed == null ? List.of() : EmiStack.of(displayed).getTooltip();
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            if (displayed == null) {
                return true;
            }
            if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                EmiApi.displayRecipes(EmiStack.of(displayed));
            } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
                EmiApi.displayUses(EmiStack.of(displayed));
            }
            return true;
        }

        private void cycle() {
            if (candidates.length == 0) {
                displayed = view.viewerIcon();
                return;
            }
            long cycle = System.currentTimeMillis() / CYCLE_MILLIS;
            if (displayed == null || cycle > lastCycle) {
                lastCycle = cycle;
                if (displayed != null && Screen.hasShiftDown()) {
                    return;
                }
                displayed = candidates[displayIndex++ % candidates.length];
            }
        }
    }
}
