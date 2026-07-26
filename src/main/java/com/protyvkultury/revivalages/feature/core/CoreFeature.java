package com.protyvkultury.revivalages.feature.core;

import com.mojang.serialization.MapCodec;
import com.protyvkultury.revivalages.RevivalAges;
import com.protyvkultury.revivalages.feature.FeatureModule;
import com.protyvkultury.revivalages.feature.content.AnyContentEnabledCondition;
import com.protyvkultury.revivalages.feature.content.ContentEnabledCondition;
import com.protyvkultury.revivalages.feature.content.ContentPolicy;
import com.protyvkultury.revivalages.feature.core.registry.CoreBlocks;
import com.protyvkultury.revivalages.feature.core.registry.CoreItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class CoreFeature implements FeatureModule {

    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, RevivalAges.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ContentEnabledCondition>>
            CONTENT_ENABLED_CONDITION = CONDITIONS.register(
                    "content_enabled",
                    () -> ContentEnabledCondition.CODEC
            );
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<AnyContentEnabledCondition>>
            ANY_CONTENT_ENABLED_CONDITION = CONDITIONS.register(
                    "any_content_enabled",
                    () -> AnyContentEnabledCondition.CODEC
            );

    @Override
    public ContentPolicy contentPolicy() {
        return ContentPolicy.infrastructure("core");
    }

    @Override
    public void register(IEventBus modBus, ModContainer modContainer) {
        CoreBlocks.register(modBus);
        CoreItems.register(modBus);
        CONDITIONS.register(modBus);
    }
}
