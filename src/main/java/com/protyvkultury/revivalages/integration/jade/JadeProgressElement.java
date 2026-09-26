package com.protyvkultury.revivalages.integration.jade;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.JadeIds;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

/** Keeps Jade's standard arrow while allowing a new recipe to reset its progress. */
final class JadeProgressElement {

    private static final ResourceLocation BASE = JadeIds.JADE("progress_base");
    private static final ResourceLocation FILL = JadeIds.JADE("progress");

    private JadeProgressElement() {
    }

    static IElement of(IElementHelper elements, double progress) {
        return elements.progress((float) Math.clamp(progress, 0.0D, 1.0D),
                BASE, FILL, 22, 16, true);
    }
}
