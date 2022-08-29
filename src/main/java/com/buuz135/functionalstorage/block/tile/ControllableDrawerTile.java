package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.DrawerBlock;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.block.tile.ActiveTile;
import com.hrznstudio.titanium.client.screen.addon.TextScreenAddon;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public abstract class ControllableDrawerTile<T extends ControllableDrawerTile<T>> extends ActiveTile<T> {

    private static HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();

    private boolean needsUpgradeCache = true;

    @Save
    private BlockPos controllerPos;
    @Save
    private InventoryComponent<ControllableDrawerTile<T>> storageUpgrades;
    @Save
    private InventoryComponent<ControllableDrawerTile<T>> utilityUpgrades;
    @Save
    private DrawerOptions drawerOptions;
    @Save
    private boolean m_hasDowngrade = false;
    @Save
    private boolean m_isCreative = false;
    @Save
    private boolean m_isVoid = false;
    @Save
    private int m_mult = 1;

    public ControllableDrawerTile(BasicTileBlock<T> base, BlockEntityType<T> entityType, BlockPos pos, BlockState state) {
        super(base, entityType, pos, state);
        this.drawerOptions = new DrawerOptions();
        this.storageUpgrades = new InventoryComponent<ControllableDrawerTile<T>>("storage_upgrades", 10, 70, getStorageSlotAmount()) {
            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack stack = this.getStackInSlot(slot);
                if (stack.getItem() instanceof StorageUpgradeItem) {
                    int mult = 1;
                    for (int i = 0; i < storageUpgrades.getSlots(); i++) {
                        if (storageUpgrades.getStackInSlot(i).getItem() instanceof StorageUpgradeItem) {
                            if (i == slot) continue;
                            if (mult == 1)
                                mult = ((StorageUpgradeItem) storageUpgrades.getStackInSlot(i).getItem()).getStorageMultiplier();
                            else
                                mult *= ((StorageUpgradeItem) storageUpgrades.getStackInSlot(i).getItem()).getStorageMultiplier();
                        }
                    }
                    for (int i = 0; i < getStorage().getSlots(); i++) {
                        if (getBaseSize(i) * mult < getStorage().getStackInSlot(i).getCount()) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                return super.extractItem(slot, amount, simulate);
            }
        }
                .setInputFilter((stack, integer) -> {
                    if (stack.getItem().equals(FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON).get())) {
                        for (int i = 0; i < getStorage().getSlots(); i++) {
                            if (getStorage().getStackInSlot(i).getCount() > 64) {
                                return false;
                            }
                        }
                    }
                    return stack.getItem() instanceof UpgradeItem && ((UpgradeItem) stack.getItem()).getType() == UpgradeItem.Type.STORAGE;
                })
                .setOnSlotChanged((stack, integer) -> {
                    needsUpgradeCache = true;
                })
                .setSlotLimit(1);
        if (getStorageSlotAmount() > 0) {
            this.addInventory((InventoryComponent<T>) this.storageUpgrades);
        }
        this.addInventory((InventoryComponent<T>) (this.utilityUpgrades = new InventoryComponent<ControllableDrawerTile<T>>("utility_upgrades", 114, 70, 3)
                        .setInputFilter((stack, integer) -> stack.getItem() instanceof UpgradeItem && ((UpgradeItem) stack.getItem()).getType() == UpgradeItem.Type.UTILITY)
                        .setSlotLimit(1)
                        .setOnSlotChanged((itemStack, integer) -> {
                            needsUpgradeCache = true;
                            if (controllerPos != null && this.level.getBlockEntity(controllerPos) instanceof DrawerControllerTile controllerTile) {
                                controllerTile.getConnectedDrawers().rebuild();
                            }
                        })
                )
        );

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initClient() {
        super.initClient();
        if (getStorageSlotAmount() > 0) {
            addGuiAddonFactory(() -> new TextScreenAddon("Storage", 10, 59, false, ChatFormatting.DARK_GRAY.getColor()) {
                @Override
                public String getText() {
                    return new TranslatableComponent("key.categories.storage").getString();
                }
            });
        }
        addGuiAddonFactory(() -> new TextScreenAddon("Utility", 114, 59, false, ChatFormatting.DARK_GRAY.getColor()) {
            @Override
            public String getText() {
                return new TranslatableComponent("key.categories.utility").getString();
            }
        });
        addGuiAddonFactory(() -> new TextScreenAddon("key.categories.inventory", 8, 92, false, ChatFormatting.DARK_GRAY.getColor()) {
            @Override
            public String getText() {
                return new TranslatableComponent("key.categories.inventory").getString();
            }
        });
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        if (level.getGameTime() % 20 == 0) {
            for (int i = 0; i < this.utilityUpgrades.getSlots(); i++) {
                ItemStack stack = this.utilityUpgrades.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Item item = stack.getItem();
                    if (item.equals(FunctionalStorage.REDSTONE_UPGRADE.get())) {
                        level.updateNeighborsAt(this.getBlockPos(), this.getBasicTileBlock());
                        break;
                    }
                }
            }
        }
        if (level.getGameTime() % 4 == 0) {
            for (int i = 0; i < this.utilityUpgrades.getSlots(); i++) {
                ItemStack stack = this.utilityUpgrades.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Item item = stack.getItem();
                    if (item.equals(FunctionalStorage.PULLING_UPGRADE.get())) {
                        Direction direction = UpgradeItem.getDirection(stack);
                        TileUtil.getTileEntity(level, pos.relative(direction)).ifPresent(blockEntity1 -> {
                            blockEntity1.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).ifPresent(iItemHandler -> {
                                for (int otherSlot = 0; otherSlot < iItemHandler.getSlots(); otherSlot++) {
                                    ItemStack pulledStack = iItemHandler.extractItem(otherSlot, 2, true);
                                    if (pulledStack.isEmpty()) continue;
                                    boolean hasWorked = false;
                                    for (int ourSlot = 0; ourSlot < this.getStorage().getSlots(); ourSlot++) {
                                        ItemStack simulated = getStorage().insertItem(ourSlot, pulledStack, true);
                                        if (simulated.getCount() != pulledStack.getCount()) {
                                            getStorage().insertItem(ourSlot, iItemHandler.extractItem(otherSlot, pulledStack.getCount() - simulated.getCount(), false), false);
                                            hasWorked = true;
                                            break;
                                        }
                                    }
                                    if (hasWorked) break;
                                }
                            });
                        });
                    }
                    if (item.equals(FunctionalStorage.PUSHING_UPGRADE.get())) {
                        Direction direction = UpgradeItem.getDirection(stack);
                        TileUtil.getTileEntity(level, pos.relative(direction)).ifPresent(blockEntity1 -> {
                            blockEntity1.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).ifPresent(otherHandler -> {
                                for (int otherSlot = 0; otherSlot < getStorage().getSlots(); otherSlot++) {
                                    ItemStack pulledStack = getStorage().extractItem(otherSlot, 2, true);
                                    if (pulledStack.isEmpty()) continue;
                                    boolean hasWorked = false;
                                    for (int ourSlot = 0; ourSlot < otherHandler.getSlots(); ourSlot++) {
                                        ItemStack simulated = otherHandler.insertItem(ourSlot, pulledStack, true);
                                        if (simulated.getCount() != pulledStack.getCount()) {
                                            otherHandler.insertItem(ourSlot, getStorage().extractItem(otherSlot, pulledStack.getCount() - simulated.getCount(), false), false);
                                            hasWorked = true;
                                            break;
                                        }
                                    }
                                    if (hasWorked) break;
                                }
                            });
                        });
                    }
                    if (item.equals(FunctionalStorage.COLLECTOR_UPGRADE.get())) {
                        Direction direction = UpgradeItem.getDirection(stack);
                        AABB box = new AABB(pos.relative(direction));
                        for (ItemEntity entitiesOfClass : level.getEntitiesOfClass(ItemEntity.class, box)) {
                            ItemStack pulledStack = ItemHandlerHelper.copyStackWithSize(entitiesOfClass.getItem(), Math.min(entitiesOfClass.getItem().getCount(), 4));
                            if (pulledStack.isEmpty()) continue;
                            boolean hasWorked = false;
                            for (int ourSlot = 0; ourSlot < this.getStorage().getSlots(); ourSlot++) {
                                ItemStack simulated = getStorage().insertItem(ourSlot, pulledStack, true);
                                if (simulated.getCount() != pulledStack.getCount()) {
                                    getStorage().insertItem(ourSlot, ItemHandlerHelper.copyStackWithSize(entitiesOfClass.getItem(), pulledStack.getCount() - simulated.getCount()), false);
                                    entitiesOfClass.getItem().shrink(pulledStack.getCount() - simulated.getCount());
                                    hasWorked = true;
                                    break;
                                }
                            }
                            if (hasWorked) break;
                        }
                    }
                }
            }
        }
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(BlockPos controllerPos) {
        if (this.controllerPos != null) {
            TileUtil.getTileEntity(getLevel(), this.controllerPos, DrawerControllerTile.class).ifPresent(drawerControllerTile -> {
                drawerControllerTile.addConnectedDrawers(LinkingToolItem.ActionMode.REMOVE, getBlockPos());
            });
        }
        this.controllerPos = controllerPos;
    }

    public int getStorageMultiplier() {
        maybeCacheUpgrades();
        return m_mult;
    }

    public boolean isVoid() {
        maybeCacheUpgrades();
        return m_isVoid;
    }

    public boolean isCreative() {
        maybeCacheUpgrades();
        return m_isCreative;
    }

    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalStorage.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalStorage.LINKING_TOOL.get()))
            return InteractionResult.PASS;
        if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem upgradeItem) {
            if (upgradeItem instanceof StorageUpgradeItem storageUpgradeItem) {
                InventoryComponent component = storageUpgrades;
                for (int i = 0; i < component.getSlots(); i++) {
                    if (component.getStackInSlot(i).isEmpty()) {
                        playerIn.setItemInHand(hand, component.insertItem(i, stack, false));
                        return InteractionResult.SUCCESS;
                    }
                }
                for (int i = 0; i < component.getSlots(); i++) {
                    if (!component.getStackInSlot(i).isEmpty() && component.getStackInSlot(i).getItem() instanceof StorageUpgradeItem instertedUpgrade && instertedUpgrade.getStorageMultiplier() < storageUpgradeItem.getStorageMultiplier()) {
                        ItemHandlerHelper.giveItemToPlayer(playerIn, component.getStackInSlot(i).copy());
                        component.setStackInSlot(i, ItemStack.EMPTY);
                        playerIn.setItemInHand(hand, component.insertItem(i, stack, false));
                        return InteractionResult.SUCCESS;
                    }
                }
            } else {
                InventoryComponent component = utilityUpgrades;
                for (int i = 0; i < component.getSlots(); i++) {
                    if (component.getStackInSlot(i).isEmpty()) {
                        playerIn.setItemInHand(hand, component.insertItem(i, stack, false));
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        if (super.onActivated(playerIn, hand, facing, hitX, hitY, hitZ) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }
        if (slot == -1) {
            openGui(playerIn);
        } else if (isServer()) {
            if (!stack.isEmpty() && getStorage().insertItem(slot, stack, true).getCount() != stack.getCount()) {
                playerIn.setItemInHand(hand, getStorage().insertItem(slot, stack, false));
                return InteractionResult.SUCCESS;
            } else if (System.currentTimeMillis() - INTERACTION_LOGGER.getOrDefault(playerIn.getUUID(), System.currentTimeMillis()) < 300) {
                for (ItemStack itemStack : playerIn.getInventory().items) {
                    if (!itemStack.isEmpty() && getStorage().insertItem(slot, itemStack, true).getCount() != itemStack.getCount()) {
                        itemStack.setCount(getStorage().insertItem(slot, itemStack.copy(), false).getCount());
                    }
                }
            }
            INTERACTION_LOGGER.put(playerIn.getUUID(), System.currentTimeMillis());
        }
        return InteractionResult.SUCCESS;
    }

    public abstract int getStorageSlotAmount();

    public void onClicked(Player playerIn, int slot) {
        if (isServer() && slot != -1) {
            HitResult rayTraceResult = RayTraceUtils.rayTraceSimple(this.level, playerIn, 16, 0);
            if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockResult = (BlockHitResult) rayTraceResult;
                Direction facing = blockResult.getDirection();
                if (facing.equals(this.getFacingDirection())) {
                    ItemHandlerHelper.giveItemToPlayer(playerIn, getStorage().extractItem(slot, playerIn.isShiftKeyDown() ? getStorage().getStackInSlot(slot).getMaxStackSize() : 1, false));
                }
            }
        }
    }

    public abstract IItemHandler getStorage();

    public abstract LazyOptional<IItemHandler> getOptional();

    public abstract int getBaseSize(int lost);

    private void maybeCacheUpgrades() {
        if (needsUpgradeCache) {
            m_isCreative = false;
            m_hasDowngrade = false;
            m_mult = 1;
            for (int i = 0; i < storageUpgrades.getSlots(); i++) {
                Item upgrade = storageUpgrades.getStackInSlot(i).getItem();
                if (upgrade.equals(FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON).get())) {
                    m_hasDowngrade = true;
                }
                if (upgrade.equals(FunctionalStorage.CREATIVE_UPGRADE.get())) {
                    m_isCreative = true;
                }
                if (upgrade instanceof StorageUpgradeItem) {
                    m_mult *= ((StorageUpgradeItem) upgrade).getStorageMultiplier();
                }
            }
            m_isVoid = false;
            for (int i = 0; i < utilityUpgrades.getSlots(); i++) {
                if (utilityUpgrades.getStackInSlot(i).getItem().equals(FunctionalStorage.VOID_UPGRADE.get())) {
                    m_isVoid = true;
                }
            }
            needsUpgradeCache = false;
        }
    }

    public boolean hasDowngrade() {
        maybeCacheUpgrades();
        return m_hasDowngrade;
    }

    public void toggleLocking() {
        setLocked(!this.isLocked());
    }

    public boolean isLocked() {
        return this.getBlockState().hasProperty(DrawerBlock.LOCKED) && this.getBlockState().getValue(DrawerBlock.LOCKED);
    }

    public void setLocked(boolean locked) {
        if (this.getBlockState().hasProperty(DrawerBlock.LOCKED)) {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(DrawerBlock.LOCKED, locked), 3);
        }
    }

    public void toggleOption(ConfigurationToolItem.ConfigurationAction action) {
        this.drawerOptions.setActive(action, !this.drawerOptions.isActive(action));
        markForUpdate();
    }

    public DrawerOptions getDrawerOptions() {
        return drawerOptions;
    }


    public InventoryComponent<ControllableDrawerTile<T>> getUtilityUpgrades() {
        return utilityUpgrades;
    }

    public InventoryComponent<ControllableDrawerTile<T>> getStorageUpgrades() {
        return storageUpgrades;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        getOptional().invalidate();
    }

    public boolean isEverythingEmpty() {
        for (int i = 0; i < getStorage().getSlots(); i++) {
            if (!getStorage().getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        for (int i = 0; i < getStorageUpgrades().getSlots(); i++) {
            if (!getStorageUpgrades().getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        for (int i = 0; i < getUtilityUpgrades().getSlots(); i++) {
            if (!getUtilityUpgrades().getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static class DrawerOptions implements INBTSerializable<CompoundTag> {

        public HashMap<ConfigurationToolItem.ConfigurationAction, Boolean> options;

        public DrawerOptions() {
            this.options = new HashMap<>();
            this.options.put(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS, true);
            this.options.put(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER, true);
            this.options.put(ConfigurationToolItem.ConfigurationAction.TOGGLE_UPGRADES, true);
        }

        public boolean isActive(ConfigurationToolItem.ConfigurationAction configurationAction) {
            return options.getOrDefault(configurationAction, true);
        }

        public void setActive(ConfigurationToolItem.ConfigurationAction configurationAction, boolean active) {
            this.options.put(configurationAction, active);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag compoundTag = new CompoundTag();
            for (ConfigurationToolItem.ConfigurationAction action : this.options.keySet()) {
                compoundTag.putBoolean(action.name(), this.options.get(action));
            }
            return compoundTag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            for (String allKey : nbt.getAllKeys()) {
                this.options.put(ConfigurationToolItem.ConfigurationAction.valueOf(allKey), nbt.getBoolean(allKey));
            }
        }
    }
}
