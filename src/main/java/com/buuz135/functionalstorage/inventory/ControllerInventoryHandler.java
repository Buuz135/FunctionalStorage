package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.util.ConnectedDrawers;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

public abstract class ControllerInventoryHandler implements ResourceHandler<ItemResource> {

    private HandlerIndex[] indices;

    public ControllerInventoryHandler() {
        invalidateSlots();
    }

    public void invalidateSlots() {
        List<HandlerIndex> rebuilt = new ArrayList<>();
        for (ResourceHandler<ItemResource> handler : getDrawers().getItemHandlers()) {
            if (handler instanceof ControllerInventoryHandler) continue;
            for (int index = 0; index < handler.size(); index++) rebuilt.add(new HandlerIndex(handler, index));
        }
        indices = rebuilt.toArray(HandlerIndex[]::new);
    }

    private HandlerIndex index(int index) {
        return index >= 0 && index < indices.length ? indices[index] : null;
    }

    @Override public int size() { return indices.length; }

    @Override
    public ItemResource getResource(int index) {
        HandlerIndex selected = index(index);
        return selected == null ? ItemResource.EMPTY : selected.handler.getResource(selected.index);
    }

    @Override
    public long getAmountAsLong(int index) {
        HandlerIndex selected = index(index);
        return selected == null ? 0 : selected.handler.getAmountAsLong(selected.index);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        HandlerIndex selected = index(index);
        return selected == null ? 0 : selected.handler.getCapacityAsLong(selected.index, resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        HandlerIndex selected = index(index);
        return selected != null && selected.handler.isValid(selected.index, resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        HandlerIndex selected = index(index);
        return selected == null ? 0 : selected.handler.insert(selected.index, resource, amount, transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        HandlerIndex selected = index(index);
        return selected == null ? 0 : selected.handler.extract(selected.index, resource, amount, transaction);
    }

    public abstract ConnectedDrawers getDrawers();

    private record HandlerIndex(ResourceHandler<ItemResource> handler, int index) {}
}
