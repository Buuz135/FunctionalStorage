package com.buuz135.functionalstorage.inventory.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static com.buuz135.functionalstorage.inventory.BigInventoryHandler.*;

public class DrawerStackItemHandler implements IItemHandler, INBTSerializable<CompoundTag> {

    private List<BigInventoryHandler.BigStack> storedStacks;
    private ItemStack stack;
    private FunctionalStorage.DrawerType type;
    private int multiplier;
    private boolean downgrade;
    private boolean isVoid;

    public DrawerStackItemHandler(ItemStack stack, FunctionalStorage.DrawerType drawerType) {
        this.stack = stack;
        this.storedStacks = new ArrayList<>();
        this.type = drawerType;
        this.multiplier = 1;
        this.downgrade = false;
        this.isVoid = false;
        for (int i = 0; i < drawerType.getSlots(); i++) {
            this.storedStacks.add(i, new BigInventoryHandler.BigStack(ItemStack.EMPTY, 0));
        }
        if (stack.hasTag()) {
            deserializeNBT(stack.getTag().getCompound("Tile").getCompound("handler"));
            for (Tag tag : stack.getOrCreateTag().getCompound("Tile").getCompound("storageUpgrades").getList("Items", Tag.TAG_COMPOUND)) {
                ItemStack itemStack = ItemStack.of((CompoundTag) tag);
                if (itemStack.getItem() instanceof StorageUpgradeItem) {
                    if (multiplier == 1) multiplier = ((StorageUpgradeItem) itemStack.getItem()).getStorageMultiplier();
                    else multiplier *= ((StorageUpgradeItem) itemStack.getItem()).getStorageMultiplier();
                }
                if (itemStack.getItem().equals(FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON).get())) {
                    this.downgrade = true;
                }
            }
            for (Tag tag : stack.getOrCreateTag().getCompound("Tile").getCompound("utilityUpgrades").getList("Items", Tag.TAG_COMPOUND)) {
                ItemStack itemStack = ItemStack.of((CompoundTag) tag);
                if (itemStack.getItem().equals(FunctionalStorage.VOID_UPGRADE.get())) {
                    this.isVoid = true;
                }
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag items = new CompoundTag();
        for (int i = 0; i < this.storedStacks.size(); i++) {
            CompoundTag bigStack = new CompoundTag();
            bigStack.put(STACK, this.storedStacks.get(i).getStack().serializeNBT());
            bigStack.putInt(AMOUNT, this.storedStacks.get(i).getAmount());
            items.put(i + "", bigStack);
        }
        compoundTag.put(BIG_ITEMS, items);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (String allKey : nbt.getCompound(BIG_ITEMS).getAllKeys()) {
            this.storedStacks.get(Integer.parseInt(allKey)).setStack(ItemStack.of(nbt.getCompound(BIG_ITEMS).getCompound(allKey).getCompound(STACK)));
            this.storedStacks.get(Integer.parseInt(allKey)).setAmount(nbt.getCompound(BIG_ITEMS).getCompound(allKey).getInt(AMOUNT));
        }
    }

    @Override
    public int getSlots() {
        return type.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        BigStack bigStack = this.storedStacks.get(slot);
        ItemStack copied = bigStack.getStack().copy();
        copied.setCount(bigStack.getAmount());
        return copied;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (isValid(slot, stack)) {
            BigStack bigStack = this.storedStacks.get(slot);
            int inserted = Math.min(getSlotLimit(slot) - bigStack.getAmount(), stack.getCount());
            if (!simulate) {
                bigStack.setStack(stack);
                bigStack.setAmount(Math.min(bigStack.getAmount() + inserted, getSlotLimit(slot)));
                onChange();
            }
            if (inserted == stack.getCount() || isVoid()) return ItemStack.EMPTY;
            return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - inserted);
        }
        return stack;
    }

    private boolean isVoid() {
        return true;
    }

    private void onChange() {
        if (stack.getOrCreateTag().contains("Tile"))
            stack.getOrCreateTag().put("Tile", new CompoundTag());
        stack.getOrCreateTag().getCompound("Tile").put("handler", serializeNBT());
    }

    private boolean isValid(int slot, @Nonnull ItemStack stack) {
        if (slot < type.getSlots()) {
            BigStack bigStack = this.storedStacks.get(slot);
            ItemStack fl = bigStack.getStack();
            return fl.isEmpty() || (fl.sameItem(stack) && ItemStack.tagMatches(fl, stack));
        }
        return false;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        if (slot < type.getSlots()) {
            BigStack bigStack = this.storedStacks.get(slot);
            if (bigStack.getStack().isEmpty()) return ItemStack.EMPTY;
            if (bigStack.getAmount() <= amount) {
                ItemStack out = bigStack.getStack().copy();
                int newAmount = bigStack.getAmount();
                if (!simulate) {
                    if (!isLocked()) bigStack.setStack(ItemStack.EMPTY);
                    bigStack.setAmount(0);
                    onChange();
                }
                out.setCount(newAmount);
                return out;
            } else {
                if (!simulate) {
                    bigStack.setAmount(bigStack.getAmount() - amount);
                    onChange();
                }
                return ItemHandlerHelper.copyStackWithSize(bigStack.getStack(), amount);
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean isLocked() {
        return true;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (hasDowngrade()) return 64;
        return (int) Math.min(Integer.MAX_VALUE, type.getSlotAmount() * (long) getMultiplier());
    }

    private long getMultiplier() {
        return multiplier;
    }

    private boolean hasDowngrade() {
        return downgrade;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return !stack.isEmpty();
    }

    public List<BigStack> getStoredStacks() {
        return storedStacks;
    }
}
