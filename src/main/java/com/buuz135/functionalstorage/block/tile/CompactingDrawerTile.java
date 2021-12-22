package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.CompactingInventoryHandler;
import com.buuz135.functionalstorage.util.CompactingUtil;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.block.tile.ActiveTile;
import com.hrznstudio.titanium.util.RayTraceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class CompactingDrawerTile extends ControllableDrawerTile<CompactingDrawerTile> {

    @Save
    public CompactingInventoryHandler handler;
    private final LazyOptional<IItemHandler> lazyStorage;

    public CompactingDrawerTile(BasicTileBlock<CompactingDrawerTile> base, BlockPos pos, BlockState state) {
        super(base, pos, state);
        this.handler = new CompactingInventoryHandler() {
            @Override
            public void onChange() {
                CompactingDrawerTile.this.markForUpdate();
            }

            @Override
            public int getMultiplier() {
                return getStorageMultiplier();
            }

            @Override
            public boolean isVoid() {
                return CompactingDrawerTile.this.isVoid();
            }
        };
        lazyStorage = LazyOptional.of(() -> this.handler);
        //TODO Check for the recipe on load
    }

    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        if (!handler.isSetup() && slot != -1){
            ItemStack stack = playerIn.getItemInHand(hand).copy();
            stack.setCount(1);
            CompactingUtil compactingUtil = new CompactingUtil(this.level);
            compactingUtil.setup(stack);
            handler.setup(compactingUtil);
            for (int i = 0; i < handler.getResultList().size(); i++) {
                if (ItemStack.isSame(handler.getResultList().get(i).getResult(), stack)){
                    slot = i;
                    break;
                }
            }
        }
        super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot);
        return InteractionResult.SUCCESS;
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
    public LazyOptional<IItemHandler> getOptional() {
        return lazyStorage;
    }

    @Nonnull
    @Override
    public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyStorage.cast();
        }
        return super.getCapability(cap, side);
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
