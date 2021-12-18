package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.block.tile.ActiveTile;
import com.hrznstudio.titanium.block.tile.BasicTile;
import com.hrznstudio.titanium.nbthandler.NBTManager;
import com.hrznstudio.titanium.util.RayTraceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class DrawerTile extends ActiveTile<DrawerTile> {

    private static HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();

    @Save
    public BigInventoryHandler handler;
    private final LazyOptional<IItemHandler> lazyStorage;
    private FunctionalStorage.DrawerType type;

    public DrawerTile(BasicTileBlock<DrawerTile> base, BlockPos pos, BlockState state, FunctionalStorage.DrawerType type) {
        super(base, pos, state);
        this.type = type;
        this.handler = new BigInventoryHandler(type) {
            @Override
            public void onChange() {
                DrawerTile.this.markForUpdate();
            }
        };
        lazyStorage = LazyOptional.of(() -> this.handler);
    }

    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        if (super.onActivated(playerIn, hand, facing, hitX, hitY, hitZ) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }
        if (slot == -1){
            openGui(playerIn);
        } else if (isServer()){
            ItemStack stack = playerIn.getItemInHand(hand);
            if (!stack.isEmpty() && handler.isItemValid(slot, stack)) {
                playerIn.setItemInHand(hand, handler.insertItem(slot, stack, false));
            } else if (System.currentTimeMillis() - INTERACTION_LOGGER.getOrDefault(playerIn.getUUID(), System.currentTimeMillis()) < 300) {
                for (ItemStack itemStack : playerIn.getInventory().items) {
                    if (!itemStack.isEmpty() && handler.insertItem(slot, itemStack, true).isEmpty()) {
                        handler.insertItem(slot, itemStack.copy(), false);
                        itemStack.setCount(0);
                    }
                }
            }
            INTERACTION_LOGGER.put(playerIn.getUUID(), System.currentTimeMillis());
        }
        return InteractionResult.SUCCESS;
    }

    public void onClicked(Player playerIn, int slot) {
        if (isServer()){
            HitResult rayTraceResult = RayTraceUtils.rayTraceSimple(this.level, playerIn, 16, 0);
            if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockResult = (BlockHitResult) rayTraceResult;
                Direction facing = blockResult.getDirection();
                if (facing.equals(this.getFacingDirection())){
                    ItemHandlerHelper.giveItemToPlayer(playerIn, handler.extractItem(slot, playerIn.isShiftKeyDown() ? 64 : 1, false));
                }
            }
        }
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
    public DrawerTile getSelf() {
        return this;
    }

    public BigInventoryHandler getHandler() {
        return handler;
    }

    public FunctionalStorage.DrawerType getDrawerType() {
        return type;
    }
}
