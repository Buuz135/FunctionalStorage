package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.util.DrawerWoodType;
import com.buuz135.functionalstorage.util.IWoodType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.tuple.Pair;

public class FramedDrawerBlock extends DrawerBlock{

    public FramedDrawerBlock(FunctionalStorage.DrawerType type) {
        super(DrawerWoodType.FRAMED, type);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<DrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new DrawerTile(this, (BlockEntityType<DrawerTile>) FunctionalStorage.DRAWER_TYPES.get(this.getType()).stream().filter(registryObjectRegistryObjectPair -> registryObjectRegistryObjectPair.getLeft().get().equals(this)).map(Pair::getRight).findFirst().get().get(), blockPos, state, this.getType());
    }
}
