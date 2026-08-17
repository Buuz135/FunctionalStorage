package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.StorageControllerBlock;
import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.fluid.ControllerFluidHandler;
import com.buuz135.functionalstorage.inventory.ControllerInventoryHandler;
import com.buuz135.functionalstorage.inventory.ILockable;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.util.ConnectedDrawers;
import com.buuz135.functionalstorage.util.StorageTransferUtil;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.client.screen.addon.TextScreenAddon;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public abstract class StorageControllerTile<T extends StorageControllerTile<T>> extends ItemControllableDrawerTile<T> {

    protected static HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();

    @Save
    protected ConnectedDrawers connectedDrawers;
    public ControllerInventoryHandler inventoryHandler;
    public ControllerFluidHandler fluidHandler;
    private ResourceHandler<FluidResource> fluidTransferHandler;

    public StorageControllerTile(BasicTileBlock<T> base, BlockEntityType<T> entityType, BlockPos pos, BlockState state) {
        super(base, entityType, pos, state, new DrawerProperties(FunctionalStorageConfig.DRAWER_CONTROLLER_LINKING_RANGE, FSAttachments.CONTROLLER_RANGE_MODIFIER));
        this.connectedDrawers = new ConnectedDrawers(null, this);
        this.inventoryHandler = new ControllerInventoryHandler() {
            @Override
            public ConnectedDrawers getDrawers() {
                return connectedDrawers;
            }
        };
        this.fluidHandler = new ControllerFluidHandler() {
            @Override
            public ConnectedDrawers getDrawers() {
                return connectedDrawers;
            }
        };
    }

    @Override
    public int getStorageSlotAmount() {
        return 4;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        if (this.connectedDrawers.getConnectedDrawers().size() != (this.connectedDrawers.getItemHandlers().size() + this.connectedDrawers.getFluidHandlers().size() + this.connectedDrawers.getExtensions())) {
            this.connectedDrawers.setLevel(getLevel());
            this.connectedDrawers.rebuild();
            markForUpdate();
            updateNeigh();
        }
    }

    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ) {
        ItemStack stack = playerIn.getItemInHand(hand);
        ItemResource heldResource = ItemResource.of(stack);
        if (stack.getItem().equals(FunctionalStorage.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalStorage.LINKING_TOOL.get()))
            return InteractionResult.PASS;
        if (isServer()) {
            if (playerIn.isCrouching()) {
                openGui(playerIn);
            } else {
                playerIn.sendOverlayMessage(Component.translatable("gui.functionalstorage.open_gui").withStyle(ChatFormatting.GRAY));
            }
            for (ResourceHandler<ItemResource> iItemHandler : this.connectedDrawers.getItemHandlers()) {
                if (iItemHandler instanceof ILockable && ((ILockable) iItemHandler).isLocked()) {
                    for (int slot = 0; slot < iItemHandler.size(); slot++) {
                        int inserted = StorageTransferUtil.insert(iItemHandler, slot, heldResource, stack.getCount(), false);
                        if (!stack.isEmpty() && inserted > 0) {
                            StorageTransferUtil.insert(iItemHandler, slot, heldResource, inserted, true);
                            playerIn.setItemInHand(hand, inserted == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted));
                            return InteractionResult.SUCCESS;
                        } else if (System.currentTimeMillis() - INTERACTION_LOGGER.getOrDefault(playerIn.getUUID(), System.currentTimeMillis()) < 300) {
                            var inventory = playerIn.getInventory();
                            for (int inventorySlot = 0; inventorySlot < inventory.getContainerSize(); inventorySlot++) {
                                ItemStack itemStack = inventory.getItem(inventorySlot);
                                ItemResource inventoryResource = ItemResource.of(itemStack);
                                int inventoryInserted = StorageTransferUtil.insert(iItemHandler, slot, inventoryResource, itemStack.getCount(), false);
                                if (!itemStack.isEmpty() && inventoryInserted > 0) {
                                    StorageTransferUtil.insert(iItemHandler, slot, inventoryResource, inventoryInserted, true);
                                    itemStack.shrink(inventoryInserted);
                                }
                            }
                        }
                    }
                }
            }
            for (ResourceHandler<ItemResource> iItemHandler : this.connectedDrawers.getItemHandlers()) {
                if (iItemHandler instanceof ILockable && !((ILockable) iItemHandler).isLocked()) {
                    for (int slot = 0; slot < iItemHandler.size(); slot++) {
                        int inserted = StorageTransferUtil.insert(iItemHandler, slot, heldResource, stack.getCount(), false);
                        if (!stack.isEmpty() && !iItemHandler.getResource(slot).isEmpty() && inserted > 0) {
                            StorageTransferUtil.insert(iItemHandler, slot, heldResource, inserted, true);
                            playerIn.setItemInHand(hand, inserted == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted));
                            return InteractionResult.SUCCESS;
                        } else if (System.currentTimeMillis() - INTERACTION_LOGGER.getOrDefault(playerIn.getUUID(), System.currentTimeMillis()) < 300) {
                            var inventory = playerIn.getInventory();
                            for (int inventorySlot = 0; inventorySlot < inventory.getContainerSize(); inventorySlot++) {
                                ItemStack itemStack = inventory.getItem(inventorySlot);
                                ItemResource inventoryResource = ItemResource.of(itemStack);
                                int inventoryInserted = StorageTransferUtil.insert(iItemHandler, slot, inventoryResource, itemStack.getCount(), false);
                                if (!itemStack.isEmpty() && !iItemHandler.getResource(slot).isEmpty() && inventoryInserted > 0) {
                                    StorageTransferUtil.insert(iItemHandler, slot, inventoryResource, inventoryInserted, true);
                                    itemStack.shrink(inventoryInserted);
                                }
                            }
                        }
                    }
                }
            }
            INTERACTION_LOGGER.put(playerIn.getUUID(), System.currentTimeMillis());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public ControllerInventoryHandler getStorage() {
        return inventoryHandler;
    }

    @Override
    public ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction direction) {
        if (fluidTransferHandler == null) fluidTransferHandler = fluidHandler;
        return fluidTransferHandler;
    }

    @Override
    public void toggleLocking() {
        super.toggleLocking();
        if (isServer()) {
            for (Long connectedDrawer : new ArrayList<>(this.connectedDrawers.getConnectedDrawers())) {
                BlockEntity blockEntity = this.level.getBlockEntity(BlockPos.of(connectedDrawer));
                if (blockEntity instanceof StorageControllerTile) continue;
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
                if (blockEntity instanceof StorageControllerTile) continue;
                if (blockEntity instanceof ControllableDrawerTile) {
                    if (action.getMax() == 1) {
                        ((ControllableDrawerTile<?>) blockEntity).getDrawerOptions().setActive(action, this.getDrawerOptions().isActive(action));
                    } else {
                        ((ControllableDrawerTile<?>) blockEntity).getDrawerOptions().setAdvancedValue(action, this.getDrawerOptions().getAdvancedValue(action));
                    }
                    ((ControllableDrawerTile<?>) blockEntity).markForUpdate();
                }
            }
        }
    }

    public ConnectedDrawers getConnectedDrawers() {
        return connectedDrawers;
    }

    @Override
    public int getUtilitySlotAmount() {
        return 0;
    }

    public boolean addConnectedDrawers(LinkingToolItem.ActionMode action, BlockPos... positions) {
        var range = getStorageMultiplier();
        var didWork = false;
        var area = new AABB(this.getBlockPos()).inflate(range);
        for (BlockPos position : positions) {
            if (level.getBlockState(position).getBlock() instanceof StorageControllerBlock) continue;
            if (area.contains(Vec3.atCenterOf(position)) && this.getLevel().getBlockEntity(position) instanceof ControllableDrawerTile<?> controllableDrawerTile) {
                if (action == LinkingToolItem.ActionMode.ADD) {
                    controllableDrawerTile.setControllerPos(this.getBlockPos());
                    if (!connectedDrawers.getConnectedDrawers().contains(position.asLong())){
                        this.connectedDrawers.getConnectedDrawers().add(position.asLong());
                        didWork = true;
                    }
                }
            }
            if (action == LinkingToolItem.ActionMode.REMOVE) {
                this.connectedDrawers.getConnectedDrawers().removeIf(aLong -> aLong == position.asLong());
                TileUtil.getTileEntity(level, position, ControllableDrawerTile.class).ifPresent(controllableDrawerTile -> controllableDrawerTile.clearControllerPos());
                didWork = true;
            }
        }
        this.connectedDrawers.rebuild();

        markForUpdate();
        return didWork;
    }

    // TODO 1.20.4 - fix
//    @Override
//    public AABB getRenderBoundingBox() {
//        return super.getRenderBoundingBox().inflate(1200);
//    }

    @Override
    public InventoryComponent<ControllableDrawerTile<T>> getStorageUpgradesConstructor() {
        return new InventoryComponent<ControllableDrawerTile<T>>("storage_upgrades", 10, 70, getStorageSlotAmount())
                .setInputFilter((stack, integer) -> stack.has(FSAttachments.CONTROLLER_RANGE_MODIFIER))
                .setOnSlotChanged((stack, integer) -> {
                    setNeedsUpgradeCache(true);
                    this.connectedDrawers.rebuild();
                    this.connectedDrawers.rebuildShapes();
                    markForUpdate();
                })
                .setSlotLimit(1);
    }
}
