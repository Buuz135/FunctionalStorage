package com.buuz135.functionalstorage.item.component;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public record EmitRedstoneBehavior() implements FunctionalUpgradeBehavior {
    public static final EmitRedstoneBehavior INSTANCE = new EmitRedstoneBehavior();
    public static final MapCodec<EmitRedstoneBehavior> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack, int upgradeSlot) {
        if (level.getGameTime() % 20 == 0) {
            level.updateNeighborsAt(pos, drawer.getBasicTileBlock());
        }
    }

    @Override
    public boolean canConnectRedstone(Level level, BlockPos blockPos, BlockState state, ControllableDrawerTile<?> drawer, Direction direction, ItemStack upgradeStack, int upgradeSlot) {
        return drawer instanceof ItemControllableDrawerTile<?> || drawer instanceof FluidDrawerTile;
    }

    @Override
    public int getRedstoneSignal(Level level, BlockPos blockPos, BlockState state, ControllableDrawerTile<?> drawer, Direction direction, ItemStack upgradeStack, int upgradeSlot) {
        return switch (drawer) {
            case ItemControllableDrawerTile<?> itemDrawer -> {
                int redstoneSlot = upgradeStack.getOrDefault(FSAttachments.SLOT, 0);
                if (redstoneSlot < itemDrawer.getStorage().getSlots()) {
                    int amount = (int) Math.floor(itemDrawer.getStorage().getStackInSlot(redstoneSlot).getCount() * 14f / itemDrawer.getStorage().getSlotLimit(redstoneSlot));
                    yield amount + (amount > 0 ? 1 : 0);
                }

                yield -1;
            }

            case FluidDrawerTile fluidDrawer -> {
                int redstoneSlot = upgradeStack.getOrDefault(FSAttachments.SLOT, 0);
                if (redstoneSlot < fluidDrawer.getFluidHandler().getTanks()) {
                    yield (int) Math.floor(fluidDrawer.getFluidHandler().getFluidInTank(redstoneSlot).getAmount() * 15f / fluidDrawer.getFluidHandler().getTankCapacity(redstoneSlot));
                }

                yield -1;
            }

            default -> -1;
        };
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
