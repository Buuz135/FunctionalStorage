package com.buuz135.functionalstorage.client;

import com.hrznstudio.titanium.annotation.config.ConfigFile;
import com.hrznstudio.titanium.annotation.config.ConfigVal;

@ConfigFile(value = "functionalstorage-client")
public class FunctionalStorageClientConfig {

    @ConfigVal(comment = "Drawer content render range in blocks")
    @ConfigVal.InRangeInt(min = 1)
    public static int DRAWER_RENDER_RANGE = 16;

    @ConfigVal(comment = "The thickness of 3D item/block displays")
    @ConfigVal.InRangeDouble(min = 0.05, max = 0.75)
    public static double DRAWER_RENDER_THICKNESS = 0.125;
}
