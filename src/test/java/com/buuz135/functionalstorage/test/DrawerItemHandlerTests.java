package com.buuz135.functionalstorage.test;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(idPrefix = "drawer_item_handler_")
public class DrawerItemHandlerTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests whether drawers items behave correctly as resource handlers")
    static void testRoundtrip(FSTestHelper helper) {
        var pos = new BlockPos(1, 0, 1);
        helper.setDrawer(pos, FunctionalStorage.DrawerType.X_1);

        helper.insertAll(pos, new ItemStack(Items.ACACIA_BUTTON, 128), Direction.UP);

        var drawerItem = helper.dropAndDestroy(pos);

        var capability = helper.requireCapability(Capabilities.Item.ITEM, drawerItem);

        helper.assertValueEqual(capability.getResource(0).getItem(), Items.ACACIA_BUTTON, "item");
        helper.assertValueEqual(capability.getAmountAsInt(0), 128, "item count");

        try (var transaction = Transaction.openRoot()) {
            helper.assertValueEqual(capability.extract(ItemResource.of(Items.ACACIA_BUTTON), 32, transaction), 32, "extracted items");
            helper.assertValueEqual(capability.insert(ItemResource.of(Items.ACACIA_BUTTON), 4, transaction), 4, "inserted items");
            transaction.commit();
        }

        helper.assertValueEqual(capability.getAmountAsInt(0), 100, "item count");

        var player = helper.makeTickingMockServerPlayerInCorner(GameType.CREATIVE);
        player.setItemInHand(InteractionHand.MAIN_HAND, drawerItem);
        helper.placeAt(player, player.getItemInHand(InteractionHand.MAIN_HAND), pos.below(), Direction.UP);
        helper.assertValueEqual(
                helper.requireCapability(Capabilities.Item.BLOCK, pos, Direction.UP).getAmountAsInt(0),
                100, "items contained in placed block"
        );

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests whether drawer items can vend infinite items with the creative vending upgrade")
    static void testCreativeVending(FSTestHelper helper) {
        var pos = new BlockPos(1, 0, 1);
        helper.setDrawer(pos, FunctionalStorage.DrawerType.X_1);
        try (var tx = Transaction.openRoot()) {
            helper.getBlockEntity(pos, ControllableDrawerTile.class).getStorageUpgrades()
                    .insert(ItemResource.of(FunctionalStorage.CREATIVE_UPGRADE.get()), 1, tx);
            tx.commit();
        }

        helper.insertAll(pos, new ItemStack(Items.GLOW_INK_SAC, 1), Direction.UP);
        var drawerItem = helper.dropAndDestroy(pos);
        var capability = helper.requireCapability(Capabilities.Item.ITEM, drawerItem);

        try (var transaction = Transaction.openRoot()) {
            helper.assertValueEqual(capability.extract(ItemResource.of(Items.GLOW_INK_SAC), 50000, transaction), 50000, "extracted items");
            transaction.commit();
            helper.assertValueEqual(capability.extract(ItemResource.of(Items.GLOW_INK_SAC), 50000, transaction), 50000, "extracted items");
        }

        helper.succeed();
    }
}
