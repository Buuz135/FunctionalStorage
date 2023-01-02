package com.buuz135.functionalstorage.fluid;

import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import com.buuz135.functionalstorage.inventory.ControllerInventoryHandler;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class ControllerFluidHandler implements IFluidHandler {

    HandlerTankSelector[] selectors;
    private int tanks = 0;

    public ControllerFluidHandler() {
        invalidateSlots();
    }

    public void invalidateSlots() {
        List<HandlerTankSelector> selectors = new ArrayList<>();
        this.tanks = 0;
        for (IFluidHandler handler : getDrawers().getFluidHandlers()) {
            if (handler instanceof ControllerInventoryHandler) continue;
            int handlerTanks = handler.getTanks();
            for (int i = 0; i < handlerTanks; ++i) {
                selectors.add(new HandlerTankSelector(handler, i));
            }
            this.tanks += handlerTanks;
        }
        this.selectors = selectors.toArray(new HandlerTankSelector[selectors.size()]);
    }

    private HandlerTankSelector selectorForTank(int tank) {
        return tank >= 0 && tank < selectors.length ? selectors[tank] : null;
    }

    @Override
    public int getTanks() {
        return tanks;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        HandlerTankSelector selector = selectorForTank(tank);
        return null != selector ? selector.getStackInSlot() : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        HandlerTankSelector selector = selectorForTank(tank);
        return null != selector ? selector.getCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        HandlerTankSelector selector = selectorForTank(tank);
        return null != selector && selector.isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        for (HandlerTankSelector selector : this.selectors) {
            if (!selector.getStackInSlot().isEmpty() && selector.getStackInSlot().isFluidEqual(resource)) {
                return selector.fill(resource, action);
            }
        }
        for (HandlerTankSelector selector : this.selectors) {
            if (selector.getStackInSlot().isEmpty()) {
                return selector.fill(resource, action);
            }
        }
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        for (HandlerTankSelector selector : this.selectors) {
            if (!selector.getStackInSlot().isEmpty() && selector.getStackInSlot().isFluidEqual(resource)) {
                return selector.drain(resource, action);
            }
        }
        for (HandlerTankSelector selector : this.selectors) {
            if (selector.getStackInSlot().isEmpty()) {
                return selector.drain(resource, action);
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        for (HandlerTankSelector selector : this.selectors) {
            if (!selector.getStackInSlot().isEmpty()) {
                return selector.drain(maxDrain, action);
            }
        }
        return FluidStack.EMPTY;
    }

    public abstract DrawerControllerTile.ConnectedDrawers getDrawers();
}

class HandlerTankSelector {
    IFluidHandler handler;
    int slot;

    public HandlerTankSelector(IFluidHandler handler, int slot) {
        this.handler = handler;
        this.slot = slot;
    }

    public FluidStack getStackInSlot() {
        return handler.getFluidInTank(slot);
    }

    public int fill(@NotNull FluidStack stack, IFluidHandler.FluidAction action) {
        return handler.fill(stack, action);
    }

    public FluidStack drain(int amount, IFluidHandler.FluidAction action) {
        return handler.drain(amount, action);
    }

    public FluidStack drain(FluidStack fluidStack, IFluidHandler.FluidAction action) {
        return handler.drain(fluidStack, action);
    }

    public int getCapacity() {
        return handler.getTankCapacity(slot);
    }

    public boolean isFluidValid(@NotNull FluidStack stack) {
        return handler.isFluidValid(slot, stack);
    }
}
