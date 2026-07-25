package com.protyvkultury.revivalages.feature.world.structuralintegrity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum SupportWood {
    OAK("oak", Blocks.OAK_LOG),
    SPRUCE("spruce", Blocks.SPRUCE_LOG),
    BIRCH("birch", Blocks.BIRCH_LOG),
    JUNGLE("jungle", Blocks.JUNGLE_LOG),
    ACACIA("acacia", Blocks.ACACIA_LOG),
    DARK_OAK("dark_oak", Blocks.DARK_OAK_LOG),
    MANGROVE("mangrove", Blocks.MANGROVE_LOG),
    CHERRY("cherry", Blocks.CHERRY_LOG),
    BAMBOO("bamboo", Blocks.BAMBOO_BLOCK);

    private final String serializedName;
    private final Block sourceBlock;

    SupportWood(String serializedName, Block sourceBlock) {
        this.serializedName = serializedName;
        this.sourceBlock = sourceBlock;
    }

    public String serializedName() {
        return serializedName;
    }

    public Block sourceBlock() {
        return sourceBlock;
    }
}
