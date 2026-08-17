package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.inventory.CompactingInventoryHandler;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.util.CompactingUtil;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class CompactingDrawerTile extends ItemControllableDrawerTile<CompactingDrawerTile> {

    @Save
    public CompactingInventoryHandler handler;
    @Save
    private boolean hasCheckedRecipes;

    public CompactingDrawerTile(BasicTileBlock<CompactingDrawerTile> base, BlockEntityType<CompactingDrawerTile> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state, new DrawerProperties(8, FSAttachments.ITEM_STORAGE_MODIFIER));
        this.handler = new CompactingInventoryHandler(3) {
            @Override
            public void onChange() {
                CompactingDrawerTile.this.markForUpdate();
                CompactingDrawerTile.this.updateComparatorOutput();
            }

            @Override
            public float getMultiplier() {
                return getStorageMultiplier();
            }

            @Override
            public boolean isVoid() {
                return CompactingDrawerTile.this.isVoid();
            }

            @Override
            public boolean isCreative() {
                return CompactingDrawerTile.this.isCreative();
            }

            @Override
            public boolean isLocked() {
                return CompactingDrawerTile.this.isLocked();
            }

        };
        this.hasCheckedRecipes = false;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, CompactingDrawerTile blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        if (!hasCheckedRecipes) {
            if (!handler.getParent().isEmpty()) {
                CompactingUtil compactingUtil = new CompactingUtil(this.level, 3);
                compactingUtil.setup(handler.getParent());
                handler.setup(compactingUtil);
            }
            hasCheckedRecipes = true;
        }
    }

    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalStorage.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalStorage.LINKING_TOOL.get())) return InteractionResult.PASS;
        if (!handler.isSetup() && slot != -1 && isServer() && !stack.is(StorageTags.DRAWER_STORAGE_DENYLIST)) {
            stack = playerIn.getItemInHand(hand).copy();
            stack.setCount(1);
            CompactingUtil compactingUtil = new CompactingUtil(this.level, 3);
            compactingUtil.setup(stack, slot);
            List<CompactingUtil.Result> rearrangedResults = compactingUtil.rearrangeResults(stack, slot);
            handler.setupWithRearrangedResults(rearrangedResults);
            markForUpdate();
        }
        return super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot);
    }

    @Override
    public int getStorageSlotAmount() {
        return 3;
    }

    @Override
    public CompactingInventoryHandler getStorage() {
        return handler;
    }

    @Override
    protected boolean canChangeMultiplier(double newSizeMultiplier) {
        for (int i = 0; i < getStorage().getSlots(); i++) {
            var stored = getStorage().getStackInSlot(i);
            if (!stored.isEmpty() && stored.getCount() > Math.min(Integer.MAX_VALUE, Math.floor(newSizeMultiplier * getHandler().getSlotLimitBase(i)))) {
                return false;
            }
        }
        return true;
    }

    @NotNull
    @Override
    public CompactingDrawerTile getSelf() {
        return this;
    }

    public CompactingInventoryHandler getHandler() {
        return handler;
    }

}
