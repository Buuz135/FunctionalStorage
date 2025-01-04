package com.buuz135.functionalstorage.item.component;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public record AndBehavior(List<FunctionalUpgradeBehavior> behaviors) implements FunctionalUpgradeBehavior {
    public static final MapCodec<AndBehavior> CODEC = FunctionalUpgradeBehavior.CODEC.listOf(1, Integer.MAX_VALUE)
            .fieldOf("behaviors")
            .xmap(AndBehavior::new, AndBehavior::behaviors);

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack, int upgradeSlot) {
        for (FunctionalUpgradeBehavior behavior : behaviors) {
            behavior.work(level, pos, drawer, upgradeStack, upgradeSlot);
        }
    }

    @Override
    public boolean canConnectRedstone(Level level, BlockPos blockPos, BlockState state, ControllableDrawerTile<?> drawer, Direction direction, ItemStack upgradeStack, int upgradeSlot) {
        for (FunctionalUpgradeBehavior behavior : behaviors) {
            if (behavior.canConnectRedstone(level, blockPos, state, drawer, direction, upgradeStack, upgradeSlot)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getRedstoneSignal(Level level, BlockPos blockPos, BlockState state, ControllableDrawerTile<?> drawer, Direction direction, ItemStack upgradeStack, int upgradeSlot) {
        for (FunctionalUpgradeBehavior behavior : behaviors) {
            int signal = behavior.getRedstoneSignal(level, blockPos, state, drawer, direction, upgradeStack, upgradeSlot);
            if (signal >= 0) {
                return signal;
            }
        }
        return -1;
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
