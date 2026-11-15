package com.buuz135.functionalstorage.test;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

import java.util.stream.IntStream;

@ForEachTest(idPrefix = "storage_upgrade_")
public class StorageUpgradeTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests whether storage upgrades can be replaced in-place")
    static void testReplacement(FSTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setDrawer(pos, FunctionalStorage.DrawerType.X_1);
        var blockEntity = helper.getBlockEntity(pos, ControllableDrawerTile.class);

        try (var tx = Transaction.openRoot()) {
            blockEntity.getStorageUpgrades().insert(0, ItemResource.of(FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.GOLD).get()), 1, tx);
            tx.commit();
        }
        helper.insertAll(pos, Items.ACACIA_BUTTON.getDefaultInstance().copyWithCount(30_000), Direction.DOWN);

        var player = helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL);

        player.openMenu(blockEntity);
        var container = player.containerMenu;

        var upgradeSlotsOffset = IntStream.range(0, container.slots.size())
                        .filter(s -> container.slots.get(s) instanceof ResourceHandlerSlot rhs && rhs.getResourceHandler() == blockEntity.getStorageUpgrades())
                        .findFirst().orElseThrow();

        // This should fail as we cannot replace gold with iron given that the storage is already over 64
        helper.assertItem(
                helper.swapInContainer(player, upgradeSlotsOffset, FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON).get().getDefaultInstance()),
                FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON).get(), 1
        );

        // This should fail as we cannot replace gold with copper (copper < gold) given that the storage is already over 16k
        helper.assertItem(
                helper.swapInContainer(player, upgradeSlotsOffset, FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.COPPER).get().getDefaultInstance()),
                FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.COPPER).get(), 1
        );

        // This should work as diamond > gold so the swap should be successful
        helper.assertItem(
                helper.swapInContainer(player, upgradeSlotsOffset, FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.DIAMOND).get().getDefaultInstance()),
                FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.GOLD).get(), 1
        );

        helper.succeed();
    }
}
