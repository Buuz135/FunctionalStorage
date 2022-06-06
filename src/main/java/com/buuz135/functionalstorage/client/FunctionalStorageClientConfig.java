package com.buuz135.functionalstorage.client;

import com.hrznstudio.titanium.annotation.config.ConfigFile;
import com.hrznstudio.titanium.annotation.config.ConfigVal;

@ConfigFile(value = "functionalstorage-client")
public class FunctionalStorageClientConfig {

    @ConfigVal(comment = "Drawer content render range in blocks")
    @ConfigVal.InRangeInt(min = 1)
    public static int DRAWER_RENDER_RANGE = 16;
}
