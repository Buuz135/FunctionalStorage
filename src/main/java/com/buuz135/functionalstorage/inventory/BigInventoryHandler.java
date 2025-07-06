package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class BigInventoryHandler implements IItemHandler, INBTSerializable<CompoundTag>, ILockable {

    public static String BIG_ITEMS = "BigItems";
    public static String STACK = "Stack";
    public static String AMOUNT = "Amount";

    private final FunctionalStorage.DrawerType type;
    private final List<BigStack> storedStacks;

    public BigInventoryHandler(FunctionalStorage.DrawerType type) {
        this.type = type;
        this.storedStacks = new ArrayList<>();
        for (int i = 0; i < type.getSlots(); i++) {
            this.storedStacks.add(i, new BigStack(ItemStack.EMPTY, 0));
        }
    }

    @Override
    public int getSlots() {
        if (isVoid()) return type.getSlots() + 1;
        return type.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (type.getSlots() == slot) return ItemStack.EMPTY;
        BigStack bigStack = this.storedStacks.get(slot);
        if (isCreative()) {
            return bigStack.slotStack.copyWithCount(Integer.MAX_VALUE);
        }
        return bigStack.slotStack;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (isVoid() && type.getSlots() == slot && isVoidValid(stack) || (isVoidValid(stack) && isCreative()))
            return ItemStack.EMPTY;
        if (isValid(slot, stack)) {
            BigStack bigStack = this.storedStacks.get(slot);
            int inserted = Math.min(getSlotLimit(slot) - bigStack.getAmount(), stack.getCount());
            if (!simulate) {
                if (bigStack.getStack().isEmpty())
                    bigStack.setStack(stack.copyWithCount(stack.getMaxStackSize()));
                bigStack.setAmount(Math.min(bigStack.getAmount() + inserted, getSlotLimit(slot)));
                onChange();
            }
            if (inserted == stack.getCount() || isVoid()) return ItemStack.EMPTY;
            return stack.copyWithCount(stack.getCount() - inserted);
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0 || type.getSlots() == slot) return ItemStack.EMPTY;
        if (slot < type.getSlots()){
            BigStack bigStack = this.storedStacks.get(slot);
            if (bigStack.getStack().isEmpty()) return ItemStack.EMPTY;
            if (!isCreative() && bigStack.getAmount() <= amount) {
                ItemStack out = bigStack.getStack().copy();
                int newAmount = bigStack.getAmount();
                if (!simulate && !isCreative()) {
                    if (!isLocked()) bigStack.setStack(ItemStack.EMPTY);
                    bigStack.setAmount(0);
                    onChange();
                }
                out.setCount(newAmount);
                return out;
            } else {
                if (!simulate && !isCreative()) {
                    bigStack.setAmount(bigStack.getAmount() - amount);
                    onChange();
                }
                return bigStack.getStack().copyWithCount(amount);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (isCreative()) return Integer.MAX_VALUE;
        if (type.getSlots() == slot) return Integer.MAX_VALUE;
        double stackSize = 1;
        if (!getStoredStacks().get(slot).getStack().isEmpty()) {
            stackSize = getStoredStacks().get(slot).getStack().getMaxStackSize() / 64D;
        }
        return (int) Math.floor(getTotalAmount() * stackSize);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return !stack.isEmpty();
    }

    private boolean isValid(int slot, @Nonnull ItemStack stack){
        if (slot < type.getSlots()){
            BigStack bigStack = this.storedStacks.get(slot);
            ItemStack fl = bigStack.getStack();
            if (isLocked() && fl.isEmpty()) return false;
            return fl.isEmpty() || (ItemStack.isSameItemSameComponents(fl, stack));
        }
        return false;
    }

    private boolean isVoidValid(ItemStack stack){
        for (BigStack storedStack : this.storedStacks) {
            if (ItemStack.isSameItemSameComponents(storedStack.getStack(), stack)) return true;
        }
        return false;
    }

    @Override
    public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag items = new CompoundTag();
        for (int i = 0; i < this.storedStacks.size(); i++) {
            CompoundTag bigStack = new CompoundTag();
            bigStack.put(STACK, this.storedStacks.get(i).getStack().saveOptional(provider));
            bigStack.putInt(AMOUNT, this.storedStacks.get(i).getAmount());
            items.put(i + "", bigStack);
        }
        compoundTag.put(BIG_ITEMS, items);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, CompoundTag nbt) {
        for (String allKey : nbt.getCompound(BIG_ITEMS).getAllKeys()) {
            this.storedStacks.get(Integer.parseInt(allKey)).setStack(Utils.deserialize(provider, nbt.getCompound(BIG_ITEMS).getCompound(allKey).getCompound(STACK)));
            this.storedStacks.get(Integer.parseInt(allKey)).setAmount(nbt.getCompound(BIG_ITEMS).getCompound(allKey).getInt(AMOUNT));
        }
    }

    public abstract void onChange();

    public abstract float getMultiplier();

    public double getTotalAmount() {
        return 64d * getMultiplier();
    }

    public abstract boolean isVoid();

    public abstract boolean isLocked();

    public abstract boolean isCreative();

    public List<BigStack> getStoredStacks() {
        return storedStacks;
    }

    public static class BigStack {

        private ItemStack stack;
        private ItemStack slotStack;
        private int amount;

        public BigStack(ItemStack stack, int amount) {
            this.stack = stack.copy();
            this.amount = amount;
            this.slotStack = stack.copyWithCount(amount);
        }

        public ItemStack getStack() {
            return stack;
        }

        public void setStack(ItemStack stack) {
            this.stack = stack.copy();
            this.slotStack = stack.copyWithCount(amount);
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
            this.slotStack.setCount(amount);
        }
    }
}
