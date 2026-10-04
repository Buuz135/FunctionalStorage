package com.buuz135.functionalstorage.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

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

    @Nullable
    public static ResourceStack<FluidResource> moveWithSound(ResourceHandler<FluidResource> from, ResourceHandler<FluidResource> to, Level level, @Nullable BlockPos pos,
                                                              @Nullable Player player, @Nullable TransactionContext transaction, boolean pickup) {
        if (player == null && pos == null) {
            throw new IllegalArgumentException("Either player or pos must be provided.");
        }

        var moved = ResourceHandlerUtil.moveFirst(from, to, fr -> true, Integer.MAX_VALUE, transaction);
        if (moved != null) {
            playSoundAndGameEvent(moved.resource(), level, pos, player, transaction, pickup);
        }
        return moved;
    }

    private static void playSoundAndGameEvent(FluidResource resource, Level level, @Nullable BlockPos blockPos, @Nullable Player player, @Nullable TransactionContext transaction, boolean pickup) {
        if (player == null && blockPos == null) {
            throw new IllegalArgumentException("Either player or blockPos must be provided.");
        }

        // Prioritize block position, use player position as a fallback
        Vec3 position = blockPos != null ? Vec3.atCenterOf(blockPos) : new Vec3(player.getX(), player.getY() + 0.5, player.getZ());

        if (transaction == null) {
            //No transaction, just trigger the sound and game event immediately
            FluidUtil.triggerSoundAndGameEvent(resource, level, position, player, pickup);
        } else {
            //Trigger on root commit
            RootCommitJournal onRootCommit = new RootCommitJournal(() -> FluidUtil.triggerSoundAndGameEvent(resource, level, position, player, pickup));
            onRootCommit.updateSnapshots(transaction);
        }
    }
}
