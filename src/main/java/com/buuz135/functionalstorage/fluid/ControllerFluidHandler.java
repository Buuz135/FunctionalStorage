package com.buuz135.functionalstorage.fluid;

import com.buuz135.functionalstorage.util.ConnectedDrawers;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

public abstract class ControllerFluidHandler implements ResourceHandler<FluidResource> {

    private HandlerIndex[] indices;

    public ControllerFluidHandler() {
        invalidateSlots();
    }

    public void invalidateSlots() {
        List<HandlerIndex> rebuilt = new ArrayList<>();
        for (ResourceHandler<FluidResource> handler : getDrawers().getFluidHandlers()) {
            if (handler instanceof ControllerFluidHandler) continue;
            for (int index = 0; index < handler.size(); index++) rebuilt.add(new HandlerIndex(handler, index));
        }
        indices = rebuilt.toArray(HandlerIndex[]::new);
    }

    private HandlerIndex index(int index) {
        return index >= 0 && index < indices.length ? indices[index] : null;
    }

    @Override public int size() { return indices.length; }

    @Override
    public FluidResource getResource(int index) {
        HandlerIndex selected = index(index);
        return selected == null ? FluidResource.EMPTY : selected.handler.getResource(selected.index);
    }

    @Override
    public long getAmountAsLong(int index) {
        HandlerIndex selected = index(index);
        return selected == null ? 0 : selected.handler.getAmountAsLong(selected.index);
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        HandlerIndex selected = index(index);
        return selected == null ? 0 : selected.handler.getCapacityAsLong(selected.index, resource);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        HandlerIndex selected = index(index);
        return selected != null && selected.handler.isValid(selected.index, resource);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        HandlerIndex selected = index(index);
        return selected == null ? 0 : selected.handler.insert(selected.index, resource, amount, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        HandlerIndex selected = index(index);
        return selected == null ? 0 : selected.handler.extract(selected.index, resource, amount, transaction);
    }

    public abstract ConnectedDrawers getDrawers();

    private record HandlerIndex(ResourceHandler<FluidResource> handler, int index) {}
}
