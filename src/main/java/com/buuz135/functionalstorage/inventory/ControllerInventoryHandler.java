package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class HandlerSlotSelector {
    IItemHandler handler;
    int slot;

    public HandlerSlotSelector(IItemHandler handler, int slot) {
        this.handler = handler;
        this.slot = slot;
    }

    public ItemStack getStackInSlot() {
        return handler.getStackInSlot(slot);
    }

    public ItemStack insertItem(@NotNull ItemStack stack, boolean simulate) {
        return handler.insertItem(slot, stack, simulate);
    }

    public ItemStack extractItem(int amount, boolean simulate) {
        return handler.extractItem(slot, amount, simulate);
    }

    public int getSlotLimit() {
        return handler.getSlotLimit(slot);
    }

    public boolean isItemValid(@NotNull ItemStack stack) {
        return handler.isItemValid(slot, stack);
    }
}

public abstract class ControllerInventoryHandler implements IItemHandler {

    HandlerSlotSelector[] selectors;
    private int slots = 0;

    public ControllerInventoryHandler() {
        invalidateSlots();
    }

    @Override
    public int getSlots() {
        return slots;
    }

    public void invalidateSlots() {
        List<HandlerSlotSelector> selectors = new ArrayList<HandlerSlotSelector>();
        this.slots = 0;
        for (IItemHandler handler : getDrawers().getHandlers()) {
            if (handler instanceof ControllerInventoryHandler) continue;
            int handlerSlots = handler.getSlots();
            for (int i = 0; i < handlerSlots; ++i) {
                selectors.add(new HandlerSlotSelector(handler, i));
            }
            this.slots += handlerSlots;
        }
        this.selectors = selectors.toArray(new HandlerSlotSelector[selectors.size()]);
    }

    private HandlerSlotSelector selectorForSlot(int slot) {
        return slot >= 0 && slot < selectors.length ? selectors[slot] : null;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        HandlerSlotSelector selector = selectorForSlot(slot);
        return null != selector ? selector.getStackInSlot() : ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        HandlerSlotSelector selector = selectorForSlot(slot);
        return null != selector ? selector.insertItem(stack, simulate) : ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        HandlerSlotSelector selector = selectorForSlot(slot);
        return null != selector ? selector.extractItem(amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        HandlerSlotSelector selector = selectorForSlot(slot);
        return null != selector ? selector.getSlotLimit() : 0;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        HandlerSlotSelector selector = selectorForSlot(slot);
        return null != selector ? selector.isItemValid(stack) : false;
    }

    public abstract DrawerControllerTile.ConnectedDrawers getDrawers();
}
