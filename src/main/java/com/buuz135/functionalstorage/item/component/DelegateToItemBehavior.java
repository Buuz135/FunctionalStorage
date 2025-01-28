package com.buuz135.functionalstorage.item.component;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.item.FunctionalUpgradeItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record DelegateToItemBehavior() implements FunctionalUpgradeBehavior {
    public static final DelegateToItemBehavior INSTANCE = new DelegateToItemBehavior();
    public static final MapCodec<DelegateToItemBehavior> CODEC = MapCodec.unit(INSTANCE);
    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack, int upgradeSlot) {
        if (upgradeStack.getItem() instanceof FunctionalUpgradeItem fui) {
            fui.work(level, pos, upgradeStack);
        }
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
