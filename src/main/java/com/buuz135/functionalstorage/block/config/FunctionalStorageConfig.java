package com.buuz135.functionalstorage.block.config;

import com.hrznstudio.titanium.annotation.config.ConfigFile;
import com.hrznstudio.titanium.annotation.config.ConfigVal;

@ConfigFile(value = "functionalstorage-common")
public class FunctionalStorageConfig {

    @ConfigVal(comment = "Armory slot amount")
    @ConfigVal.InRangeInt(min = 1)
    public static int ARMORY_CABINET_SIZE = 4096;

    @ConfigVal(comment = "Linking range radius")
    public static int DRAWER_CONTROLLER_LINKING_RANGE = 8;

    @ConfigVal(comment = "Every how many ticks the drawer upgrades will work")
    public static int UPGRADE_TICK = 4;

    @ConfigVal(comment = "How many items the pulling upgrade will try to pull")
    public static int UPGRADE_PULL_ITEMS = 4;

    @ConfigVal(comment = "How much fluid (in mb) the pulling upgrade will try to pull")
    public static int UPGRADE_PULL_FLUID = 500;

    @ConfigVal(comment = "How many items the pushing upgrade will try to pull")
    public static int UPGRADE_PUSH_ITEMS = 4;

    @ConfigVal(comment = "How much fluid (in mb) the pushing upgrade will try to pull")
    public static int UPGRADE_PUSH_FLUID = 500;

    @ConfigVal(comment = "How many items the collector upgrade will try to pull")
    public static int UPGRADE_COLLECTOR_ITEMS = 4;

    @ConfigVal(comment = "How much the storage of an item drawer with a Copper Upgrade should be multiplied by")
    public static int COPPER_MULTIPLIER = 8;

    @ConfigVal(comment = "How much the storage of an item drawer with a Gold Upgrade should be multiplied by")
    public static int GOLD_MULTIPLIER = 16;

    @ConfigVal(comment = "How much the storage of an item drawer with a Diamond Upgrade should be multiplied by")
    public static int DIAMOND_MULTIPLIER = 24;

    @ConfigVal(comment = "How much the storage of an item drawer with a Netherite Upgrade should be multiplied by")
    public static int NETHERITE_MULTIPLIER = 32;

    @ConfigVal(comment = "How much should the fluid storage be divided by for any given Storage Upgrade")
    public static int FLUID_DIVISOR = 2;

    @ConfigVal(comment = "How much should the range be divided by for any given Storage Upgrade")
    public static int RANGE_DIVISOR = 4;
}
