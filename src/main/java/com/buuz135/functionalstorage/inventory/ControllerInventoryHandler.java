package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.util.ConnectedDrawers;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
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
    private int[] insertFallbackSlots;
    private boolean[] indexedInsertion;
    private Map<ItemResource, ResourceSlots> resourceSlots;
    private long indexGeneration;
    private long observedResourceGeneration = Long.MIN_VALUE;
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

    private static final ClassValue<Boolean> NATIVE_INSERT = new ClassValue<>() {
        @Override protected Boolean computeValue(Class<?> type) {
            try {
                Class<?> owner = type.getMethod("insert", int.class, ItemResource.class, int.class,
                        TransactionContext.class).getDeclaringClass();
                if (owner == BigInventoryHandler.class
                        && type.getMethod("size").getDeclaringClass() != BigInventoryHandler.class) return false;
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
        var insertionFallback = new IntArrayList(fallbackSlots);
        indexedInsertion = new boolean[indices.length];
        for (var handler : versionedHandlers) {
            if (!handler.nativeInsert) for (int slot : handler.slots) insertionFallback.add(slot);
            if (handler.nativeInsert) for (int slot : handler.slots) indexedInsertion[slot] = true;
        }
        insertFallbackSlots = insertionFallback.toIntArray();
        Arrays.sort(insertFallbackSlots);
        resourceSlots = new HashMap<>();
        observedResourceGeneration = Long.MIN_VALUE;
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
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) return 0;
        if (!CACHEABLE.get(getClass()) || !NATIVE_INSERT.get(getClass())) {
            return ResourceHandler.super.insert(resource, amount, transaction);
        }
        refreshResourceSlots();
        int[] matching = slotsFor(resource);
        int[] empty = slotsFor(ItemResource.EMPTY);
        int[] fallback = insertFallbackSlots;
        long generation = indexGeneration;
        long resources = BigInventoryHandler.resourceGeneration();
        int inserted = 0, match = 0, vacancy = 0, other = 0;
        // Slots of each ordinary drawer are contiguous. A native creative drawer
        // still consumes before the next drawer through its matching/empty slot;
        // custom views and insert implementations retain all their original slots.
        while (match < matching.length || vacancy < empty.length || other < fallback.length) {
            int slot = Math.min(match < matching.length ? matching[match] : Integer.MAX_VALUE,
                    Math.min(vacancy < empty.length ? empty[vacancy] : Integer.MAX_VALUE,
                            other < fallback.length ? fallback[other] : Integer.MAX_VALUE));
            boolean emptySlot = vacancy < empty.length && empty[vacancy] == slot;
            if (match < matching.length && matching[match] == slot) match++;
            if (vacancy < empty.length && empty[vacancy] == slot) vacancy++;
            if (other < fallback.length && fallback[other] == slot) other++;
            if (emptySlot && indexedInsertion[slot]) {
                var selectedSlot = indices[slot];
                var handler = (BigInventoryHandler) selectedSlot.handler;
                // Ordinary empty locked slots always reject. Keep native creative
                // sinks and the synthetic void slot in the ordered candidate set.
                if (handler.isLocked() && !handler.isCreative()
                        && selectedSlot.index < handler.getStoredStacks().size()) continue;
            }
            inserted += insert(slot, resource, amount - inserted, transaction);
            if (inserted == amount) break;
            if (resourcesChanged(generation, resources)) {
                for (int remaining = slot + 1; remaining < size(); remaining++) {
                    inserted += insert(remaining, resource, amount - inserted, transaction);
                    if (inserted == amount) break;
                }
                break;
            }
        }
        return inserted;
    }

    private int[] slotsFor(ItemResource resource) {
        var slots = resourceSlots.get(resource);
        return slots == null ? NO_SLOTS : slots.snapshot();
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
        long resources = BigInventoryHandler.resourceGeneration();
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
            if (resourcesChanged(generation, resources)) {
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
        long generation = BigInventoryHandler.resourceGeneration();
        if (observedResourceGeneration == generation) return;
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
                if (resource.equals(previous)) continue;
                indexGeneration++;
                if (previous != null) {
                    var old = resourceSlots.get(previous);
                    old.remove(slot);
                    if (old.slots.isEmpty()) resourceSlots.remove(previous);
                }
                resourceSlots.computeIfAbsent(resource, key -> new ResourceSlots()).add(slot);
                tracked.resources[i] = resource;
            }
            tracked.version = current;
        }
        observedResourceGeneration = generation;
    }

    private boolean resourcesChanged(long generation, long resources) {
        return indexGeneration != generation || resources != BigInventoryHandler.resourceGeneration();
    }

    private static final class VersionedHandler {
        final BigInventoryHandler handler;
        final int[] slots;
        final ItemResource[] resources;
        final boolean nativeInsert;
        long version = Long.MIN_VALUE;
        VersionedHandler(BigInventoryHandler handler, int[] slots) {
            this.handler = handler;
            this.slots = slots;
            resources = new ItemResource[slots.length];
            nativeInsert = NATIVE_INSERT.get(handler.getClass());
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
