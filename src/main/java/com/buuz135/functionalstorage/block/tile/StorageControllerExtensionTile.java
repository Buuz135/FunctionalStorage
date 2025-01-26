package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class StorageControllerExtensionTile<T extends StorageControllerExtensionTile<T>> extends ItemControllableDrawerTile<T> {

    public StorageControllerExtensionTile(BasicTileBlock<T> base, BlockEntityType<T> entityType, BlockPos pos, BlockState state) {
        super(base, entityType, pos, state, new DrawerProperties(FunctionalStorageConfig.DRAWER_CONTROLLER_LINKING_RANGE, FSAttachments.CONTROLLER_RANGE_MODIFIER));
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
    public IFluidHandler getFluidHandler(@Nullable Direction direction) {
        return getControllerInstance().map(c -> c.getFluidHandler(direction)).orElse(null);
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

    private Optional<StorageControllerTile> getControllerInstance() {
        if (getControllerPos() == null) return Optional.empty();
        if (level == null || !level.isLoaded(getControllerPos())) return Optional.empty();
        return TileUtil.getTileEntity(this.level, getControllerPos(), StorageControllerTile.class);
    }

    private boolean hasUpdated = false;

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        if (!hasUpdated){
            hasUpdated = true;
            updateNeigh();
            markForUpdate();
        }
    }
}
