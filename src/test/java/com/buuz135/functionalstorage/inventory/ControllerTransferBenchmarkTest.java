package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.util.CompactingUtil;
import com.buuz135.functionalstorage.util.ConnectedDrawers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Optional native benchmarks for insertion and heterogeneous controller networks. */
@ExtendWith(EphemeralTestServerProvider.class)
@EnabledIfSystemProperty(named = "functionalstorage.benchmark", matches = "true")
class ControllerTransferBenchmarkTest {
    ControllerTransferBenchmarkTest(MinecraftServer server) {}

    @Test
    void rejectedInsertionIntoUnrelatedDrawers() {
        var fixture = fixture(false, false);
        var wanted = ItemResource.of(Items.DIAMOND);
        try (var tx = Transaction.openRoot()) {
            measure("insert_miss", () -> fixture.insert(wanted, 10000, tx), 0);
        }
    }

    @Test
    void simulatedInsertionIntoLastDrawer() {
        var fixture = fixture(true, false);
        var wanted = ItemResource.of(Items.DIAMOND);
        measure("insert_late_match", () -> {
            try (var tx = Transaction.openRoot()) {
                return fixture.insert(wanted, 600, tx);
            }
        }, 600);
    }

    @Test
    void extractionThroughMixedOrdinaryAndCompactingDrawers() {
        var fixture = fixture(false, true);
        var wanted = ItemResource.of(Items.DIAMOND);
        try (var tx = Transaction.openRoot()) {
            measure("mixed_extract_miss", () -> fixture.extract(wanted, 10000, tx), 0);
        }
    }

    @Test
    void rejectedInsertionIntoLockedEmptySlots() {
        var fixture = fixture(false, false, true);
        var wanted = ItemResource.of(Items.DIAMOND);
        try (var tx = Transaction.openRoot()) {
            measure("insert_locked_empty", () -> fixture.insert(wanted, 10000, tx), 0);
        }
    }

    private static ControllerInventoryHandler fixture(boolean matching, boolean mixed) {
        return fixture(matching, mixed, false);
    }

    private static ControllerInventoryHandler fixture(boolean matching, boolean mixed, boolean locked) {
        var drawers = new ConnectedDrawers(null, null);
        BigInventoryHandler last = null;
        for (int i = 0; i < 512; i++) {
            var drawer = new BigInventoryHandler(FunctionalStorage.DrawerType.X_4) {
                @Override public void onChange() {}
                @Override public float getMultiplier() { return 1024; }
                @Override public boolean isVoid() { return false; }
                @Override public boolean isLocked() { return locked; }
                @Override public boolean isCreative() { return false; }
            };
            for (var stack : drawer.getStoredStacks()) {
                stack.setResource(ItemResource.of(Items.STONE));
                stack.setAmount(100);
            }
            if (locked) for (int slot = 1; slot < 4; slot++) {
                drawer.getStoredStacks().get(slot).setResource(ItemResource.EMPTY);
                drawer.getStoredStacks().get(slot).setAmount(0);
            }
            drawers.getItemHandlers().add(drawer);
            last = drawer;
            if (mixed && i % 8 == 0) {
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
                drawers.getItemHandlers().add(compacting);
            }
        }
        if (matching) {
            last.getStoredStacks().get(3).setResource(ItemResource.of(Items.DIAMOND));
            last.getStoredStacks().get(3).setAmount(20000);
        }
        return new ControllerInventoryHandler() {
            @Override public ConnectedDrawers getDrawers() { return drawers; }
        };
    }

    private static void measure(String scenario, LongSupplier operation, long expected) {
        for (int i = 0; i < 1000; i++) assertEquals(expected, operation.getAsLong());
        long[] times = new long[5];
        for (int round = 0; round < times.length; round++) {
            long start = System.nanoTime();
            long result = 0;
            for (int i = 0; i < 2000; i++) result += operation.getAsLong();
            times[round] = System.nanoTime() - start;
            assertEquals(expected * 2000, result);
        }
        Arrays.sort(times);
        System.out.println("TRANSFER_BENCHMARK scenario=" + scenario + " median_ns_per_request="
                + times[2] / 2000 + " ordinary_drawers=512 rounds=5 requests_per_round=2000");
    }
}
