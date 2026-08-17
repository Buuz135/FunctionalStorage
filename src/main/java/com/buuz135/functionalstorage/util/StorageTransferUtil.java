package com.buuz135.functionalstorage.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class StorageTransferUtil {
    private StorageTransferUtil() {}

    public static ItemStack getStack(ResourceHandler<ItemResource> handler, int index) {
        return handler.getResource(index).toStack(handler.getAmountAsInt(index));
    }

    public static <T extends Resource> int insert(ResourceHandler<T> handler, int index, T resource, int amount, boolean commit) {
        if (resource.isEmpty() || amount <= 0) return 0;
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(index, resource, amount, transaction);
            if (commit) transaction.commit();
            return inserted;
        }
    }

    public static <T extends Resource> int extract(ResourceHandler<T> handler, int index, T resource, int amount, boolean commit) {
        if (resource.isEmpty() || amount <= 0) return 0;
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = handler.extract(index, resource, amount, transaction);
            if (commit) transaction.commit();
            return extracted;
        }
    }
}
