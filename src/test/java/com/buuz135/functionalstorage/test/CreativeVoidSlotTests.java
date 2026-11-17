package com.buuz135.functionalstorage.test;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(idPrefix = "creative_void_slot_")
public class CreativeVoidSlotTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Creative drawers handle their extra void slot without indexing a missing stack")
    static void creativeDrawerVoidSlot(FSTestHelper helper) {
        checkVoidSlot(helper, true);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ordinary void drawers retain their configured-resource filter")
    static void ordinaryDrawerVoidSlot(FSTestHelper helper) {
        checkVoidSlot(helper, false);
    }

    private static void checkVoidSlot(FSTestHelper helper, boolean creative) {
        var pos = new BlockPos(1, 0, 1);
        helper.setDrawer(pos, FunctionalStorage.DrawerType.X_1);
        var drawer = helper.getBlockEntity(pos, ControllableDrawerTile.class);
        try (var tx = Transaction.openRoot()) {
            helper.assertValueEqual(drawer.getUtilityUpgrades().insert(ItemResource.of(FunctionalStorage.VOID_UPGRADE.get()), 1, tx), 1, "void upgrade");
            if (creative) {
                helper.assertValueEqual(drawer.getStorageUpgrades().insert(ItemResource.of(FunctionalStorage.CREATIVE_UPGRADE.get()), 1, tx), 1, "creative upgrade");
            }
            tx.commit();
        }
        var handler = helper.requireCapability(Capabilities.Item.BLOCK, pos, Direction.UP);
        var diamond = ItemResource.of(Items.DIAMOND);
        try (var tx = Transaction.openRoot()) {
            helper.assertValueEqual(handler.insert(diamond, 1, tx), 1, "configure diamond");
            tx.commit();
        }
        helper.assertValueEqual(handler.size(), 2, "one physical slot and one synthetic void slot");
        try (var tx = Transaction.openRoot()) {
            helper.assertValueEqual(handler.insert(1, ItemResource.of(Items.STONE), 64, tx), 0, "unconfigured resource rejected");
            helper.assertValueEqual(handler.insert(1, diamond, 64, tx), 64, "configured resource voided");
            helper.assertValueEqual(handler.insert(1, diamond, 0, tx), 0, "zero transfer");
            tx.commit();
        }
        helper.assertValueEqual(handler.getResource(0), diamond, "physical resource preserved");
        helper.assertValueEqual(handler.getAmountAsLong(0), creative ? Integer.MAX_VALUE : 1L, "physical amount preserved");
        helper.succeed();
    }
}
