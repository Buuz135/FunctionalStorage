package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.hrznstudio.titanium.item.BasicItem;

public class FSItem extends BasicItem {
    public FSItem(Properties properties) {
        super(properties);
        setItemGroup(FunctionalStorage.TAB);
    }
}
