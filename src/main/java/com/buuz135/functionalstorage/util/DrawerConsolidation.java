package com.buuz135.functionalstorage.util;

import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongFunction;

/** A bounded server-thread job. Only positions and resource identities survive a tick. */
public final class DrawerConsolidation {
    private static final long LOCAL_NANOS = 500_000;
    private static final long SERVER_NANOS = 2_000_000;
    private static final int LOCAL_STEPS = 1024;
    private static final int SERVER_STEPS = 4096;
    public record Result(long moved, int freed) {}
    public record Progress(int checked, int drawers, int processedTypes, int types) {}
    private record Slot(long position, int index) {}

    /** Shared by every controller on a server; the time limit is checked between steps. */
    public static final class Budget {
        private long tick = Long.MIN_VALUE;
        private int steps;
        private long nanos;

        public void reset(long tick) {
            if (this.tick != tick) {
                this.tick = tick;
                steps = 0;
                nanos = 0;
            }
        }
    }

    private static final class Slots {
        final List<Slot> locked = new ArrayList<>();
        final List<Slot> unlocked = new ArrayList<>();
        int size() { return locked.size() + unlocked.size(); }
        Slot get(int index) { return index < locked.size() ? locked.get(index) : unlocked.get(index - locked.size()); }
    }

    private final List<Long> positions;
    private final LongFunction<BigInventoryHandler> resolve;
    private final Set<Long> seen = new HashSet<>();
    private final Map<ItemResource, Slots> groups = new LinkedHashMap<>();
    private final Set<Slot> freed = new HashSet<>();
    private Iterator<Map.Entry<ItemResource, Slots>> remaining;
    private Map.Entry<ItemResource, Slots> group;
    private int scanIndex;
    private int targetIndex;
    private int sourceIndex;
    private int processedTypes;
    private boolean done;
    private long moved;

    public DrawerConsolidation(List<Long> positions, LongFunction<BigInventoryHandler> resolve) {
        this.positions = new ArrayList<>(positions);
        this.resolve = resolve;
    }

    public Result result() { return new Result(moved, freed.size()); }
    public Progress progress() { return new Progress(scanIndex, positions.size(), processedTypes, groups.size()); }

    public boolean tick(Budget budget) {
        long start = System.nanoTime();
        try {
            for (int local = 0; !done && local < LOCAL_STEPS && budget.steps < SERVER_STEPS
                    && System.nanoTime() - start < LOCAL_NANOS
                    && budget.nanos + System.nanoTime() - start < SERVER_NANOS; local++) {
                budget.steps++;
                step();
            }
        } finally {
            budget.nanos += System.nanoTime() - start;
        }
        return done;
    }

    private BigInventoryHandler drawer(long position) {
        var handler = resolve.apply(position);
        return handler == null || handler instanceof EnderInventoryHandler || handler.isCreative() ? null : handler;
    }

    private BigInventoryHandler drawer(Slot slot) {
        var handler = drawer(slot.position);
        return handler != null && slot.index < handler.getStoredStacks().size() ? handler : null;
    }

    private void step() {
        if (scanIndex < positions.size()) {
            long position = positions.get(scanIndex++);
            if (!seen.add(position)) return;
            var handler = drawer(position);
            if (handler == null) return;
            for (int index = 0; index < handler.getStoredStacks().size(); index++) {
                var resource = handler.getResource(index);
                if (resource.isEmpty()) continue;
                var slots = groups.computeIfAbsent(resource, key -> new Slots());
                (handler.isLocked() ? slots.locked : slots.unlocked).add(new Slot(position, index));
            }
            return;
        }
        if (remaining == null) remaining = groups.entrySet().iterator();
        if (group == null) {
            if (!remaining.hasNext()) { done = true; return; }
            group = remaining.next();
            targetIndex = 0;
            sourceIndex = group.getValue().size() - 1;
            return;
        }
        var slots = group.getValue();
        if (sourceIndex <= targetIndex) { group = null; processedTypes++; return; }
        var targetSlot = slots.get(targetIndex);
        var sourceSlot = slots.get(sourceIndex);
        var target = drawer(targetSlot);
        var source = drawer(sourceSlot);
        var resource = group.getKey();
        // Resolve live blocks and contents on every attempt: automation may have
        // extracted, inserted, changed types, locked, unlinked or replaced either block.
        if (target == null || !target.getResource(targetSlot.index).equals(resource)) { targetIndex++; return; }
        if (source == null || source.isLocked() || !source.getResource(sourceSlot.index).equals(resource)
                || source.getAmountAsLong(sourceSlot.index) <= 0) { sourceIndex--; return; }
        long targetBefore = target.getAmountAsLong(targetSlot.index);
        long sourceBefore = source.getAmountAsLong(sourceSlot.index);
        long free = target.getCapacityAsLong(targetSlot.index, resource) - targetBefore;
        if (free <= 0) { targetIndex++; return; }
        int requested = (int) Math.min(Integer.MAX_VALUE, Math.min(free, sourceBefore));
        // Never keep a transaction or a handler snapshot open across ticks.
        try (Transaction transfer = Transaction.openRoot()) {
            int extracted = source.extract(sourceSlot.index, resource, requested, transfer);
            int inserted = extracted <= 0 ? 0 : target.insert(targetSlot.index, resource, extracted, transfer);
            if (extracted <= 0 || inserted != extracted
                    || target.getAmountAsLong(targetSlot.index) - targetBefore != extracted
                    || sourceBefore - source.getAmountAsLong(sourceSlot.index) != extracted) { sourceIndex--; return; }
            boolean emptied = source.getAmountAsLong(sourceSlot.index) == 0;
            try {
                transfer.commit();
            } finally {
                // Root commit closes the transaction before notifying drawers.
                // A notification can throw or refill a donor after it was emptied.
                var lifecycle = Transaction.getLifecycle();
                if (lifecycle == Transaction.Lifecycle.NONE || lifecycle == Transaction.Lifecycle.ROOT_CLOSING) {
                    moved += extracted;
                    if (emptied) {
                        freed.add(sourceSlot);
                        sourceIndex--;
                    }
                }
            }
        }
    }
}
