package com.buuz135.functionalstorage.test;

import com.buuz135.functionalstorage.FunctionalStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

public class FSTestHelper extends ExtendedGameTestHelper {
    public FSTestHelper(GameTestInfo info) {
        super(info);
    }

    public <T> T requireCapability(ItemCapability<T, ItemAccess> cap, ItemStack item) {
        var capability = ItemAccess.forStack(item).getCapability(cap);
        assertNotNull(capability, "item capability");
        return capability;
    }

    public void setDrawer(BlockPos pos, FunctionalStorage.DrawerType type) {
        setBlock(pos, FunctionalStorage.DRAWER_TYPES.get(type).getFirst().getBlock());
    }

    public void insertAll(BlockPos pos, ItemStack stack, Direction direction) {
        assertValueEqual(insert(pos, stack, direction), stack.count(), "amount inserted");
    }

    public int insert(BlockPos pos, ItemStack stack, Direction direction) {
        var capability = requireCapability(Capabilities.Item.BLOCK, pos, direction);
        try (var trans = Transaction.openRoot()) {
            var inserted = capability.insert(ItemResource.of(stack), stack.count(), trans);
            trans.commit();
            return inserted;
        }
    }

    public ItemStack dropAndDestroy(BlockPos pos) {
        pos = absolutePos(pos);
        var drops = Block.getDrops(
                getLevel().getBlockState(pos),
                getLevel(), pos,
                getLevel().getBlockEntity(pos),
                null,
                ItemStack.EMPTY
        );
        assertValueEqual(1, drops.size(), "block drops");
        getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        return drops.getFirst();
    }
}
