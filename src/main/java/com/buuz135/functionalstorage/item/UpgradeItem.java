package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.hrznstudio.titanium.item.BasicItem;

public class UpgradeItem extends BasicItem {

    private final Type type;

    public UpgradeItem(Properties properties, Type type) {
        super(properties.tab(FunctionalStorage.TAB));
        this.type = type;
    }

    public static enum Type{
        STORAGE,
        UTILITY
    }
}
