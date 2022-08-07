package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DrawerTile extends ControllableDrawerTile<DrawerTile> {

    @Save
    public BigInventoryHandler handler;
    private final LazyOptional<IItemHandler> lazyStorage;
    private FunctionalStorage.DrawerType type;

    public DrawerTile(BasicTileBlock<DrawerTile> base, BlockEntityType<DrawerTile> blockEntityType, BlockPos pos, BlockState state, FunctionalStorage.DrawerType type) {
        super(base, blockEntityType, pos, state);
        this.type = type;
        this.handler = new BigInventoryHandler(type) {
            @Override
            public void onChange() {
                DrawerTile.this.markForUpdate();
            }

            @Override
            public int getMultiplier() {
                return getStorageMultiplier();
            }

            @Override
            public boolean isVoid() {
                return DrawerTile.this.isVoid();
            }

            @Override
            public boolean hasDowngrade() {
                return DrawerTile.this.hasDowngrade();
            }

            @Override
            public boolean isLocked() {
                return DrawerTile.this.isLocked();
            }

            @Override
            public boolean isCreative() {
                return DrawerTile.this.isCreative();
            }


        };
        lazyStorage = LazyOptional.of(() -> this.handler);
    }

    @Nonnull
    @Override
    public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyStorage.cast();
        }
        return super.getCapability(cap, side);
    }

    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalStorage.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalStorage.LINKING_TOOL.get())) return InteractionResult.PASS;
        if (slot != -1 && !playerIn.getItemInHand(hand).isEmpty()){
            BigInventoryHandler.BigStack bigStack = getHandler().getStoredStacks().get(slot);
            if (bigStack.getStack().isEmpty()){
                bigStack.setStack(playerIn.getItemInHand(hand));
            }
        }
        return super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot);
    }

    @NotNull
    @Override
    public DrawerTile getSelf() {
        return this;
    }

    public FunctionalStorage.DrawerType getDrawerType() {
        return type;
    }

    @Override
    public int getStorageSlotAmount() {
        return 4;
    }

    @Override
    public IItemHandler getStorage() {
        return handler;
    }

    @Override
    public LazyOptional<IItemHandler> getOptional() {
        return lazyStorage;
    }

    @Override
    public int getBaseSize(int lost) {
        return type.getSlotAmount();
    }

    public BigInventoryHandler getHandler() {
        return handler;
    }

}
