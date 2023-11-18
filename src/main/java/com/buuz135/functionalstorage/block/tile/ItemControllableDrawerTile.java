package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public abstract class ItemControllableDrawerTile<T extends ItemControllableDrawerTile<T>> extends ControllableDrawerTile<T> {

    private static HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();
    private int removeTicks = 0;

    public ItemControllableDrawerTile(BasicTileBlock<T> base, BlockEntityType<T> entityType, BlockPos pos, BlockState state) {
        super(base, entityType, pos, state);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initClient() {
        super.initClient();
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        this.removeTicks = Math.max(this.removeTicks - 1, 0);
        if (level.getGameTime() % FunctionalStorageConfig.UPGRADE_TICK == 0) {
            if (getUtilitySlotAmount() > 0){
                for (int i = 0; i < this.getUtilityUpgrades().getSlots(); i++) {
                    ItemStack stack = this.getUtilityUpgrades().getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Item item = stack.getItem();
                        if (item.equals(FunctionalStorage.PULLING_UPGRADE.get())) {
                            Direction direction = UpgradeItem.getDirection(stack);
                            TileUtil.getTileEntity(level, pos.relative(direction)).ifPresent(blockEntity1 -> {
                                blockEntity1.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).ifPresent(iItemHandler -> {
                                    for (int otherSlot = 0; otherSlot < iItemHandler.getSlots(); otherSlot++) {
                                        ItemStack pulledStack = iItemHandler.extractItem(otherSlot, FunctionalStorageConfig.UPGRADE_PULL_ITEMS, true);
                                        if (pulledStack.isEmpty()) continue;
                                        boolean hasWorked = false;
                                        for (int ourSlot = 0; ourSlot < this.getStorage().getSlots(); ourSlot++) {
                                            ItemStack simulated = getStorage().insertItem(ourSlot, pulledStack, true);
                                            if (!simulated.equals(pulledStack)) {
                                                ItemStack extracted = iItemHandler.extractItem(otherSlot, pulledStack.getCount() - simulated.getCount(), false);
                                                getStorage().insertItem(ourSlot, extracted, false);
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
                                blockEntity1.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).ifPresent(otherHandler -> {
                                    for (int drawerSlot = 0; drawerSlot < getStorage().getSlots(); drawerSlot++) {
                                        ItemStack pulledStack = getStorage().extractItem(drawerSlot, FunctionalStorageConfig.UPGRADE_PUSH_ITEMS, true);
                                        if (pulledStack.isEmpty()) continue;
                                        boolean hasWorked = false;
                                        for (int destinationSlot = 0; destinationSlot < otherHandler.getSlots(); destinationSlot++) {
                                            if (otherHandler.getStackInSlot(destinationSlot).getCount() >= otherHandler.getSlotLimit(destinationSlot))
                                                continue;
                                            ItemStack simulated = otherHandler.insertItem(destinationSlot, pulledStack, true);
                                            if (simulated.getCount() <= pulledStack.getCount()) {
                                                otherHandler.insertItem(destinationSlot, getStorage().extractItem(drawerSlot, pulledStack.getCount() - simulated.getCount(), false), false);
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
                                ItemStack pulledStack = ItemHandlerHelper.copyStackWithSize(entitiesOfClass.getItem(), Math.min(entitiesOfClass.getItem().getCount(), FunctionalStorageConfig.UPGRADE_COLLECTOR_ITEMS));
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
    }

    @Override
    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (super.onActivated(playerIn, hand, facing, hitX, hitY, hitZ) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }
        if (slot != -1 && isServer()) {
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
        if (super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    public abstract int getStorageSlotAmount();

    public void onClicked(Player playerIn, int slot) {
        if (isServer() && slot != -1 && this.removeTicks == 0) {
            this.removeTicks = 3;
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

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        getOptional().invalidate();
    }

    @Override
    public InventoryComponent<ControllableDrawerTile<T>> getStorageUpgradesConstructor() {
        return new InventoryComponent<ControllableDrawerTile<T>>("storage_upgrades", 10, 70, getStorageSlotAmount()) {
            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack stack = this.getStackInSlot(slot);
                if (stack.getItem() instanceof StorageUpgradeItem) {
                    int mult = 1;
                    for (int i = 0; i < getStorageUpgrades().getSlots(); i++) {
                        if (getStorageUpgrades().getStackInSlot(i).getItem() instanceof StorageUpgradeItem) {
                            if (i == slot) continue;
                            if (mult == 1)
                                mult = ((StorageUpgradeItem) getStorageUpgrades().getStackInSlot(i).getItem()).getStorageMultiplier();
                            else
                                mult *= ((StorageUpgradeItem) getStorageUpgrades().getStackInSlot(i).getItem()).getStorageMultiplier();
                        }
                    }
                    for (int i = 0; i < getStorage().getSlots(); i++) {
                        if (getStorage().getStackInSlot(i).isEmpty()) continue;
                        double stackSize = getStorage().getStackInSlot(i).getMaxStackSize() / 64D;
                        if ((int) Math.floor(Math.min(Integer.MAX_VALUE, getBaseSize(i) * (long) mult) * stackSize) < getStorage().getStackInSlot(i).getCount()) {
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
                    setNeedsUpgradeCache(true);
                })
                .setSlotLimit(1);
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

    @Override
    public int getTitleColor() {
        return ChatFormatting.DARK_GRAY.getColor();
    }

}
