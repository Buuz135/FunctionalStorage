package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.util.ConnectedDrawers;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public abstract class ControllerInventoryHandler implements ResourceHandler<ItemResource> {

    private HandlerIndex[] indices;
    private VersionedHandler[] versionedHandlers;
    private int[] fallbackSlots;
    private Map<ItemResource, ResourceSlots> resourceSlots;
    private long indexGeneration;
    private static final int[] NO_SLOTS = new int[0];

    // Extensions with a different resource view must keep using live slot reads.
    private static final ClassValue<Boolean> CACHEABLE = new ClassValue<>() {
        @Override protected Boolean computeValue(Class<?> type) {
            try {
                Class<?> owner = type.getMethod("getResource", int.class).getDeclaringClass();
                if (owner == ControllerInventoryHandler.class
                        && type.getMethod("size").getDeclaringClass() != ControllerInventoryHandler.class) return false;
                return owner == BigInventoryHandler.class || owner == ControllerInventoryHandler.class;
            } catch (NoSuchMethodException e) {
                return false;
            }
        }
    };

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
        var handlers = new IdentityHashMap<BigInventoryHandler, List<Integer>>();
        var fallback = new ArrayList<Integer>();
        for (int slot = 0; slot < indices.length; slot++) {
            var handler = indices[slot].handler;
            if (CACHEABLE.get(getClass()) && handler instanceof BigInventoryHandler big
                    && CACHEABLE.get(handler.getClass()) && big.hasVersionedResources()) {
                handlers.computeIfAbsent(big, key -> new ArrayList<>()).add(slot);
            } else {
                fallback.add(slot);
            }
        }
        versionedHandlers = handlers.entrySet().stream().map(entry -> new VersionedHandler(entry.getKey(),
                entry.getValue().stream().mapToInt(Integer::intValue).toArray())).toArray(VersionedHandler[]::new);
        fallbackSlots = fallback.stream().mapToInt(Integer::intValue).toArray();
        resourceSlots = new HashMap<>();
        indexGeneration++;
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

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) return 0;
        if (!CACHEABLE.get(getClass())) return ResourceHandler.super.extract(resource, amount, transaction);
        refreshResourceSlots();
        var selected = resourceSlots.get(resource);
        int[] matching = selected == null ? NO_SLOTS : selected.snapshot();
        long generation = indexGeneration;
        int extracted = 0, match = 0, fallback = 0;
        // Merge in controller order; unversioned handlers are always read live.
        while (match < matching.length || fallback < fallbackSlots.length) {
            int slot;
            if (fallback == fallbackSlots.length
                    || match < matching.length && matching[match] < fallbackSlots[fallback]) {
                slot = matching[match++];
            } else {
                slot = fallbackSlots[fallback++];
            }
            extracted += extract(slot, resource, amount - extracted, transaction);
            if (extracted == amount) break;
            // An extension can change another slot during extraction. Once the
            // resource view changes, finish this request with live slot reads.
            if (resourcesChanged(generation)) {
                for (int remaining = slot + 1; remaining < size(); remaining++) {
                    extracted += extract(remaining, resource, amount - extracted, transaction);
                    if (extracted == amount) break;
                }
                break;
            }
        }
        return extracted;
    }

    private void refreshResourceSlots() {
        for (var tracked : versionedHandlers) {
            if (!tracked.handler.hasVersionedResources()) {
                invalidateSlots();
                refreshResourceSlots();
                return;
            }
            long current = tracked.handler.resourceVersion();
            if (tracked.version == current) continue;
            for (int i = 0; i < tracked.slots.length; i++) {
                int slot = tracked.slots[i];
                var previous = tracked.resources[i];
                var resource = getResource(slot);
                if (previous.equals(resource)) continue;
                indexGeneration++;
                if (!previous.isEmpty()) {
                    var old = resourceSlots.get(previous);
                    old.remove(slot);
                    if (old.slots.isEmpty()) resourceSlots.remove(previous);
                }
                if (!resource.isEmpty()) resourceSlots.computeIfAbsent(resource, key -> new ResourceSlots()).add(slot);
                tracked.resources[i] = resource;
            }
            tracked.version = current;
        }
    }

    private boolean resourcesChanged(long generation) {
        if (indexGeneration != generation) return true;
        for (var tracked : versionedHandlers) {
            if (tracked.version != tracked.handler.resourceVersion()) return true;
        }
        return false;
    }

    private static final class VersionedHandler {
        final BigInventoryHandler handler;
        final int[] slots;
        final ItemResource[] resources;
        long version = Long.MIN_VALUE;
        VersionedHandler(BigInventoryHandler handler, int[] slots) {
            this.handler = handler;
            this.slots = slots;
            resources = new ItemResource[slots.length];
            Arrays.fill(resources, ItemResource.EMPTY);
        }
    }

    private static final class ResourceSlots {
        final IntAVLTreeSet slots = new IntAVLTreeSet();
        private int[] snapshot;
        void add(int slot) { slots.add(slot); snapshot = null; }
        void remove(int slot) { slots.remove(slot); snapshot = null; }
        int[] snapshot() {
            if (snapshot == null) snapshot = slots.toIntArray();
            return snapshot;
        }
    }

    public abstract ConnectedDrawers getDrawers();

    private record HandlerIndex(ResourceHandler<ItemResource> handler, int index) {}
}
