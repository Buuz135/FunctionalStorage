package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.block.tile.BasicTile;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Collection;

public abstract class Drawer<T extends BasicTile<T>> extends RotatableBlock<T> {
    public Drawer(String name, Properties properties, Class<T> tileClass) {
        super(name, properties, tileClass);
    }

    @Nullable
    public ControllableDrawerTile<?> getBlockEntityAt(BlockGetter level, BlockPos pos) {
        return TileUtil.getTileEntity(level, pos, ControllableDrawerTile.class).orElse(null);
    }

    public abstract Collection<VoxelShape> getHitShapes(BlockState state);
}
