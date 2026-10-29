package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.FluidDrawerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(EphemeralTestServerProvider.class)
class FluidDrawerPersistenceTest {

    private static final BlockPos POSITION = new BlockPos(8, 64, 8);

    @SuppressWarnings("unchecked")
    private static FluidDrawerTile drawer() {
        var block = (FluidDrawerBlock) BuiltInRegistries.BLOCK.getValue(
                Identifier.fromNamespaceAndPath("functionalstorage", "fluid_1"));
        // The level is mocked: keep packet delivery and neighbour updates out of this test.
        // onChange(), the native handler, transactions and serialization remain real.
        return new FluidDrawerTile(block,
                (BlockEntityType<FluidDrawerTile>) FunctionalStorage.FLUID_DRAWER_1.type().get(),
                POSITION, block.defaultBlockState(), FunctionalStorage.DrawerType.X_1) {
            @Override
            public void syncObject(Object object) {
            }

            @Override
            public void updateComparatorOutput() {
            }

            @Override
            public boolean isVoid() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
    }

    @Test
    void committedFluidChangesRequestPersistence(MinecraftServer server) {
        var level = mock(ServerLevel.class);
        var drawer = drawer();
        drawer.setLevel(level);
        var lava = FluidResource.of(Fluids.LAVA);

        try (var transaction = Transaction.openRoot()) {
            assertEquals(4000, drawer.fluidHandler.insert(lava, 4000, transaction));
            transaction.commit();
        }
        verify(level).blockEntityChanged(POSITION);
        clearInvocations(level);

        for (int i = 0; i < 100; i++) {
            try (var transaction = Transaction.openRoot()) {
                assertEquals(100, drawer.fluidHandler.extract(lava, 100, transaction));
            }
            assertEquals(4000, drawer.fluidHandler.getAmountAsLong(0));
        }
        verify(level, never()).blockEntityChanged(any());

        // Committing a child transaction must not persist an aborted parent.
        try (var parent = Transaction.openRoot()) {
            try (var child = Transaction.open(parent)) {
                assertEquals(100, drawer.fluidHandler.insert(lava, 100, child));
                child.commit();
            }
            verify(level, never()).blockEntityChanged(any());
        }
        assertEquals(4000, drawer.fluidHandler.getAmountAsLong(0));
        verify(level, never()).blockEntityChanged(any());

        try (var transaction = Transaction.openRoot()) {
            assertEquals(100, drawer.fluidHandler.insert(lava, 100, transaction));
            transaction.commit();
        }
        assertEquals(4100, drawer.fluidHandler.getAmountAsLong(0));
        verify(level).blockEntityChanged(POSITION);

        var saved = drawer.fluidHandler.serializeNBT(server.registryAccess());
        var restored = drawer();
        restored.fluidHandler.deserializeNBT(server.registryAccess(), saved);
        assertEquals(lava, restored.fluidHandler.getResource(0));
        assertEquals(4100, restored.fluidHandler.getAmountAsLong(0));

        clearInvocations(level);
        try (var transaction = Transaction.openRoot()) {
            assertEquals(100, drawer.fluidHandler.extract(lava, 100, transaction));
            transaction.commit();
        }
        verify(level).blockEntityChanged(POSITION);
        assertEquals(4000, drawer.fluidHandler.getAmountAsLong(0));
    }
}
