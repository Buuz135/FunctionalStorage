package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.util.CompactingUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class CompactingInventoryHandler implements IItemHandler, INBTSerializable<CompoundTag>, ILockable {

    public static String PARENT = "Parent";
    public static String BIG_ITEMS = "BigItems";
    public static String STACK = "Stack";
    public static String AMOUNT = "Amount";

    public int totalAmount;

    private int amount;
    private ItemStack parent;
    private List<CompactingUtil.Result> resultList;
    private int slots;

    public CompactingInventoryHandler(int slots) {
        this.resultList = new ArrayList<>();
        this.slots = slots;
        this.totalAmount = 512;
        for (int i = 0; i < slots - 1; i++) {
            this.totalAmount *= 9;
        }
        for (int i = 0; i < slots; i++) {
            this.resultList.add(i, new CompactingUtil.Result(ItemStack.EMPTY, 1));
        }
        this.parent = ItemStack.EMPTY;
    }

    @Override
    public int getSlots() {
        if (isVoid()) return this.slots + 1;
        return this.slots;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot == this.slots) return ItemStack.EMPTY;
        CompactingUtil.Result bigStack = this.resultList.get(slot);
        ItemStack copied = bigStack.getResult().copy();
        copied.setCount(isCreative() ? Integer.MAX_VALUE : this.amount / bigStack.getNeeded());
        return copied;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (isVoid() && slot == this.slots && isVoidValid(stack) || (isVoidValid(stack) && isCreative()))
            return ItemStack.EMPTY;
        if (isValid(slot, stack)) {
            CompactingUtil.Result result = this.resultList.get(slot);
            int inserted = Math.min(getSlotLimit(slot) * result.getNeeded() - amount, stack.getCount() * result.getNeeded());
            inserted = (int) (Math.floor(inserted / result.getNeeded()) * result.getNeeded());
            if (!simulate) {
                this.amount = Math.min(this.amount + inserted, totalAmount * getMultiplier());
                onChange();
            }
            if (inserted == stack.getCount() * result.getNeeded() || isVoid()) return ItemStack.EMPTY;
            return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - inserted / result.getNeeded());

        }
        return stack;
    }

    private boolean isVoidValid(ItemStack stack) {
        for (CompactingUtil.Result result : this.resultList) {
            if (result.getResult().sameItem(stack) && ItemStack.tagMatches(result.getResult(), stack)) return true;
        }
        return false;
    }

    public boolean isSetup(){
        return !this.resultList.get(this.resultList.size() -1).getResult().isEmpty();
    }

    public void setup(CompactingUtil compactingUtil){
        this.resultList = compactingUtil.getResults();
        this.parent = compactingUtil.getResults().get(0).getResult();
        if (this.parent.isEmpty()) {
            this.parent = compactingUtil.getResults().get(1).getResult();
        }
        if (this.parent.isEmpty() && compactingUtil.getResults().size() >= 3) {
            this.parent = compactingUtil.getResults().get(2).getResult();
        }
        onChange();
    }

    public void reset(){
        if (isLocked()) return;
        this.resultList.forEach(result -> {
            result.setResult(ItemStack.EMPTY);
            result.setNeeded(1);
        });
    }

    public int getAmount() {
        return amount;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0 || slot == this.slots) return ItemStack.EMPTY;
        if (slot < this.slots) {
            CompactingUtil.Result bigStack = this.resultList.get(slot);
            if (bigStack.getResult().isEmpty()) return ItemStack.EMPTY;
            int stackAmount = bigStack.getNeeded() * amount;
            if (stackAmount >= this.amount) {
                ItemStack out = bigStack.getResult().copy();
                int newAmount = (int) Math.floor(this.amount / bigStack.getNeeded());
                if (!simulate && !isCreative()) {
                    this.amount -= (newAmount * bigStack.getNeeded());
                    if (this.amount == 0) reset();
                    onChange();
                }
                out.setCount(newAmount);
                return out;
            } else {
                if (!simulate && !isCreative()) {
                    this.amount -= stackAmount;
                    onChange();
                }
                return ItemHandlerHelper.copyStackWithSize(bigStack.getResult(), amount);
            }


        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (isCreative()) return Integer.MAX_VALUE;
        if (slot == this.slots) return Integer.MAX_VALUE;
        int total = totalAmount;
        if (hasDowngrade()) total = 64 * 9 * 9;
        return (int) Math.min(Integer.MAX_VALUE, Math.floor((total * getMultiplier()) / this.resultList.get(slot).getNeeded()));
    }

    public int getSlotLimitBase(int slot) {
        if (slot == this.slots) return Integer.MAX_VALUE;
        int total = totalAmount;
        if (hasDowngrade()) total = 64 * 9 * 9;
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(total / this.resultList.get(slot).getNeeded()));
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return isSetup() && !stack.isEmpty();
    }

    private boolean isValid(int slot, @Nonnull ItemStack stack){
        if (slot < this.slots) {
            CompactingUtil.Result bigStack = this.resultList.get(slot);
            ItemStack fl = bigStack.getResult();
            return !fl.isEmpty() && fl.sameItem(stack) && ItemStack.tagMatches(fl, stack);
        }
        return false;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put(PARENT, this.getParent().serializeNBT());
        compoundTag.putInt(AMOUNT, this.amount);
        CompoundTag items = new CompoundTag();
        for (int i = 0; i < this.resultList.size(); i++) {
            CompoundTag bigStack = new CompoundTag();
            bigStack.put(STACK, this.resultList.get(i).getResult().serializeNBT());
            bigStack.putInt(AMOUNT, this.resultList.get(i).getNeeded());
            items.put(i + "", bigStack);
        }
        compoundTag.put(BIG_ITEMS, items);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.parent = ItemStack.of(nbt.getCompound(PARENT));
        this.amount = nbt.getInt(AMOUNT);
        for (String allKey : nbt.getCompound(BIG_ITEMS).getAllKeys()) {
            this.resultList.get(Integer.parseInt(allKey)).setResult(ItemStack.of(nbt.getCompound(BIG_ITEMS).getCompound(allKey).getCompound(STACK)));
            this.resultList.get(Integer.parseInt(allKey)).setNeeded(Math.max(1, nbt.getCompound(BIG_ITEMS).getCompound(allKey).getInt(AMOUNT)));
        }
    }

    public abstract void onChange();

    public abstract int getMultiplier();

    public abstract boolean isVoid();

    public List<CompactingUtil.Result> getResultList() {
        return resultList;
    }

    public ItemStack getParent() {
        return parent;
    }

    public abstract boolean hasDowngrade();

    public abstract boolean isCreative();
}
