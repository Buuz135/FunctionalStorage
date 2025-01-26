package com.buuz135.functionalstorage.item.component;

import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public record CollectItemEntitiesBehavior() implements FunctionalUpgradeBehavior {
    public static final CollectItemEntitiesBehavior INSTANCE = new CollectItemEntitiesBehavior();
    public static final MapCodec<CollectItemEntitiesBehavior> CODEC = MapCodec.unit(INSTANCE);
    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> dr, ItemStack upgradeStack, int upgradeSlot) {
        if (!(dr instanceof ItemControllableDrawerTile<?> drawer)) return;

        Direction direction = UpgradeItem.getDirection(upgradeStack);
        AABB box = new AABB(pos.relative(direction));
        for (ItemEntity entitiesOfClass : level.getEntitiesOfClass(ItemEntity.class, box)) {
            ItemStack pulledStack = entitiesOfClass.getItem().copyWithCount(Math.min(entitiesOfClass.getItem().getCount(), FunctionalStorageConfig.UPGRADE_COLLECTOR_ITEMS));
            if (pulledStack.isEmpty()) continue;
            for (int ourSlot = 0; ourSlot < drawer.getStorage().getSlots(); ourSlot++) {
                ItemStack simulated = drawer.getStorage().insertItem(ourSlot, pulledStack, true);
                if (simulated.getCount() != pulledStack.getCount()) {
                    drawer.getStorage().insertItem(ourSlot, entitiesOfClass.getItem().copyWithCount(pulledStack.getCount() - simulated.getCount()), false);
                    entitiesOfClass.getItem().shrink(pulledStack.getCount() - simulated.getCount());
                    return;
                }
            }
        }
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
