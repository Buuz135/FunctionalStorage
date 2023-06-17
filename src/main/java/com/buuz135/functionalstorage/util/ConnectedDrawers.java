package com.buuz135.functionalstorage.util;

import com.buuz135.functionalstorage.block.tile.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class ConnectedDrawers implements INBTSerializable<CompoundTag> {

    private StorageControllerTile controllerTile;

    private List<Long> connectedDrawers;
    private List<IItemHandler> itemHandlers;
    private List<IFluidHandler> fluidHandlers;
    private Level level;
    private int extensions;

    public ConnectedDrawers(Level level, StorageControllerTile controllerTile) {
        this.controllerTile = controllerTile;

        this.connectedDrawers = new ArrayList<>();
        this.itemHandlers = new ArrayList<>();
        this.fluidHandlers = new ArrayList<>();
        this.level = level;
        this.extensions = 0;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void rebuild() {
        this.itemHandlers = new ArrayList<>();
        this.fluidHandlers = new ArrayList<>();
        this.extensions = 0;
        if (level != null && !level.isClientSide()) {
            for (Long connectedDrawer : this.connectedDrawers) {
                BlockPos pos = BlockPos.of(connectedDrawer);
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof StorageControllerTile) continue;
                if (entity instanceof StorageControllerExtensionTile) {
                    ++extensions;
                    continue;
                }
                if (entity instanceof ItemControllableDrawerTile<?> itemControllableDrawerTile) {
                    this.itemHandlers.add(itemControllableDrawerTile.getStorage());
                }
                if (entity instanceof FluidDrawerTile fluidDrawerTile) {
                    this.fluidHandlers.add(fluidDrawerTile.getFluidHandler());
                }
            }
        }

        this.controllerTile.inventoryHandler.invalidateSlots();
        this.controllerTile.fluidHandler.invalidateSlots();
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

    public List<IItemHandler> getItemHandlers() {
        return itemHandlers;
    }

    public List<IFluidHandler> getFluidHandlers() {
        return fluidHandlers;
    }

    public int getExtensions() {
        return extensions;
    }
}
