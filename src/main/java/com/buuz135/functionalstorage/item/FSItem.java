package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.hrznstudio.titanium.item.BasicItem;
import com.hrznstudio.titanium.module.DeferredRegistryHelper;

public class FSItem extends BasicItem {
    public FSItem(Properties properties) {
        super(DeferredRegistryHelper.applyItemRegistrationId(properties));
        setItemGroup(FunctionalStorage.TAB);
    }
}
