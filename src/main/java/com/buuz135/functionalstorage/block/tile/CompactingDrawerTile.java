package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.client.gui.DrawerInfoGuiAddon;
import com.buuz135.functionalstorage.inventory.CompactingInventoryHandler;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.util.CompactingUtil;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class CompactingDrawerTile extends ItemControllableDrawerTile<CompactingDrawerTile> {

    @Save
    public CompactingInventoryHandler handler;
    private boolean hasCheckedRecipes;

    public CompactingDrawerTile(BasicTileBlock<CompactingDrawerTile> base, BlockEntityType<CompactingDrawerTile> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state, new DrawerProperties(8, FSAttachments.ITEM_STORAGE_MODIFIER));
        this.handler = new CompactingInventoryHandler(3) {
            @Override
            public void onChange() {
                CompactingDrawerTile.this.markForUpdate();
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initClient() {
        super.initClient();
        addGuiAddonFactory(() -> new DrawerInfoGuiAddon(64, 16,
                com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, this instanceof CompactingFramedDrawerTile ? "textures/block/framed_front_compacting.png" : "textures/block/compacting_drawer_front.png"),
                3,
                integer -> {
                    if (integer == 0) return Pair.of(28, 28);
                    if (integer == 1) return Pair.of(4, 28);
                    return Pair.of(16, 4);
                },
                integer -> getStorage().getStackInSlot(integer),
                integer -> getStorage().getSlotLimit(integer)
        ));
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
        if (!handler.isSetup() && slot != -1 && isServer()) {
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
    public IItemHandler getStorage() {
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
