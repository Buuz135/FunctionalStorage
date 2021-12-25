package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.inventory.ArmoryCabinetInventoryHandler;
import com.buuz135.functionalstorage.inventory.CompactingInventoryHandler;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.block.tile.ActiveTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArmoryCabinetTile extends ActiveTile<ArmoryCabinetTile> {

    //TODO Not sync nbt

    @Save
    public ArmoryCabinetInventoryHandler handler;
    private final LazyOptional<IItemHandler> lazyStorage;

    public ArmoryCabinetTile(BasicTileBlock<ArmoryCabinetTile> base, BlockPos pos, BlockState state) {
        super(base, pos, state);
        this.handler = new ArmoryCabinetInventoryHandler() {
            @Override
            public void onChange() {
                ArmoryCabinetTile.this.markForUpdate();
            }
        };
        this.lazyStorage = LazyOptional.of(() -> handler);
    }

    @Nonnull
    @Override
    public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyStorage.cast();
        }
        return super.getCapability(cap, side);
    }

    public IItemHandler getStorage() {
        return handler;
    }

    public LazyOptional<IItemHandler> getOptional() {
        return lazyStorage;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, blockEntity -> new CompoundTag());
    }

    @NotNull
    @Override
    public ArmoryCabinetTile getSelf() {
        return this;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        getOptional().invalidate();
    }
}
