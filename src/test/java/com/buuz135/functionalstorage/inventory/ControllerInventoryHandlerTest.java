package com.buuz135.functionalstorage.inventory;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.me.storage.ExternalStorageFacade;
import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.util.ConnectedDrawers;
import com.buuz135.functionalstorage.util.CompactingUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
class ControllerInventoryHandlerTest {
    private final MinecraftServer server;
    ControllerInventoryHandlerTest(MinecraftServer server) { this.server = server; }

    private static final class Drawer extends BigInventoryHandler {
        int extractionCalls;
        int changes;
        boolean creative;
        boolean locked;

        Drawer() { super(FunctionalStorage.DrawerType.X_4); }
        void set(int slot, ItemResource resource, int amount) {
            getStoredStacks().get(slot).setResource(resource);
            getStoredStacks().get(slot).setAmount(amount);
        }
        @Override public void onChange() { changes++; }
        @Override public float getMultiplier() { return 1024; }
        @Override public boolean isVoid() { return false; }
        @Override public boolean isLocked() { return locked; }
        @Override public boolean isCreative() { return creative; }
        @Override public int extract(int index, ItemResource resource, int amount,
                TransactionContext transaction) {
            extractionCalls++;
            return super.extract(index, resource, amount, transaction);
        }
    }

    private static ConnectedDrawers drawers(Drawer... handlers) {
        var drawers = new ConnectedDrawers(null, null);
        drawers.getItemHandlers().addAll(List.of(handlers));
        return drawers;
    }

    private static ControllerInventoryHandler controller(ConnectedDrawers drawers) {
        return new ControllerInventoryHandler() {
            @Override public ConnectedDrawers getDrawers() { return drawers; }
        };
    }

    @Test
    void skipsUnrelatedSlotsInLargeController() {
        var handlers = new ArrayList<Drawer>();
        var stone = ItemResource.of(Items.STONE);
        var diamond = ItemResource.of(Items.DIAMOND);
        for (int i = 0; i < 512; i++) {
            var drawer = new Drawer();
            for (int slot = 0; slot < 4; slot++) drawer.set(slot, stone, 100);
            handlers.add(drawer);
        }
        handlers.getLast().set(3, diamond, 100);
        var controller = controller(drawers(handlers.toArray(Drawer[]::new)));
        try (var tx = Transaction.openRoot()) {
            assertEquals(64, controller.extract(diamond, 64, tx));
        }
        assertEquals(1, handlers.stream().mapToInt(h -> h.extractionCalls).sum(),
                "only the matching drawer slot should receive an extraction probe");
        assertEquals(100, handlers.getLast().getAmountAsLong(3), "simulation rolls back");
    }

    @Test
    void preservesOrderPartialExtractionAndCommit() {
        var first = new Drawer();
        var second = new Drawer();
        var resource = ItemResource.of(Items.DIAMOND);
        first.set(1, resource, 3);
        second.set(0, resource, 8);
        var controller = controller(drawers(first, second));
        try (var tx = Transaction.openRoot()) {
            assertEquals(7, controller.extract(resource, 7, tx));
            assertEquals(0, first.getAmountAsLong(1));
            assertEquals(4, second.getAmountAsLong(0));
            assertEquals(0, first.changes);
            tx.commit();
        }
        assertEquals(1, first.changes);
        assertEquals(1, second.changes);
        assertEquals(0, controller.extract(resource, 0, null));
    }

    @Test
    void nestedAbortRestoresContentsAndLaterCallsSeeChanges() {
        var drawer = new Drawer();
        var resource = ItemResource.of(Items.DIAMOND);
        drawer.set(0, resource, 20);
        var controller = controller(drawers(drawer));
        try (var root = Transaction.openRoot()) {
            try (var child = Transaction.open(root)) {
                assertEquals(12, controller.extract(resource, 12, child));
            }
            assertEquals(20, drawer.getAmountAsLong(0));
            assertEquals(5, controller.extract(resource, 5, root));
            root.commit();
        }
        assertEquals(15, drawer.getAmountAsLong(0));
        drawer.set(0, ItemResource.of(Items.STONE), 20);
        try (var tx = Transaction.openRoot()) {
            assertEquals(0, controller.extract(resource, 10, tx));
            drawer.set(2, resource, 7);
            assertEquals(7, controller.extract(resource, 10, tx));
            tx.commit();
        }
    }

    @Test
    void matchesComponentsAndKeepsCreativeAndLockedSemantics() {
        var drawer = new Drawer();
        drawer.creative = true;
        drawer.locked = true;
        var named = new ItemStack(Items.DIAMOND);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Named"));
        var wanted = ItemResource.of(named);
        drawer.set(0, ItemResource.of(Items.DIAMOND), 20);
        drawer.set(1, wanted, 20);
        var controller = controller(drawers(drawer));
        try (var tx = Transaction.openRoot()) {
            assertEquals(20, controller.extract(wanted, 10000, tx));
            tx.commit();
        }
        assertEquals(20, drawer.getStoredStacks().get(1).getAmount());
        drawer.creative = false;
        try (var tx = Transaction.openRoot()) {
            assertEquals(20, controller.extract(wanted, 30, tx));
            tx.commit();
        }
        assertEquals(wanted, drawer.getResource(1), "a drained locked drawer keeps its filter");
        try (var tx = Transaction.openRoot()) {
            assertEquals(0, controller.extract(wanted, 1, tx));
        }
    }

    @Test
    void rebuildAndIndexedOverridesRemainEffective() {
        var first = new Drawer();
        var second = new Drawer();
        var resource = ItemResource.of(Items.DIAMOND);
        first.set(0, resource, 10);
        second.set(0, resource, 10);
        var drawers = drawers(first, second);
        var controller = new ControllerInventoryHandler() {
            @Override public ConnectedDrawers getDrawers() { return drawers; }
            @Override public int extract(int index, ItemResource key, int amount,
                    TransactionContext tx) {
                return index == 0 ? 0 : super.extract(index, key, amount, tx);
            }
        };
        try (var tx = Transaction.openRoot()) {
            assertEquals(5, controller.extract(resource, 5, tx));
            tx.commit();
        }
        assertEquals(10, first.getAmountAsLong(0));
        assertEquals(5, second.getAmountAsLong(0));
        drawers.getItemHandlers().remove(first);
        controller.invalidateSlots();
        try (var tx = Transaction.openRoot()) {
            assertEquals(0, controller.extract(resource, 5, tx));
        }
    }

    @Test
    void validatesRequestsEvenWithNoDrawers() {
        var controller = controller(drawers());
        assertThrows(IllegalArgumentException.class, () -> controller.extract(ItemResource.EMPTY, 1, null));
        assertThrows(IllegalArgumentException.class, () -> controller.extract(ItemResource.of(Items.DIAMOND), -1, null));
        assertEquals(0, controller.extract(ItemResource.of(Items.DIAMOND), 0, null));
    }

    @Test
    void ae2FacadeSimulationAndModulationPreserveItems() {
        var first = new Drawer();
        var second = new Drawer();
        var resource = ItemResource.of(Items.DIAMOND);
        first.set(0, ItemResource.of(Items.STONE), 100);
        first.set(1, resource, 200);
        second.set(3, resource, 500);
        var facade = ExternalStorageFacade.ofItemHandler(controller(drawers(first, second)));
        var key = AEItemKey.of(Items.DIAMOND);
        var source = IActionSource.empty();
        assertEquals(600, facade.extract(key, 600, Actionable.SIMULATE, source));
        assertEquals(200, first.getAmountAsLong(1));
        assertEquals(500, second.getAmountAsLong(3));
        assertEquals(0, first.changes);
        assertEquals(600, facade.extract(key, 600, Actionable.MODULATE, source));
        assertEquals(0, first.getAmountAsLong(1));
        assertEquals(100, second.getAmountAsLong(3));
        assertEquals(100, facade.extract(key, 600, Actionable.MODULATE, source));
        assertEquals(0, facade.extract(key, 600, Actionable.MODULATE, source));
        assertEquals(100, first.getAmountAsLong(0));
    }

    @Test
    void cachedMissSeesStackReplacementAndSetStack() {
        var drawer = new Drawer();
        var resource = ItemResource.of(Items.DIAMOND);
        var controller = controller(drawers(drawer));
        try (var tx = Transaction.openRoot()) {
            assertEquals(0, controller.extract(resource, 5, tx));
            drawer.getStoredStacks().set(0, new BigInventoryHandler.BigStack(new ItemStack(Items.DIAMOND), 10));
            assertEquals(5, controller.extract(resource, 5, tx));
        }
        assertEquals(10, drawer.getAmountAsLong(0));
        drawer.getStoredStacks().get(0).setStack(new ItemStack(Items.STONE));
        try (var tx = Transaction.openRoot()) {
            assertEquals(0, controller.extract(resource, 5, tx));
            drawer.getStoredStacks().get(1).setStack(new ItemStack(Items.DIAMOND));
            drawer.getStoredStacks().get(1).setAmount(8);
            assertEquals(5, controller.extract(resource, 5, tx));
        }
    }

    @Test
    void drainingSimulationAndNestedRollbackInvalidateResourceIndex() {
        var drawer = new Drawer();
        var resource = ItemResource.of(Items.DIAMOND);
        drawer.set(0, resource, 5);
        var controller = controller(drawers(drawer));
        try (var root = Transaction.openRoot()) {
            try (var child = Transaction.open(root)) {
                assertEquals(5, controller.extract(resource, 5, child));
                assertEquals(0, controller.extract(resource, 5, child));
            }
            assertEquals(5, controller.extract(resource, 5, root));
        }
        try (var tx = Transaction.openRoot()) {
            assertEquals(5, controller.extract(resource, 5, tx));
            tx.commit();
        }
        assertTrue(drawer.getResource(0).isEmpty());
    }

    @Test
    void getterOverridesAreNeverCached() {
        var resource = ItemResource.of(Items.DIAMOND);
        var view = new Drawer();
        view.set(0, resource, 10);
        var drawers = drawers(view);
        var controller = new ControllerInventoryHandler() {
            @Override public ConnectedDrawers getDrawers() { return drawers; }
            @Override public ItemResource getResource(int index) { return ItemResource.EMPTY; }
        };
        // Preserve the inherited bulk contract: indexed extract is authoritative,
        // including handlers exposing a different resource view to callers.
        try (var tx = Transaction.openRoot()) {
            assertEquals(5, controller.extract(resource, 5, tx));
        }
    }

    @Test
    void mixedUnversionedHandlersStayLiveAndKeepSlotOrder() {
        var first = new Drawer();
        var last = new Drawer();
        var resource = ItemResource.of(Items.DIAMOND);
        first.set(0, resource, 3);
        last.set(0, resource, 7);
        var external = new ItemStacksResourceHandler(1);
        var drawers = drawers(first, last);
        drawers.getItemHandlers().add(1, external);
        var controller = controller(drawers);
        try (var tx = Transaction.openRoot()) {
            assertEquals(2, controller.extract(resource, 2, tx));
            assertEquals(5, external.insert(0, resource, 5, tx));
            assertEquals(4, controller.extract(resource, 4, tx));
            assertEquals(2, external.getAmountAsLong(0));
            assertEquals(7, last.getAmountAsLong(0));
            tx.commit();
        }
    }

    @Test
    void extractionSideEffectsDoNotHideNewMatchesLaterInSameRequest() {
        var drawer = new Drawer();
        var resource = ItemResource.of(Items.DIAMOND);
        drawer.set(0, resource, 3);
        var drawers = drawers(drawer);
        var controller = new ControllerInventoryHandler() {
            @Override public ConnectedDrawers getDrawers() { return drawers; }
            @Override public int extract(int index, ItemResource key, int amount, TransactionContext tx) {
                int result = super.extract(index, key, amount, tx);
                if (index == 0) drawer.set(1, resource, 5);
                return result;
            }
        };
        try (var tx = Transaction.openRoot()) {
            assertEquals(7, controller.extract(resource, 7, tx));
        }
    }

    @Test
    void customStackResourceViewsFallBackEvenWhenAddedAfterCacheWarmup() {
        var drawer = new Drawer();
        var resource = ItemResource.of(Items.DIAMOND);
        var controller = controller(drawers(drawer));
        try (var tx = Transaction.openRoot()) {
            assertEquals(0, controller.extract(resource, 5, tx));
        }
        class CustomStack extends BigInventoryHandler.BigStack {
            ItemResource view = ItemResource.of(Items.STONE);
            CustomStack() { super(new ItemStack(Items.STONE), 10); }
            @Override public ItemResource getResource() { return view; }
        }
        var stack = new CustomStack();
        drawer.getStoredStacks().set(0, stack);
        try (var tx = Transaction.openRoot()) {
            assertEquals(0, controller.extract(resource, 5, tx));
            stack.view = resource;
            assertEquals(5, controller.extract(resource, 5, tx));
        }
    }

    @Test
    void deserializationInvalidatesCachedMisses() {
        var drawer = new Drawer();
        var source = new Drawer();
        var resource = ItemResource.of(Items.DIAMOND);
        source.set(2, resource, 10);
        var controller = controller(drawers(drawer));
        try (var tx = Transaction.openRoot()) {
            assertEquals(0, controller.extract(resource, 5, tx));
            drawer.deserializeNBT(server.registryAccess(), source.serializeNBT(server.registryAccess()));
            assertEquals(5, controller.extract(resource, 5, tx));
        }
        assertEquals(10, drawer.getAmountAsLong(2));
    }

    @Test
    void compactingDrawersRetainTheirNativeConversionAndRollback() {
        var compacting = new CompactingInventoryHandler(3) {
            @Override public void onChange() {}
            @Override public float getMultiplier() { return 1024; }
            @Override public boolean isVoid() { return false; }
            @Override public boolean isLocked() { return false; }
            @Override public boolean isCreative() { return false; }
        };
        compacting.setupWithRearrangedResults(new ArrayList<>(List.of(
                new CompactingUtil.Result(new ItemStack(Items.IRON_NUGGET), 1),
                new CompactingUtil.Result(new ItemStack(Items.IRON_INGOT), 9),
                new CompactingUtil.Result(new ItemStack(Items.IRON_BLOCK), 81))));
        var resource = ItemResource.of(Items.IRON_INGOT);
        try (var tx = Transaction.openRoot()) {
            assertEquals(2, compacting.insert(1, resource, 2, tx));
            tx.commit();
        }
        var drawers = drawers();
        drawers.getItemHandlers().add(compacting);
        var controller = controller(drawers);
        try (var tx = Transaction.openRoot()) {
            assertEquals(1, controller.extract(resource, 1, tx));
        }
        assertEquals(2, compacting.getAmountAsLong(1));
        try (var tx = Transaction.openRoot()) {
            assertEquals(1, controller.extract(resource, 1, tx));
            tx.commit();
        }
        assertEquals(1, compacting.getAmountAsLong(1));
    }
}
