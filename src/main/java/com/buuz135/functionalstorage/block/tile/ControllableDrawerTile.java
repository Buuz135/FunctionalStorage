package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.DrawerBlock;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.item.component.SizeProvider;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.block.tile.ActiveTile;
import com.hrznstudio.titanium.client.screen.addon.TextScreenAddon;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.HashMap;
import java.util.function.Supplier;

public abstract class ControllableDrawerTile<T extends ControllableDrawerTile<T>> extends ActiveTile<T> {

    private boolean needsUpgradeCache = true;

    @Save
    private BlockPos controllerPos;
    // We do not save this automatically so that we can read it first
    private InventoryComponent<ControllableDrawerTile<T>> storageUpgrades;
    @Save
    private InventoryComponent<ControllableDrawerTile<T>> utilityUpgrades;
    @Save
    private DrawerOptions drawerOptions;
    @Save
    private boolean isCreative = false;
    @Save
    private boolean isVoid = false;
    @Save
    private boolean isStorageUpgradeLocked = false;

    public final Supplier<DataComponentType<SizeProvider>> sizeUpgradeComponent;
    @Save
    protected int baseSize;
    private int storageSize;

    public ControllableDrawerTile(BasicTileBlock<T> base, BlockEntityType<T> entityType, BlockPos pos, BlockState state, DrawerProperties props) {
        super(base, entityType, pos, state);
        this.drawerOptions = new DrawerOptions();
        this.storageUpgrades = getStorageUpgradesConstructor();
        if (getStorageSlotAmount() > 0) {
            this.addInventory((InventoryComponent<T>) this.storageUpgrades);
        }
        if (getUtilitySlotAmount() > 0){
            this.addInventory((InventoryComponent<T>) (this.utilityUpgrades = new InventoryComponent<ControllableDrawerTile<T>>("utility_upgrades", 114, 70, getUtilitySlotAmount())
                            .setInputFilter((stack, integer) -> {
                                var incompatIn = stack.get(FSAttachments.INCOMPATIBLE_UPGRADES);
                                for (int i = 0; i < utilityUpgrades.getSlots(); i++) {
                                    var upgrade = utilityUpgrades.getStackInSlot(i);
                                    var incompatOnUpgrade = upgrade.get(FSAttachments.INCOMPATIBLE_UPGRADES);
                                    if ((incompatIn != null && upgrade.is(incompatIn)) || (incompatOnUpgrade != null && stack.is(incompatOnUpgrade))) {
                                        return false;
                                    }
                                }
                                return (stack.getItem() instanceof UpgradeItem && ((UpgradeItem) stack.getItem()).getType() == UpgradeItem.Type.UTILITY) || stack.has(FSAttachments.FUNCTIONAL_BEHAVIOR);
                            })
                            .setSlotLimit(1)
                            .setOnSlotChanged((itemStack, integer) -> {
                                needsUpgradeCache = true;
                                if (controllerPos != null) {
                                    if(this.level.getBlockEntity(controllerPos) instanceof StorageControllerTile controllerTile)
                                        controllerTile.getConnectedDrawers().rebuild();
                                }
                            })
                    )
            );
        }

        this.baseSize = props.baseSize();
        this.sizeUpgradeComponent = props.upgradeComponent();
    }

    @Override
    public void loadAdditional(CompoundTag compound, HolderLookup.Provider provider) {
        if (compound.contains("storageUpgrades")) {
            storageUpgrades.deserializeNBT(provider, compound.getCompound("storageUpgrades"));
            recalculateUpgrades();
        }
        super.loadAdditional(compound, provider);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.put("storageUpgrades", storageUpgrades.serializeNBT(provider));
        super.saveAdditional(compoundTag, provider);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initClient() {
        super.initClient();
        if (getStorageSlotAmount() > 0) {
            addGuiAddonFactory(() -> new TextScreenAddon("gui.functionalstorage.storage", 10, 59, false, ChatFormatting.DARK_GRAY.getColor()) {
                @Override
                public String getText() {
                    return Component.translatable("key.categories.storage").getString();
                }
            });
        }
        if (getUtilitySlotAmount() > 0){
            addGuiAddonFactory(() -> new TextScreenAddon("gui.functionalstorage.utility", 114, 59, false, ChatFormatting.DARK_GRAY.getColor()) {
                @Override
                public String getText() {
                    return Component.translatable("key.categories.utility").getString();
                }
            });
        }
        addGuiAddonFactory(() -> new TextScreenAddon("key.categories.inventory", 8, 92, false, ChatFormatting.DARK_GRAY.getColor()) {
            @Override
            public String getText() {
                return Component.translatable("key.categories.inventory").getString();
            }
        });
    }

    @Override
    public Direction getFacingDirection() {
        return this.level.getBlockState(this.worldPosition).hasProperty(DrawerBlock.FACING_HORIZONTAL_CUSTOM) ? (Direction)this.level.getBlockState(this.worldPosition).getValue(DrawerBlock.FACING_HORIZONTAL_CUSTOM) : (this.level.getBlockState(this.worldPosition).hasProperty(RotatableBlock.FACING_HORIZONTAL) ? (Direction)this.level.getBlockState(this.worldPosition).getValue(RotatableBlock.FACING_HORIZONTAL) : Direction.NORTH);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        if (getUtilitySlotAmount() > 0){
            for (int i = 0; i < this.utilityUpgrades.getSlots(); i++) {
                ItemStack stack = this.utilityUpgrades.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    var comp = stack.get(FSAttachments.FUNCTIONAL_BEHAVIOR);
                    if (comp != null) {
                        comp.work(this.level, getBlockPos(), this, stack, i);
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
            TileUtil.getTileEntity(getLevel(), this.controllerPos, StorageControllerTile.class).ifPresent(drawerControllerTile -> {
                drawerControllerTile.addConnectedDrawers(LinkingToolItem.ActionMode.REMOVE, getBlockPos());
            });
        }
        this.controllerPos = controllerPos;
    }

    public void clearControllerPos()
    {
        this.controllerPos = null;
    }

    public int getStorageMultiplier() {
        maybeCacheUpgrades();
        return storageSize;
    }

    public boolean isVoid() {
        maybeCacheUpgrades();
        return isVoid;
    }

    public boolean isCreative() {
        maybeCacheUpgrades();
        return isCreative;
    }

    public void setNeedsUpgradeCache(boolean needsUpgradeCache) {
        this.needsUpgradeCache = needsUpgradeCache;
    }

    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalStorage.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalStorage.LINKING_TOOL.get()))
            return InteractionResult.PASS;

        var sizeComp = stack.get(sizeUpgradeComponent);
        var funcComp = stack.get(FSAttachments.FUNCTIONAL_BEHAVIOR);

        if (sizeComp != null || stack.is(FunctionalStorage.CREATIVE_UPGRADE.get())) {
            InventoryComponent component = storageUpgrades;
            for (int i = 0; i < component.getSlots(); i++) {
                if (component.getStackInSlot(i).isEmpty() && component.isItemValid(i, stack)) {
                    playerIn.setItemInHand(hand, component.insertItem(i, stack, false));
                    return InteractionResult.SUCCESS;
                }
            }

            if (sizeComp != null) {
                for (int i = 0; i < component.getSlots(); i++) {
                    if (!component.getStackInSlot(i).isEmpty() && component.isItemValid(i, stack) && component.getStackInSlot(i).has(sizeUpgradeComponent) && component.getStackInSlot(i).get(sizeUpgradeComponent).applyFactorModifier(1f) < sizeComp.applyFactorModifier(1f)) {
                        ItemHandlerHelper.giveItemToPlayer(playerIn, component.getStackInSlot(i).copy());
                        ItemStack upgradeStack = stack.copy();
                        upgradeStack.setCount(1);
                        component.setStackInSlot(i, upgradeStack);
                        stack.shrink(1);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        } else if (funcComp != null || (stack.getItem() instanceof UpgradeItem ui && ui.getType() == UpgradeItem.Type.UTILITY)) {
            InventoryComponent component = utilityUpgrades;
            for (int i = 0; i < component.getSlots(); i++) {
                if (component.getStackInSlot(i).isEmpty() && component.isItemValid(i, stack)) {
                    playerIn.setItemInHand(hand, component.insertItem(i, stack, false));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        if (super.onActivated(playerIn, hand, facing, hitX, hitY, hitZ) == ItemInteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }
        if (slot == -1) {
            openGui(playerIn);
        }
        return InteractionResult.SUCCESS;
    }

    public abstract int getStorageSlotAmount();

    public int getUtilitySlotAmount(){
        return 3;
    }

    public void onClicked(Player playerIn, int slot) {

    }

    private void maybeCacheUpgrades() {
        if (needsUpgradeCache) {
            recalculateUpgrades();
            needsUpgradeCache = false;
        }
    }

    public void recalculateUpgrades() {
        isCreative = false;
        storageSize = SizeProvider.calculate(storageUpgrades, sizeUpgradeComponent, baseSize);
        for (int i = 0; i < storageUpgrades.getSlots(); i++) {
            Item upgrade = storageUpgrades.getStackInSlot(i).getItem();
            if (upgrade.equals(FunctionalStorage.CREATIVE_UPGRADE.get())) {
                isCreative = true;
            }
        }
        isVoid = false;
        if (getUtilitySlotAmount() > 0){
            for (int i = 0; i < utilityUpgrades.getSlots(); i++) {
                if (utilityUpgrades.getStackInSlot(i).getItem().equals(FunctionalStorage.VOID_UPGRADE.get())) {
                    isVoid = true;
                }
            }
        }
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
        if (action.getMax() == 1) {
            this.drawerOptions.setActive(action, !this.drawerOptions.isActive(action));
        } else {
            this.drawerOptions.setAdvancedValue(action, (this.drawerOptions.getAdvancedValue(action) + 1) % (action.getMax() + 1));
        }
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

    public boolean isEverythingEmpty() {
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

    public abstract InventoryComponent<ControllableDrawerTile<T>> getStorageUpgradesConstructor();

    @Override
    public int getTitleColor() {
        return ChatFormatting.DARK_GRAY.getColor();
    }

    public boolean isStorageUpgradeLocked() {
        return isStorageUpgradeLocked;
    }

    public static class DrawerOptions implements INBTSerializable<CompoundTag> {

        public HashMap<ConfigurationToolItem.ConfigurationAction, Boolean> options;
        public HashMap<ConfigurationToolItem.ConfigurationAction, Integer> advancedOptions;

        public DrawerOptions() {
            this.options = new HashMap<>();
            this.options.put(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS, true);
            this.options.put(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER, true);
            this.options.put(ConfigurationToolItem.ConfigurationAction.TOGGLE_UPGRADES, true);
            this.advancedOptions = new HashMap<>();
            this.advancedOptions.put(ConfigurationToolItem.ConfigurationAction.INDICATOR, 0);
        }

        public boolean isActive(ConfigurationToolItem.ConfigurationAction configurationAction) {
            return options.getOrDefault(configurationAction, true);
        }

        public void setActive(ConfigurationToolItem.ConfigurationAction configurationAction, boolean active) {
            this.options.put(configurationAction, active);
        }

        public int getAdvancedValue(ConfigurationToolItem.ConfigurationAction configurationAction) {
            return advancedOptions.getOrDefault(configurationAction, 0);
        }

        public void setAdvancedValue(ConfigurationToolItem.ConfigurationAction configurationAction, int value) {
            this.advancedOptions.put(configurationAction, value);
        }

        @Override
        public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) {
            CompoundTag compoundTag = new CompoundTag();
            for (ConfigurationToolItem.ConfigurationAction action : this.options.keySet()) {
                compoundTag.putBoolean(action.name(), this.options.get(action));
            }
            for (ConfigurationToolItem.ConfigurationAction action : this.advancedOptions.keySet()) {
                compoundTag.putInt("Advanced: " + action.name(), this.advancedOptions.get(action));
            }
            return compoundTag;
        }

        @Override
        public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, CompoundTag nbt) {
            for (String allKey : nbt.getAllKeys()) {
                if (allKey.startsWith("Advanced: ")) {
                    this.advancedOptions.put(ConfigurationToolItem.ConfigurationAction.valueOf(allKey.replace("Advanced: ", "")), nbt.getInt(allKey));
                } else {
                    this.options.put(ConfigurationToolItem.ConfigurationAction.valueOf(allKey), nbt.getBoolean(allKey));
                }
            }
        }
    }
}
