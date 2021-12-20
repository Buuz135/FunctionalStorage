package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.block.DrawerControllerBlock;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.ControllerInventoryHandler;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DrawerControllerTile extends ControllableDrawerTile<DrawerControllerTile>{

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
    public void serverTick(Level level, BlockPos pos, BlockState state, DrawerControllerTile blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        if (this.connectedDrawers.getConnectedDrawers().size() != this.connectedDrawers.getHandlers().size()){
            this.connectedDrawers.setLevel(getLevel());
            this.connectedDrawers.rebuild();
            //this.lazyStorage.invalidate();
            //this.lazyStorage = LazyOptional.of(() -> this.handler);
            markForUpdate();
            updateNeigh();
        }
    }

    @Override
    public IItemHandler getStorage() {
        return handler;
    }

    @Override
    public LazyOptional<IItemHandler> getOptional() {
        return lazyStorage;
    }

    @NotNull
    @Override
    public DrawerControllerTile getSelf() {
        return this;
    }

    public void addConnectedDrawers(LinkingToolItem.ActionMode action, BlockPos... positions){
        for (BlockPos position : positions) {
           if (this.getBlockPos().closerThan(position, 8)){
               if (action == LinkingToolItem.ActionMode.ADD){
                   if (!connectedDrawers.getConnectedDrawers().contains(position.asLong())) this.connectedDrawers.getConnectedDrawers().add(position.asLong());
               }
           }
           if (action == LinkingToolItem.ActionMode.REMOVE){
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

        private void rebuild(){
            this.handlers = new ArrayList<>();
            if (level != null && !level.isClientSide()){
                for (Long connectedDrawer : this.connectedDrawers) {
                    BlockPos pos = BlockPos.of(connectedDrawer);
                    BlockEntity entity = level.getBlockEntity(pos);
                    if (entity instanceof ControllableDrawerTile){
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
}
