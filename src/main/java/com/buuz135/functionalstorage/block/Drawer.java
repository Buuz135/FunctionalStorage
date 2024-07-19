package com.buuz135.functionalstorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface Drawer {
    int getHit(BlockState state, Level worldIn, BlockPos pos, Player player);
}
