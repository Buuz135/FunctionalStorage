package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.DrawerBlock;
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
import com.mojang.datafixers.types.Func;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public abstract class ControllableDrawerTile<T extends ControllableDrawerTile<T>> extends ActiveTile<T> {

    private static HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();

    @Save
    private BlockPos controllerPos;
    @Save
    private InventoryComponent<ControllableDrawerTile<T>> storageUpgrades;
    @Save
    private InventoryComponent<ControllableDrawerTile<T>> utilityUpgrades;

    public ControllableDrawerTile(BasicTileBlock<T> base, BlockPos pos, BlockState state) {
        super(base, pos, state);
        this.addInventory((InventoryComponent<T>) (this.storageUpgrades = new InventoryComponent<ControllableDrawerTile<T>>("storage_upgrades", 10, 70, getStorageSlotAmount()) {
                    @NotNull
                    @Override
                    public ItemStack extractItem(int slot, int amount, boolean simulate) {
                        ItemStack stack = this.getStackInSlot(slot);
                        if (stack.getItem() instanceof StorageUpgradeItem){
                            int mult = 1;
                            for (int i = 0; i < storageUpgrades.getSlots(); i++) {
                                if (storageUpgrades.getStackInSlot(i).getItem() instanceof StorageUpgradeItem){
                                    if (i == slot) continue;
                                    if (mult == 1) mult = ((StorageUpgradeItem) storageUpgrades.getStackInSlot(i).getItem()).getStorageMultiplier();
                                    else mult *= ((StorageUpgradeItem) storageUpgrades.getStackInSlot(i).getItem()).getStorageMultiplier();
                                }
                            }
                            for (int i = 0; i < getStorage().getSlots(); i++) {
                                if (getBaseSize(i) * mult < getStorage().getStackInSlot(i).getCount()){
                                    return ItemStack.EMPTY;
                                }
                            }
                        }
                        return super.extractItem(slot, amount, simulate);
                    }
                }
                        .setInputFilter((stack, integer) -> {
                            if (stack.getItem().equals(FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON).get())){
                                for (int i = 0; i < getStorage().getSlots(); i++) {
                                    if (getStorage().getStackInSlot(i).getCount() > 64){
                                        return false;
                                    }
                                }
                            }
                            return stack.getItem() instanceof UpgradeItem && ((UpgradeItem) stack.getItem()).getType() == UpgradeItem.Type.STORAGE;
                        })
                .setSlotLimit(1))
        );
        this.addInventory((InventoryComponent<T>) (this.utilityUpgrades = new InventoryComponent<ControllableDrawerTile<T>>("utility_upgrades", 114, 70, 3)
                .setInputFilter((stack, integer) -> stack.getItem() instanceof UpgradeItem && ((UpgradeItem) stack.getItem()).getType() == UpgradeItem.Type.UTILITY)
                .setSlotLimit(1))
        );
        addGuiAddonFactory(() -> new TextScreenAddon("Storage", 10, 59, false, ChatFormatting.DARK_GRAY.getColor()){
            @Override
            public String getText() {
                return  new TranslatableComponent("key.categories.storage").getString();
            }
        });
        addGuiAddonFactory(() -> new TextScreenAddon("Utility", 114, 59, false, ChatFormatting.DARK_GRAY.getColor()){
            @Override
            public String getText() {
                return  new TranslatableComponent("key.categories.utility").getString();
            }
        });
        addGuiAddonFactory(() -> new TextScreenAddon("key.categories.inventory", 8, 92, false, ChatFormatting.DARK_GRAY.getColor()){
            @Override
            public String getText() {
                return  new TranslatableComponent("key.categories.inventory").getString();
            }
        });
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        if (level.getGameTime() % 4 == 0){
            for (int i = 0; i < this.utilityUpgrades.getSlots(); i++) {
                ItemStack stack = this.utilityUpgrades.getStackInSlot(i);
                if (!stack.isEmpty()){
                    Item item = stack.getItem();
                    if (item.equals(FunctionalStorage.PULLING_UPGRADE.get())){
                        Direction direction = Direction.byName(stack.getOrCreateTag().getString("Direction"));
                        TileUtil.getTileEntity(level, pos.relative(direction)).ifPresent(blockEntity1 -> {
                            blockEntity1.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).ifPresent(iItemHandler -> {
                                for (int otherSlot = 0; otherSlot < iItemHandler.getSlots(); otherSlot++) {
                                    ItemStack pulledStack = iItemHandler.extractItem(otherSlot, 2, true);
                                    if (pulledStack.isEmpty()) continue;
                                    boolean hasWorked = false;
                                    for (int ourSlot = 0; ourSlot < this.getStorage().getSlots(); ourSlot++) {
                                        ItemStack simulated = getStorage().insertItem(ourSlot, pulledStack, true);
                                        if (simulated.getCount() != pulledStack.getCount()){
                                            getStorage().insertItem(ourSlot, iItemHandler.extractItem(otherSlot, pulledStack.getCount() - simulated.getCount(), false), false );
                                            hasWorked = true;
                                            break;
                                        }
                                    }
                                    if (hasWorked) break;
                                }
                            });
                        });
                    }
                }
            }
        }
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(BlockPos controllerPos) {
        if (this.controllerPos != null){
            TileUtil.getTileEntity(getLevel(), this.controllerPos, DrawerControllerTile.class).ifPresent(drawerControllerTile -> {
                drawerControllerTile.addConnectedDrawers(LinkingToolItem.ActionMode.REMOVE, getBlockPos());
            });
        }
        this.controllerPos = controllerPos;
    }

    public int getStorageMultiplier(){
        int mult = 1;
        for (int i = 0; i < storageUpgrades.getSlots(); i++) {
            if (storageUpgrades.getStackInSlot(i).getItem() instanceof StorageUpgradeItem){
                if (mult == 1) mult = ((StorageUpgradeItem) storageUpgrades.getStackInSlot(i).getItem()).getStorageMultiplier();
                else mult *= ((StorageUpgradeItem) storageUpgrades.getStackInSlot(i).getItem()).getStorageMultiplier();
            }
        }
        return mult;
    }

    public boolean isVoid(){
        for (int i = 0; i < this.utilityUpgrades.getSlots(); i++) {
            if (this.utilityUpgrades.getStackInSlot(i).getItem().equals(FunctionalStorage.VOID_UPGRADE.get())){
                return true;
            }
        }
        return false;
    }

    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalStorage.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalStorage.LINKING_TOOL.get())) return InteractionResult.PASS;
        if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem){
            InventoryComponent component = ((UpgradeItem) stack.getItem()).getType() == UpgradeItem.Type.STORAGE ? storageUpgrades : utilityUpgrades;
            for (int i = 0; i < component.getSlots(); i++) {
                if (component.getStackInSlot(i).isEmpty()){
                    playerIn.setItemInHand(hand, component.insertItem(i, stack, false));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        if (super.onActivated(playerIn, hand, facing, hitX, hitY, hitZ) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }
        if (slot == -1){
            openGui(playerIn);
        } else if (isServer()){
            if (!stack.isEmpty() && getStorage().isItemValid(slot, stack)) {
                playerIn.setItemInHand(hand, getStorage().insertItem(slot, stack, false));
            } else if (System.currentTimeMillis() - INTERACTION_LOGGER.getOrDefault(playerIn.getUUID(), System.currentTimeMillis()) < 300) {
                for (ItemStack itemStack : playerIn.getInventory().items) {
                    if (!itemStack.isEmpty() && getStorage().insertItem(slot, itemStack, true).isEmpty()) {
                        getStorage().insertItem(slot, itemStack.copy(), false);
                        itemStack.setCount(0);
                    }
                }
            }
            INTERACTION_LOGGER.put(playerIn.getUUID(), System.currentTimeMillis());
        }
        return InteractionResult.SUCCESS;
    }

    public abstract int getStorageSlotAmount();

    public void onClicked(Player playerIn, int slot) {
        if (isServer() && slot != -1){
            HitResult rayTraceResult = RayTraceUtils.rayTraceSimple(this.level, playerIn, 16, 0);
            if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockResult = (BlockHitResult) rayTraceResult;
                Direction facing = blockResult.getDirection();
                if (facing.equals(this.getFacingDirection())){
                    ItemHandlerHelper.giveItemToPlayer(playerIn, getStorage().extractItem(slot, playerIn.isShiftKeyDown() ? 64 : 1, false));
                }
            }
        }
    }

    public abstract IItemHandler getStorage();

    public abstract LazyOptional<IItemHandler> getOptional();

    public abstract int getBaseSize(int lost);

    public boolean hasDowngrade(){
        for (int i = 0; i < this.storageUpgrades.getSlots(); i++) {
            if (storageUpgrades.getStackInSlot(i).getItem().equals(FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON).get())){
                return true;
            }
        }
        return false;
    }

    public void toggleLocking(){
        setLocked(!this.isLocked());
    }

    public boolean isLocked() {
        return this.getBlockState().hasProperty(DrawerBlock.LOCKED) && this.getBlockState().getValue(DrawerBlock.LOCKED);
    }

    public void setLocked(boolean locked) {
        if (this.getBlockState().hasProperty(DrawerBlock.LOCKED) ){
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(DrawerBlock.LOCKED, locked), 3);
        }
    }

    public InventoryComponent<ControllableDrawerTile<T>> getStorageUpgrades() {
        return storageUpgrades;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        getOptional().invalidate();
    }
}
