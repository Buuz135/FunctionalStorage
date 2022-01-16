package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.FunctionalStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class BigInventoryHandler implements IItemHandler, INBTSerializable<CompoundTag> {

    public static String BIG_ITEMS = "BigItems";
    public static String STACK = "Stack";
    public static String AMOUNT = "Amount";

    private final FunctionalStorage.DrawerType type;
    private List<BigStack> storedStacks;

    public BigInventoryHandler(FunctionalStorage.DrawerType type){
        this.type = type;
        this.storedStacks = new ArrayList<>();
        for (int i = 0; i < type.getSlots(); i++) {
            this.storedStacks.add(i, new BigStack(ItemStack.EMPTY, 0));
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
            if (!simulate){
                if (bigStack.getStack().isEmpty()) bigStack.setStack(stack);
                bigStack.setAmount(Math.min(bigStack.getAmount() + inserted, getSlotLimit(slot)));
                onChange();
            }
            if (inserted == stack.getCount() || isVoid()) return ItemStack.EMPTY;
            return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - inserted);
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        if (slot < type.getSlots()){
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

    @Override
    public int getSlotLimit(int slot) {
        double stackSize = 1;
        if (!getStoredStacks().get(slot).getStack().isEmpty()) {
            stackSize = getStoredStacks().get(slot).getStack().getMaxStackSize() / 64D;
        }
        if (hasDowngrade()) return (int) Math.floor(64 * stackSize);
        return (int) Math.floor(Math.min(Integer.MAX_VALUE, type.getSlotAmount() * (long) getMultiplier()) * stackSize);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return !stack.isEmpty();
    }

    private boolean isValid(int slot, @Nonnull ItemStack stack){
        if (slot < type.getSlots()){
            BigStack bigStack = this.storedStacks.get(slot);
            ItemStack fl = bigStack.getStack();
            return fl.isEmpty() || (fl.sameItem(stack) && ItemStack.tagMatches(fl, stack));
        }
        return false;
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

    public abstract void onChange();

    public abstract int getMultiplier();

    public abstract boolean isVoid();

    public abstract boolean hasDowngrade();

    public abstract boolean isLocked();

    public List<BigStack> getStoredStacks() {
        return storedStacks;
    }

    public static class BigStack {

        private ItemStack stack;
        private int amount;

        public BigStack(ItemStack stack, int amount) {
            this.stack = stack.copy();
            this.amount = amount;
        }

        public ItemStack getStack() {
            return stack;
        }

        public void setStack(ItemStack stack) {
            this.stack = stack.copy();
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }
}
