package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class SupportService {

    private static volatile int maximumHorizontal;
    private static volatile int maximumUp;
    private static volatile int maximumDown;

    private SupportService() {
    }

    public static boolean isSupported(BlockGetter level, BlockPos target) {
        int horizontal = maximumHorizontal();
        int up = maximumUp();
        int down = maximumDown();
        for (BlockPos cursor : BlockPos.betweenClosed(
                target.offset(-horizontal, -up, -horizontal),
                target.offset(horizontal, down, horizontal)
        )) {
            SupportDefinition definition = definition(level.getBlockState(cursor));
            if (definition != null && definition.supports(cursor, target)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static SupportDefinition definition(BlockState state) {
        if (!StructuralIntegrityConfig.supportBeamsEnabled()) {
            return null;
        }
        SupportDefinition definition = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .wrapAsHolder(state.getBlock())
                .getData(StructuralIntegrityFeature.SUPPORT_DATA);
        return definition != null && definition.matches(state) ? definition : null;
    }

    public static void refreshRanges(net.neoforged.neoforge.event.TagsUpdatedEvent event) {
        int horizontal = 0;
        int up = 0;
        int down = 0;
        for (net.minecraft.world.level.block.Block block : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
            SupportDefinition definition = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                    .wrapAsHolder(block)
                    .getData(StructuralIntegrityFeature.SUPPORT_DATA);
            if (definition != null) {
                horizontal = Math.max(horizontal, definition.supportHorizontal());
                up = Math.max(up, definition.supportUp());
                down = Math.max(down, definition.supportDown());
            }
        }
        maximumHorizontal = horizontal;
        maximumUp = up;
        maximumDown = down;
    }

    private static int maximumHorizontal() {
        return maximumHorizontal;
    }

    private static int maximumUp() {
        return maximumUp;
    }

    private static int maximumDown() {
        return maximumDown;
    }
}
