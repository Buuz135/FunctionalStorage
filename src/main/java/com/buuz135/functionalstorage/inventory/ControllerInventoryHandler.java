package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public abstract class ControllerInventoryHandler implements IItemHandler {

    public ControllerInventoryHandler() {

    }

    @Override
    public int getSlots() {
        return getDrawers().getHandlers().stream().map(IItemHandler::getSlots).mapToInt(Integer::intValue).sum();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        int index = 0;
        for (IItemHandler handler : getDrawers().getHandlers()) {
            int relativeIndex = slot - index;
            if (relativeIndex < handler.getSlots()){
                return handler.getStackInSlot(relativeIndex);
            }
            index += handler.getSlots();
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        int index = 0;
        for (IItemHandler handler : getDrawers().getHandlers()) {
            int relativeIndex = slot - index;
            if (relativeIndex < handler.getSlots()){
                return handler.insertItem(relativeIndex, stack, simulate);
            }
            index += handler.getSlots();
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        int index = 0;
        for (IItemHandler handler : getDrawers().getHandlers()) {
            int relativeIndex = slot - index;
            if (relativeIndex < handler.getSlots()){
                return handler.extractItem(relativeIndex, amount, simulate);
            }
            index += handler.getSlots();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        int index = 0;
        for (IItemHandler handler : getDrawers().getHandlers()) {
            int relativeIndex = slot - index;
            if (relativeIndex < handler.getSlots()){
                return handler.getSlotLimit(relativeIndex);
            }
            index += handler.getSlots();
        }
        return 0;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        int index = 0;
        for (IItemHandler handler : getDrawers().getHandlers()) {
            int relativeIndex = slot - index;
            if (relativeIndex < handler.getSlots()){
                return handler.isItemValid(relativeIndex, stack);
            }
            index += handler.getSlots();
        }
        return false;
    }

    public abstract DrawerControllerTile.ConnectedDrawers getDrawers();
}
