package com.buuz135.functionalstorage.util;

import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class DummyItemAccess implements ItemAccess {

    private final ItemResource resource;

    public DummyItemAccess(ItemResource resource) {
        this.resource = resource;
    }

    @Override
    public ItemResource getResource() {
        return resource;
    }

    @Override
    public int getAmount() {
        return 1;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }
}
