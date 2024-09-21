package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.hrznstudio.titanium.item.BasicItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class FunctionalUpgradeItem extends BasicItem {

    public FunctionalUpgradeItem(Properties properties) {
        super(properties);
        setItemGroup(FunctionalStorage.TAB);
    }

    public void work(Level level, BlockPos pos) {

    }
}
