package com.buuz135.functionalstorage.util;

import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.StorageControllerExtensionTile;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
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
    private VoxelShape cachedVoxelShape;

    public ConnectedDrawers(Level level, StorageControllerTile controllerTile) {
        this.controllerTile = controllerTile;

        this.connectedDrawers = new ArrayList<>();
        this.itemHandlers = new ArrayList<>();
        this.fluidHandlers = new ArrayList<>();
        this.level = level;
        this.extensions = 0;

        this.cachedVoxelShape = null;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void rebuild() {
        this.itemHandlers = new ArrayList<>();
        this.fluidHandlers = new ArrayList<>();
        this.extensions = 0;
        if (level != null && !level.isClientSide()) {
            var extraRange = controllerTile.getStorageMultiplier();
            if (extraRange == 1){
                extraRange = 0;
            }
            var area = new AABB(controllerTile.getBlockPos()).inflate(FunctionalStorageConfig.DRAWER_CONTROLLER_LINKING_RANGE + extraRange);
            this.connectedDrawers.removeIf(aLong -> !area.contains(Vec3.atCenterOf(BlockPos.of(aLong))));
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

    public void rebuildShapes() {
        this.cachedVoxelShape = Shapes.create(new AABB(controllerTile.getBlockPos()));
        for (Long connectedDrawer : this.connectedDrawers) {
            this.cachedVoxelShape = Shapes.join(this.cachedVoxelShape, Shapes.create(new AABB(BlockPos.of(connectedDrawer))), BooleanOp.OR);
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
        if (controllerTile.getLevel() != null && controllerTile.isClient()) {
            rebuildShapes();
        }
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

    public VoxelShape getCachedVoxelShape() {
        return cachedVoxelShape;
    }
}
