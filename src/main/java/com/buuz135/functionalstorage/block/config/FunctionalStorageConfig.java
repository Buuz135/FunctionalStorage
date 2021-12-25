package com.buuz135.functionalstorage.block.config;

import com.hrznstudio.titanium.annotation.config.ConfigFile;
import com.hrznstudio.titanium.annotation.config.ConfigVal;

@ConfigFile(value = "functionalstorage-common")
public class FunctionalStorageConfig {

    @ConfigVal(comment = "Armory slot amount")
    @ConfigVal.InRangeInt(min = 1)
    public static int ARMORY_CABINET_SIZE = 4096;

    @ConfigVal(comment = "Linking range radius")
    public static int DRAWER_CONTROLLER_LINKING_RANGE = 12;

}
