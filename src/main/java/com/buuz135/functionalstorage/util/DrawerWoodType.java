package com.buuz135.functionalstorage.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Locale;

public enum DrawerWoodType implements IWoodType {

    OAK(Blocks.OAK_LOG, Blocks.OAK_PLANKS),
    SPRUCE(Blocks.SPRUCE_LOG, Blocks.SPRUCE_PLANKS),
    BIRCH(Blocks.BIRCH_LOG, Blocks.BIRCH_PLANKS),
    JUNGLE(Blocks.JUNGLE_LOG, Blocks.JUNGLE_PLANKS),
    ACACIA(Blocks.ACACIA_LOG, Blocks.ACACIA_PLANKS),
    DARK_OAK(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS),
    CRIMSON(Blocks.CRIMSON_STEM, Blocks.CRIMSON_PLANKS),
    WARPED(Blocks.WARPED_STEM, Blocks.WARPED_PLANKS);


    private final Block log;
    private final Block planks;

    DrawerWoodType(Block log, Block planks) {
        this.log = log;
        this.planks = planks;
    }

    @Override
    public Block getWood() {
        return log;
    }

    @Override
    public Block getPlanks() {
        return planks;
    }

    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }


}
