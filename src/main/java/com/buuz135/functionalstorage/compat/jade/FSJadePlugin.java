package com.buuz135.functionalstorage.compat.jade;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.ArmoryCabinetBlock;
import com.buuz135.functionalstorage.block.tile.ArmoryCabinetTile;
import mcp.mobius.waila.api.*;

@WailaPlugin(value = FunctionalStorage.MOD_ID)
public class FSJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerComponentProvider(ArmoryComponentProvider.INSTANCE, TooltipPosition.HEAD, ArmoryCabinetBlock.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ArmoryComponentProvider.INSTANCE, ArmoryCabinetTile.class);
    }
}