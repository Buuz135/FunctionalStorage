package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.StorageControllerBlock;
import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.component.ControllerConsolidateButton;
import com.buuz135.functionalstorage.fluid.ControllerFluidHandler;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.ControllerInventoryHandler;
import com.buuz135.functionalstorage.inventory.ILockable;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.util.ConnectedDrawers;
import com.buuz135.functionalstorage.util.DrawerConsolidation;
import com.buuz135.functionalstorage.util.StorageTransferUtil;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.client.screen.addon.TextScreenAddon;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import com.hrznstudio.titanium.container.IObjectContainer;
import com.hrznstudio.titanium.util.TileUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public abstract class StorageControllerTile<T extends StorageControllerTile<T>> extends ItemControllableDrawerTile<T> {

    protected static HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();
    private static final Map<MinecraftServer, DrawerConsolidation.Budget> CONSOLIDATION_BUDGETS = Collections.synchronizedMap(new WeakHashMap<>());

    @Save
    protected ConnectedDrawers connectedDrawers;
    public ControllerInventoryHandler inventoryHandler;
    public ControllerFluidHandler fluidHandler;
    private ResourceHandler<FluidResource> fluidTransferHandler;
    private long nextConsolidationTick;
    private DrawerConsolidation consolidation;
    private Player consolidationPlayer;
    private final ControllerConsolidateButton consolidateButton = new ControllerConsolidateButton();

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
        addContainerAddonFactory(() -> consolidateButton);
        addButton(consolidateButton.setPredicate((player, data) -> {
            if (!isServer() || !getLevel().getServer().isSameThread() || data.getIntOr("Button", -1) != 0 || consolidation != null
                    || getLevel().getGameTime() < nextConsolidationTick || !canStartConsolidation(player)) return;
            nextConsolidationTick = getLevel().getGameTime() + 20;
            consolidationPlayer = player;
            consolidation = new DrawerConsolidation(connectedDrawers.getConnectedDrawers(), linked -> consolidationDrawer(linked, player));
            consolidateButton.update(ControllerConsolidateButton.RUNNING, consolidation.progress(), consolidation.result());
        }));
    }

    private boolean canContinueConsolidation(Player player) {
        return !isRemoved() && !player.isSpectator() && player.level() == getLevel()
                && getLevel().getServer().getPlayerList().getPlayer(player.getUUID()) == player
                && !preventInteraction(getBlockPos(), player);
    }

    private boolean canStartConsolidation(Player player) {
        return canContinueConsolidation(player)
                && player.containerMenu instanceof IObjectContainer menu && menu.getObject() == this
                && player.containerMenu.stillValid(player)
                && player.distanceToSqr(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5) <= 64;
    }

    private BigInventoryHandler consolidationDrawer(long linked, Player player) {
        if (isRemoved() || consolidationPlayer != player) return null;
        BlockPos pos = BlockPos.of(linked);
        if (!new AABB(getBlockPos()).inflate(getStorageMultiplier()).contains(Vec3.atCenterOf(pos))
                || !getLevel().isLoaded(pos) || preventInteraction(pos, player)) return null;
        return getLevel().getBlockEntity(pos) instanceof DrawerTile drawer && getBlockPos().equals(drawer.getControllerPos())
                ? drawer.getStorage() : null;
    }

    @Override
    public int getStorageSlotAmount() {
        return 4;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        if (consolidation != null) {
            var active = consolidation;
            var player = consolidationPlayer;
            boolean stopped = !canContinueConsolidation(player);
            boolean finished = false;
            boolean failed = false;
            if (!stopped) {
                var budget = CONSOLIDATION_BUDGETS.computeIfAbsent(level.getServer(), server -> new DrawerConsolidation.Budget());
                budget.reset(level.getServer().getTickCount());
                try {
                    stopped = finished = active.tick(budget);
                } catch (RuntimeException exception) {
                    LogUtils.getLogger().warn("Drawer consolidation stopped at {}", getBlockPos(), exception);
                    stopped = failed = true;
                }
            }
            if (stopped && consolidation == active) {
                consolidateButton.update(failed ? ControllerConsolidateButton.FAILED
                        : finished ? ControllerConsolidateButton.DONE : ControllerConsolidateButton.STOPPED,
                        active.progress(), active.result());
                consolidation = null;
                consolidationPlayer = null;
            } else if (!stopped && consolidation == active) {
                consolidateButton.update(ControllerConsolidateButton.RUNNING, active.progress(), active.result());
            }
        }
        if (this.connectedDrawers.getConnectedDrawers().size() != (this.connectedDrawers.getItemHandlers().size() + this.connectedDrawers.getFluidHandlers().size() + this.connectedDrawers.getExtensions())) {
            this.connectedDrawers.setLevel(getLevel());
            this.connectedDrawers.rebuild();
            markForUpdate();
            updateNeigh();
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (consolidation != null) consolidateButton.update(ControllerConsolidateButton.STOPPED, consolidation.progress(), consolidation.result());
        consolidation = null;
        consolidationPlayer = null;
        super.onChunkUnloaded();
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
