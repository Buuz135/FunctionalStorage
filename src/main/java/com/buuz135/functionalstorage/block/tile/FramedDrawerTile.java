package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FramedDrawerTile extends DrawerTile{

    public FramedDrawerTile(BasicTileBlock<DrawerTile> base, BlockEntityType<DrawerTile> blockEntityType, BlockPos pos, BlockState state, FunctionalStorage.DrawerType type) {
        super(base, blockEntityType, pos, state, type);
    }
}
