package com.buuz135.functionalstorage.item;

import com.buuz135.functionalstorage.item.component.DelegateToItemBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * @deprecated Use a custom item with the {@link FSAttachments#FUNCTIONAL_BEHAVIOR} component.
 * TODO - remove next lifecycle, addons are currently using it in 1.21.1
 */
@Deprecated(forRemoval = true)
public class FunctionalUpgradeItem extends FSItem {

    public FunctionalUpgradeItem(Properties properties) {
        super(properties.component(FSAttachments.FUNCTIONAL_BEHAVIOR, DelegateToItemBehavior.INSTANCE));
    }

    public void work(Level level, BlockPos pos, ItemStack upgradeStack) {

    }
}
