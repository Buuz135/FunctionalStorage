package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.util.TileUtil;
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
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public abstract class StorageControllerExtensionTile<T extends StorageControllerExtensionTile<T>> extends ItemControllableDrawerTile<T> {

    public StorageControllerExtensionTile(BasicTileBlock<T> base, BlockEntityType<T> entityType, BlockPos pos, BlockState state) {
        super(base, entityType, pos, state);
    }

    @Override
    public int getStorageSlotAmount() {
        return 1;
    }

    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalStorage.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalStorage.LINKING_TOOL.get()))
            return InteractionResult.PASS;
        if (isServer()) {
            return getControllerInstance().map(drawerControllerTile -> drawerControllerTile.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ)).orElse(InteractionResult.PASS);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public IItemHandler getStorage() {
        return getControllerInstance().map(StorageControllerTile::getStorage).orElse(null);
    }

    @Override
    public LazyOptional<IItemHandler> getOptional() {
        return getControllerInstance().map(StorageControllerTile::getOptional).orElse(null);
    }

    @Override
    public int getBaseSize(int lost) {
        return 1;
    }

    @Override
    public void toggleLocking() {
        super.toggleLocking();
        if (isServer()) {
            getControllerInstance().ifPresent(StorageControllerTile::toggleLocking);
        }
    }

    @Override
    public void toggleOption(ConfigurationToolItem.ConfigurationAction action) {
        super.toggleOption(action);
        if (isServer()) {
            getControllerInstance().ifPresent(drawerControllerTile -> drawerControllerTile.toggleOption(action));
        }
    }

    @Nonnull
    @Override
    public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side) {
        return getControllerInstance().map(drawerControllerTile -> drawerControllerTile.getCapability(cap, side)).orElse(super.getCapability(cap, side));
    }

    private Optional<StorageControllerTile> getControllerInstance() {
        if (getControllerPos() == null) return Optional.empty();
        if (level == null || !level.isLoaded(getControllerPos())) return Optional.empty();
        return TileUtil.getTileEntity(this.level, getControllerPos(), StorageControllerTile.class);
    }

    @Override
    public void invalidateCaps() {

    }
}
