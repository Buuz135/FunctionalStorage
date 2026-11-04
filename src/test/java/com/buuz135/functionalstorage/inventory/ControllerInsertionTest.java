package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.util.CompactingUtil;
import com.buuz135.functionalstorage.util.ConnectedDrawers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
class ControllerInsertionTest {
    ControllerInsertionTest(MinecraftServer server) {}

    private static class Drawer extends BigInventoryHandler {
        boolean creative, locked, voiding;
        int changes;
        Drawer() { super(FunctionalStorage.DrawerType.X_4); }
        void set(int slot, ItemResource resource, int amount) {
            getStoredStacks().get(slot).setResource(resource);
            getStoredStacks().get(slot).setAmount(amount);
        }
        @Override public void onChange() { changes++; }
        @Override public float getMultiplier() { return 1; }
        @Override public boolean isVoid() { return voiding; }
        @Override public boolean isLocked() { return locked; }
        @Override public boolean isCreative() { return creative; }
    }

    private static ControllerInventoryHandler controller(ConnectedDrawers drawers, boolean reference) {
        if (reference) return new ControllerInventoryHandler() {
            @Override public ConnectedDrawers getDrawers() { return drawers; }
            // A custom resource view deliberately retains ResourceHandler's
            // original slot-by-slot algorithm for the differential reference.
            @Override public ItemResource getResource(int index) { return super.getResource(index); }
        };
        return new ControllerInventoryHandler() {
            @Override public ConnectedDrawers getDrawers() { return drawers; }
        };
    }

    private static ControllerInventoryHandler controller(Drawer... handlers) {
        var drawers = new ConnectedDrawers(null, null);
        drawers.getItemHandlers().addAll(List.of(handlers));
        return controller(drawers, false);
    }

    @Test
    void preservesEmptyBeforeMatchingSlotOrderAndPartialFill() {
        var drawer = new Drawer();
        var wanted = ItemResource.of(Items.DIAMOND);
        drawer.set(1, wanted, 50);
        var controller = controller(drawer);
        try (var tx = Transaction.openRoot()) {
            assertEquals(150, controller.insert(wanted, 150, tx));
            assertEquals(64, drawer.getAmountAsLong(0));
            assertEquals(64, drawer.getAmountAsLong(1));
            assertEquals(64, drawer.getAmountAsLong(2));
            assertEquals(8, drawer.getAmountAsLong(3));
            tx.commit();
        }
        assertEquals(1, drawer.changes);
    }

    @Test
    void rollbackClearsNewResourceAndNestedCommitStillRollsBackWithParent() {
        var drawer = new Drawer();
        var wanted = ItemResource.of(Items.DIAMOND);
        var controller = controller(drawer);
        try (var root = Transaction.openRoot()) {
            try (var child = Transaction.open(root)) {
                assertEquals(90, controller.insert(wanted, 90, child));
                child.commit();
            }
            assertEquals(90, controller.extract(wanted, 90, root));
            assertEquals(12, controller.insert(wanted, 12, root));
        }
        assertTrue(drawer.getResource(0).isEmpty());
        assertEquals(0, drawer.changes);
        try (var tx = Transaction.openRoot()) {
            assertEquals(5, controller.insert(ItemResource.of(Items.STONE), 5, tx));
            tx.commit();
        }
        assertEquals(ItemResource.of(Items.STONE), drawer.getResource(0));
    }

    @Test
    void readsLiveCreativeAndLockedFlagsWithoutResourceChanges() {
        var drawer = new Drawer();
        drawer.locked = true;
        var wanted = ItemResource.of(Items.DIAMOND);
        drawer.set(3, wanted, 64);
        var controller = controller(drawer);
        try (var tx = Transaction.openRoot()) {
            assertEquals(0, controller.insert(wanted, 10000, tx));
            drawer.creative = true;
            assertEquals(10000, controller.insert(wanted, 10000, tx));
            drawer.creative = false;
            assertEquals(0, controller.insert(wanted, 10000, tx));
            drawer.locked = false;
            assertEquals(5, controller.insert(wanted, 5, tx));
        }
        assertTrue(drawer.getResource(0).isEmpty());
        assertEquals(64, drawer.getStoredStacks().get(3).getAmount());
    }

    @Test
    void voidOverflowAndFullComponentsRetainNativeBehavior() {
        var drawer = new Drawer();
        drawer.voiding = true;
        drawer.locked = true;
        var namedStack = new ItemStack(Items.DIAMOND);
        namedStack.set(DataComponents.CUSTOM_NAME, Component.literal("Named"));
        var named = ItemResource.of(namedStack);
        drawer.set(2, named, 64);
        var controller = controller(drawer);
        try (var tx = Transaction.openRoot()) {
            assertEquals(0, controller.insert(ItemResource.of(Items.DIAMOND), 5, tx));
            assertEquals(10000, controller.insert(named, 10000, tx));
            tx.commit();
        }
        assertEquals(64, drawer.getStoredStacks().get(2).getAmount());
        assertEquals(named, drawer.getResource(2));
    }

    @Test
    void customIndexedInsertionIsNeverSkippedOrDuplicated() {
        int[] calls = {0};
        var drawer = new Drawer() {
            @Override public int insert(int slot, ItemResource resource, int amount, TransactionContext tx) {
                calls[0]++;
                return slot == 0 ? amount : 0;
            }
        };
        drawer.set(0, ItemResource.of(Items.STONE), 64);
        var controller = controller(drawer);
        try (var tx = Transaction.openRoot()) {
            assertEquals(7, controller.insert(ItemResource.of(Items.DIAMOND), 7, tx));
        }
        assertEquals(1, calls[0]);
    }

    @Test
    void customSizeCreativeDrawerRetainsHiddenResourceBehavior() {
        var drawer = new Drawer() {
            @Override public int size() { return 2; }
        };
        drawer.creative = true;
        drawer.set(0, ItemResource.of(Items.STONE), 64);
        drawer.set(1, ItemResource.of(Items.STONE), 64);
        drawer.set(3, ItemResource.of(Items.DIAMOND), 64);
        var controller = controller(drawer);
        try (var tx = Transaction.openRoot()) {
            assertEquals(10000, controller.insert(ItemResource.of(Items.DIAMOND), 10000, tx));
        }
        assertEquals(64, drawer.getStoredStacks().get(3).getAmount());
    }

    @Test
    void nonstandardStackListFallsBackAfterCacheWarmup() {
        var drawer = new Drawer();
        drawer.creative = true;
        for (int slot = 0; slot < 4; slot++) drawer.set(slot, ItemResource.of(Items.STONE), 64);
        var controller = controller(drawer);
        try (var tx = Transaction.openRoot()) {
            assertEquals(0, controller.insert(ItemResource.of(Items.DIAMOND), 10, tx));
        }
        drawer.getStoredStacks().add(new BigInventoryHandler.BigStack(new ItemStack(Items.DIAMOND), 64));
        try (var tx = Transaction.openRoot()) {
            assertEquals(10, controller.insert(ItemResource.of(Items.DIAMOND), 10, tx));
        }
    }

    @Test
    void nativeMoveConservesItemsAcrossAbortCommitAndReopen() {
        var source = new ItemStacksResourceHandler(2);
        var wanted = ItemResource.of(Items.DIAMOND);
        var drawer = new Drawer();
        var destination = controller(drawer);
        try (var tx = Transaction.openRoot()) {
            assertEquals(64, source.insert(0, wanted, 64, tx));
            tx.commit();
        }
        try (var tx = Transaction.openRoot()) {
            assertEquals(50, ResourceHandlerUtil.move(source, destination, wanted::equals, 50, tx));
        }
        assertEquals(64, source.getAmountAsLong(0));
        assertTrue(drawer.getResource(0).isEmpty());
        try (var tx = Transaction.openRoot()) {
            assertEquals(50, ResourceHandlerUtil.move(source, destination, wanted::equals, 50, tx));
            tx.commit();
        }
        assertEquals(14, source.getAmountAsLong(0));
        assertEquals(50, drawer.getAmountAsLong(0));
        destination.invalidateSlots();
        try (var tx = Transaction.openRoot()) {
            assertEquals(14, ResourceHandlerUtil.move(source, destination, wanted::equals, 50, tx));
            tx.commit();
        }
        assertEquals(0, source.getAmountAsLong(0));
        assertEquals(64, drawer.getAmountAsLong(0));
    }

    @Test
    void bulkInsertionRejectsInvalidRequestsEvenForEmptyControllers() {
        var controller = controller();
        assertThrows(IllegalArgumentException.class, () -> controller.insert(ItemResource.EMPTY, 1, null));
        assertThrows(IllegalArgumentException.class, () -> controller.insert(ItemResource.of(Items.STONE), -1, null));
        assertEquals(0, controller.insert(ItemResource.of(Items.STONE), 0, null));
    }

    @Test
    void mixedNetworkMatchesOriginalBulkAlgorithmThroughRandomizedTransactions() {
        var optimized = network(false);
        var reference = network(true);
        var named = new ItemStack(Items.DIAMOND);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Named"));
        ItemResource[] resources = {ItemResource.of(Items.DIAMOND), ItemResource.of(Items.STONE),
                ItemResource.of(named), ItemResource.of(Items.IRON_INGOT), ItemResource.of(Items.IRON_NUGGET)};
        var random = new Random(0xAEE2F5L);
        for (int step = 0; step < 2000; step++) {
            if (step % 11 == 0) {
                int index = random.nextInt(optimized.drawers.size());
                boolean creative = random.nextBoolean(), locked = random.nextBoolean();
                optimized.drawers.get(index).creative = reference.drawers.get(index).creative = creative;
                optimized.drawers.get(index).locked = reference.drawers.get(index).locked = locked;
            }
            try (var root = Transaction.openRoot()) {
                operation(random, resources, optimized.controller, reference.controller, root);
                try (var child = Transaction.open(root)) {
                    operation(random, resources, optimized.controller, reference.controller, child);
                    sameState(optimized, reference);
                    if (random.nextBoolean()) child.commit();
                }
                sameState(optimized, reference);
                if (random.nextBoolean()) root.commit();
            }
            sameState(optimized, reference);
            if (step % 17 == 0) {
                optimized.controller.invalidateSlots();
                reference.controller.invalidateSlots();
            }
        }
    }

    private static void operation(Random random, ItemResource[] resources, ControllerInventoryHandler actual,
            ControllerInventoryHandler expected, TransactionContext tx) {
        var resource = resources[random.nextInt(resources.length)];
        int amount = random.nextInt(150);
        if (random.nextBoolean()) {
            assertEquals(expected.insert(resource, amount, tx), actual.insert(resource, amount, tx));
        } else {
            assertEquals(expected.extract(resource, amount, tx), actual.extract(resource, amount, tx));
        }
    }

    private static Network network(boolean reference) {
        var connected = new ConnectedDrawers(null, null);
        var drawers = new ArrayList<Drawer>();
        for (int index = 0; index < 6; index++) {
            var drawer = new Drawer();
            drawer.voiding = index == 5;
            drawer.set(1, ItemResource.of(Items.DIAMOND), 50);
            drawer.set(3, ItemResource.of(Items.STONE), 63);
            drawers.add(drawer);
            connected.getItemHandlers().add(drawer);
            if (index % 2 == 0) connected.getItemHandlers().add(new ItemStacksResourceHandler(2));
        }
        var compacting = new CompactingInventoryHandler(3) {
            @Override public void onChange() {}
            @Override public float getMultiplier() { return 1; }
            @Override public boolean isVoid() { return false; }
            @Override public boolean isLocked() { return true; }
            @Override public boolean isCreative() { return false; }
        };
        compacting.setupWithRearrangedResults(new ArrayList<>(List.of(
                new CompactingUtil.Result(new ItemStack(Items.IRON_NUGGET), 1),
                new CompactingUtil.Result(new ItemStack(Items.IRON_INGOT), 9),
                new CompactingUtil.Result(new ItemStack(Items.IRON_BLOCK), 81))));
        connected.getItemHandlers().add(2, compacting);
        return new Network(controller(connected, reference), drawers);
    }

    private static void sameState(Network actual, Network expected) {
        assertEquals(expected.controller.size(), actual.controller.size());
        for (int slot = 0; slot < actual.controller.size(); slot++) {
            assertEquals(expected.controller.getResource(slot), actual.controller.getResource(slot));
            assertEquals(expected.controller.getAmountAsLong(slot), actual.controller.getAmountAsLong(slot));
        }
        for (int i = 0; i < actual.drawers.size(); i++) {
            assertEquals(expected.drawers.get(i).changes, actual.drawers.get(i).changes);
            for (int slot = 0; slot < 4; slot++) {
                assertEquals(expected.drawers.get(i).getStoredStacks().get(slot).getAmount(),
                        actual.drawers.get(i).getStoredStacks().get(slot).getAmount());
            }
        }
    }

    private record Network(ControllerInventoryHandler controller, List<Drawer> drawers) {}
}
