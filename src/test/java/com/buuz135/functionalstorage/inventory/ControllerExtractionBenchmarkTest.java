package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.util.ConnectedDrawers;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.me.storage.ExternalStorageFacade;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Optional local microbenchmark; correctness never depends on wall-clock thresholds. */
@ExtendWith(EphemeralTestServerProvider.class)
@EnabledIfSystemProperty(named = "functionalstorage.benchmark", matches = "true")
class ControllerExtractionBenchmarkTest {
    ControllerExtractionBenchmarkTest(MinecraftServer server) {}

    @Test
    void largeControllerMisses() {
        var fixture = fixture(false);
        try (var tx = Transaction.openRoot()) {
            measure("miss", () -> fixture.controller.extract(ItemResource.of(Items.DIAMOND), 10000, tx), 0);
        }
    }

    @Test
    void ae2SimulationOfPartialExtraction() {
        var fixture = fixture(true);
        var facade = ExternalStorageFacade.ofItemHandler(fixture.controller);
        var key = AEItemKey.of(Items.DIAMOND);
        var source = IActionSource.empty();
        measure("ae2_simulated_hit", () -> facade.extract(key, 600, Actionable.SIMULATE, source), 600);
        assertEquals(20000, fixture.last.getAmountAsLong(3));
    }

    @Test
    void missesWhileAnotherResourceChanges() {
        var fixture = fixture(false);
        var stone = ItemResource.of(Items.STONE);
        var other = ItemResource.of(Items.COBBLESTONE);
        var absent = ItemResource.of(Items.DIAMOND);
        int[] requests = {0};
        try (var tx = Transaction.openRoot()) {
            measure("resource_churn", () -> {
                fixture.last.getStoredStacks().get(0).setResource((requests[0]++ & 1) == 0 ? stone : other);
                return fixture.controller.extract(absent, 10000, tx);
            }, 0);
        }
    }

    private static Fixture fixture(boolean matching) {
        var drawers = new ConnectedDrawers(null, null);
        var present = ItemResource.of(Items.STONE);
        BigInventoryHandler last = null;
        for (int i = 0; i < 512; i++) {
            var drawer = new BigInventoryHandler(FunctionalStorage.DrawerType.X_4) {
                @Override public void onChange() {}
                @Override public float getMultiplier() { return 1024; }
                @Override public boolean isVoid() { return false; }
                @Override public boolean isLocked() { return false; }
                @Override public boolean isCreative() { return false; }
            };
            for (var stack : drawer.getStoredStacks()) {
                stack.setResource(present);
                stack.setAmount(100);
            }
            drawers.getItemHandlers().add(drawer);
            last = drawer;
        }
        if (matching) {
            last.getStoredStacks().get(3).setResource(ItemResource.of(Items.DIAMOND));
            last.getStoredStacks().get(3).setAmount(20000);
        }
        var controller = new ControllerInventoryHandler() {
            @Override public ConnectedDrawers getDrawers() { return drawers; }
        };
        return new Fixture(controller, last);
    }

    private static void measure(String scenario, LongSupplier operation, long expected) {
        long[] times = new long[5];
        for (int i = 0; i < 5000; i++) assertEquals(expected, operation.getAsLong());
        for (int round = 0; round < times.length; round++) {
            long start = System.nanoTime();
            long extracted = 0;
            for (int i = 0; i < 10000; i++) extracted += operation.getAsLong();
            times[round] = System.nanoTime() - start;
            assertEquals(expected * 10000, extracted);
        }
        Arrays.sort(times);
        System.out.println("CONTROLLER_BENCHMARK scenario=" + scenario + " median_ns_per_request=" + times[2] / 10000
                + " slots=2048 rounds=5 requests_per_round=10000");
    }

    private record Fixture(ControllerInventoryHandler controller, BigInventoryHandler last) {}
}
