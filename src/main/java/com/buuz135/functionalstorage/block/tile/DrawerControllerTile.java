package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.inventory.ControllerInventoryHandler;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DrawerControllerTile extends ControllableDrawerTile<DrawerControllerTile> {

    private static HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();

    @Save
    private ConnectedDrawers connectedDrawers;
    public ControllerInventoryHandler handler;
    private LazyOptional<IItemHandler> lazyStorage;

    public DrawerControllerTile(BasicTileBlock<DrawerControllerTile> base, BlockPos pos, BlockState state) {
        super(base, pos, state);
        this.connectedDrawers = new ConnectedDrawers(null);
        this.handler = new ControllerInventoryHandler() {
            @Override
            public ConnectedDrawers getDrawers() {
                return connectedDrawers;
            }
        };
        this.lazyStorage = LazyOptional.of(() -> this.handler);
    }

    @Override
    public int getStorageSlotAmount() {
        return 1;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, DrawerControllerTile blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        if (this.connectedDrawers.getConnectedDrawers().size() != this.connectedDrawers.getHandlers().size()) {
            this.connectedDrawers.setLevel(getLevel());
            this.connectedDrawers.rebuild();
            markForUpdate();
            updateNeigh();
        }
    }

    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalStorage.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalStorage.LINKING_TOOL.get()))
            return InteractionResult.PASS;
        if (isServer()) {
            for (int slot = 0; slot < getStorage().getSlots(); slot++) {
                if (!stack.isEmpty() && getStorage().insertItem(slot, stack, true).getCount() != stack.getCount()) {
                    playerIn.setItemInHand(hand, getStorage().insertItem(slot, stack, false));
                    return InteractionResult.SUCCESS;
                } else if (System.currentTimeMillis() - INTERACTION_LOGGER.getOrDefault(playerIn.getUUID(), System.currentTimeMillis()) < 300) {
                    boolean worked = false;
                    for (ItemStack itemStack : playerIn.getInventory().items) {
                        if (!itemStack.isEmpty() && !getStorage().getStackInSlot(slot).isEmpty() && getStorage().insertItem(slot, itemStack, true).getCount() != stack.getCount()) {
                            itemStack.setCount(getStorage().insertItem(slot, itemStack.copy(), false).getCount());
                            worked = true;
                        }
                    }
                    if (worked) return InteractionResult.SUCCESS;
                }
            }
            INTERACTION_LOGGER.put(playerIn.getUUID(), System.currentTimeMillis());
        }
        return InteractionResult.SUCCESS;
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
        return 1;
    }

    @Override
    public void toggleLocking() {
        super.toggleLocking();
        if (isServer()) {
            for (Long connectedDrawer : new ArrayList<>(this.connectedDrawers.getConnectedDrawers())) {
                BlockEntity blockEntity = this.level.getBlockEntity(BlockPos.of(connectedDrawer));
                if (blockEntity instanceof ControllableDrawerTile) {
                    ((ControllableDrawerTile<?>) blockEntity).setLocked(this.isLocked());
                }
            }
        }
    }

    @Override
    public void toggleOption(ConfigurationToolItem.ConfigurationAction action) {
        super.toggleOption(action);
        if (isServer()) {
            for (Long connectedDrawer : new ArrayList<>(this.connectedDrawers.getConnectedDrawers())) {
                BlockEntity blockEntity = this.level.getBlockEntity(BlockPos.of(connectedDrawer));
                if (blockEntity instanceof ControllableDrawerTile) {
                    ((ControllableDrawerTile<?>) blockEntity).getDrawerOptions().setActive(action, this.getDrawerOptions().isActive(action));
                    ((ControllableDrawerTile<?>) blockEntity).markForUpdate();
                }
            }
        }
    }

    @NotNull
    @Override
    public DrawerControllerTile getSelf() {
        return this;
    }

    public ConnectedDrawers getConnectedDrawers() {
        return connectedDrawers;
    }

    public void addConnectedDrawers(LinkingToolItem.ActionMode action, BlockPos... positions) {
        for (BlockPos position : positions) {
            if (level.getBlockState(position).is(FunctionalStorage.DRAWER_CONTROLLER.get())) continue;
            if (this.getBlockPos().closerThan(position, FunctionalStorageConfig.DRAWER_CONTROLLER_LINKING_RANGE)) {
                if (action == LinkingToolItem.ActionMode.ADD) {
                    if (!connectedDrawers.getConnectedDrawers().contains(position.asLong()))
                        this.connectedDrawers.getConnectedDrawers().add(position.asLong());
                }
            }
            if (action == LinkingToolItem.ActionMode.REMOVE) {
                this.connectedDrawers.getConnectedDrawers().removeIf(aLong -> aLong == position.asLong());
            }
        }
        this.connectedDrawers.rebuild();
        markForUpdate();
    }

    @Nonnull
    @Override
    public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyStorage.cast();
        }
        return super.getCapability(cap, side);
    }

    public static class ConnectedDrawers implements INBTSerializable<CompoundTag> {

        private List<Long> connectedDrawers;
        private List<IItemHandler> handlers;
        private Level level;

        public ConnectedDrawers(Level level) {
            this.connectedDrawers = new ArrayList<>();
            this.handlers = new ArrayList<>();
            this.level = level;
        }

        public void setLevel(Level level) {
            this.level = level;
        }

        private void rebuild() {
            this.handlers = new ArrayList<>();
            if (level != null && !level.isClientSide()) {
                for (Long connectedDrawer : this.connectedDrawers) {
                    BlockPos pos = BlockPos.of(connectedDrawer);
                    BlockEntity entity = level.getBlockEntity(pos);
                    if (entity instanceof ControllableDrawerTile) {
                        this.handlers.add(((ControllableDrawerTile<?>) entity).getStorage());
                    }
                }
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag compoundTag = new CompoundTag();
            for (int i = 0; i < this.connectedDrawers.size(); i++) {
                compoundTag.putLong(i + "", this.connectedDrawers.get(i));
            }
            return compoundTag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.connectedDrawers = new ArrayList<>();
            for (String allKey : nbt.getAllKeys()) {
                connectedDrawers.add(nbt.getLong(allKey));
            }
            rebuild();
        }

        public List<Long> getConnectedDrawers() {
            return connectedDrawers;
        }

        public List<IItemHandler> getHandlers() {
            return handlers;
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return super.getRenderBoundingBox().inflate(50);
    }
}
