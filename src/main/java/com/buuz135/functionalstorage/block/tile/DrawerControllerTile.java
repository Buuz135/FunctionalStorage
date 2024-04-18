package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.fluid.ControllerFluidHandler;
import com.buuz135.functionalstorage.inventory.ControllerInventoryHandler;
import com.buuz135.functionalstorage.inventory.ILockable;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DrawerControllerTile extends ItemControllableDrawerTile<DrawerControllerTile> {

    private static HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();

    @Save
    private ConnectedDrawers connectedDrawers;
    public ControllerInventoryHandler inventoryHandler;
    public ControllerFluidHandler fluidHandler;
    private LazyOptional<IItemHandler> itemHandlerLazyOptional;
    private LazyOptional<IFluidHandler> fluidHandlerLazyOptional;

    public DrawerControllerTile(BasicTileBlock<DrawerControllerTile> base, BlockEntityType<DrawerControllerTile> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state);
        this.connectedDrawers = new ConnectedDrawers(null);
        this.inventoryHandler = new ControllerInventoryHandler() {
            @Override
            public ConnectedDrawers getDrawers() {
                return connectedDrawers;
            }
        };
        this.itemHandlerLazyOptional = LazyOptional.of(() -> this.inventoryHandler);
        this.fluidHandler = new ControllerFluidHandler() {
            @Override
            public ConnectedDrawers getDrawers() {
                return connectedDrawers;
            }
        };
        this.fluidHandlerLazyOptional = LazyOptional.of(() -> this.fluidHandler);
    }

    @Override
    public int getStorageSlotAmount() {
        return 1;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, DrawerControllerTile blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        if (this.connectedDrawers.getConnectedDrawers().size() != (this.connectedDrawers.getItemHandlers().size() + this.connectedDrawers.getFluidHandlers().size() + this.connectedDrawers.getExtensions())) {
            this.connectedDrawers.getConnectedDrawers().removeIf(aLong -> !(this.getLevel().getBlockEntity(BlockPos.of(aLong)) instanceof ControllableDrawerTile<?>));
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
            for (IItemHandler iItemHandler : this.getConnectedDrawers().itemHandlers) {
                if (iItemHandler instanceof ILockable && ((ILockable) iItemHandler).isLocked()) {
                    for (int slot = 0; slot < iItemHandler.getSlots(); slot++) {
                        if (!stack.isEmpty() && iItemHandler.insertItem(slot, stack, true).getCount() != stack.getCount()) {
                            playerIn.setItemInHand(hand, iItemHandler.insertItem(slot, stack, false));
                            return InteractionResult.SUCCESS;
                        } else if (System.currentTimeMillis() - INTERACTION_LOGGER.getOrDefault(playerIn.getUUID(), System.currentTimeMillis()) < 300) {
                            for (ItemStack itemStack : playerIn.getInventory().items) {
                                if (!itemStack.isEmpty() && iItemHandler.insertItem(slot, itemStack, true).getCount() != itemStack.getCount()) {
                                    itemStack.setCount(iItemHandler.insertItem(slot, itemStack.copy(), false).getCount());
                                }
                            }
                        }
                    }
                }
            }
            for (IItemHandler iItemHandler : this.getConnectedDrawers().itemHandlers) {
                if (iItemHandler instanceof ILockable && !((ILockable) iItemHandler).isLocked()) {
                    for (int slot = 0; slot < iItemHandler.getSlots(); slot++) {
                        if (!stack.isEmpty() && !iItemHandler.getStackInSlot(slot).isEmpty() && iItemHandler.insertItem(slot, stack, true).getCount() != stack.getCount()) {
                            playerIn.setItemInHand(hand, iItemHandler.insertItem(slot, stack, false));
                            return InteractionResult.SUCCESS;
                        } else if (System.currentTimeMillis() - INTERACTION_LOGGER.getOrDefault(playerIn.getUUID(), System.currentTimeMillis()) < 300) {
                            for (ItemStack itemStack : playerIn.getInventory().items) {
                                if (!itemStack.isEmpty() && !iItemHandler.getStackInSlot(slot).isEmpty() && iItemHandler.insertItem(slot, itemStack, true).getCount() != itemStack.getCount()) {
                                    itemStack.setCount(iItemHandler.insertItem(slot, itemStack.copy(), false).getCount());
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
    public IItemHandler getStorage() {
        return inventoryHandler;
    }

    @Override
    public LazyOptional<IItemHandler> getOptional() {
        return itemHandlerLazyOptional;
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
                if (blockEntity instanceof DrawerControllerTile) continue;
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
                if (blockEntity instanceof DrawerControllerTile) continue;
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
        var area = new AABB(this.getBlockPos()).inflate(FunctionalStorageConfig.DRAWER_CONTROLLER_LINKING_RANGE);
        for (BlockPos position : positions) {
            if (level.getBlockState(position).is(FunctionalStorage.DRAWER_CONTROLLER.getLeft().get())) continue;
            if (area.contains(Vec3.atCenterOf(position)) && this.getLevel().getBlockEntity(position) instanceof ControllableDrawerTile<?> controllableDrawerTile) {
                if (action == LinkingToolItem.ActionMode.ADD) {
                    controllableDrawerTile.setControllerPos(this.getBlockPos());
                    if (!connectedDrawers.getConnectedDrawers().contains(position.asLong())){
                        this.connectedDrawers.getConnectedDrawers().add(position.asLong());
                    }
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
            return itemHandlerLazyOptional.cast();
        }
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidHandlerLazyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.fluidHandlerLazyOptional.invalidate();
        this.itemHandlerLazyOptional.invalidate();
    }

    public class ConnectedDrawers implements INBTSerializable<CompoundTag> {

        private List<Long> connectedDrawers;
        private List<IItemHandler> itemHandlers;
        private List<IFluidHandler> fluidHandlers;
        private Level level;
        private int extensions;
        private VoxelShape cachedVoxelShape;

        public ConnectedDrawers(Level level) {
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

        public void rebuildShapes() {
            this.cachedVoxelShape = Shapes.create(new AABB(DrawerControllerTile.this.getBlockPos()));
            for (Long connectedDrawer : this.connectedDrawers) {
                this.cachedVoxelShape = Shapes.join(this.cachedVoxelShape, Shapes.create(new AABB(BlockPos.of(connectedDrawer))), BooleanOp.OR);
            }
        }

        public void rebuild() {
            this.itemHandlers = new ArrayList<>();
            this.fluidHandlers = new ArrayList<>();
            this.extensions = 0;
            if (level != null && !level.isClientSide()) {
                for (Long connectedDrawer : this.connectedDrawers) {
                    BlockPos pos = BlockPos.of(connectedDrawer);
                    BlockEntity entity = level.getBlockEntity(pos);
                    if (entity instanceof DrawerControllerTile) continue;
                    if (entity instanceof ControllerExtensionTile) {
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
            DrawerControllerTile.this.inventoryHandler.invalidateSlots();
            DrawerControllerTile.this.fluidHandler.invalidateSlots();
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
            if (DrawerControllerTile.this.level != null && DrawerControllerTile.this.level.isClientSide()) {
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

    @Override
    public AABB getRenderBoundingBox() {
        return super.getRenderBoundingBox().inflate(50);
    }
}
