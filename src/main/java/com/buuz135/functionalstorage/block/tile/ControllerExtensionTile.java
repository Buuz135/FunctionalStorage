package com.buuz135.functionalstorage.block.tile;

import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ControllerExtensionTile extends StorageControllerExtensionTile<ControllerExtensionTile> {
    public ControllerExtensionTile(BasicTileBlock<ControllerExtensionTile> base, BlockEntityType<ControllerExtensionTile> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state);
    }

    @NotNull
    @Override
    public ControllerExtensionTile getSelf() {
        return this;
    }
}
