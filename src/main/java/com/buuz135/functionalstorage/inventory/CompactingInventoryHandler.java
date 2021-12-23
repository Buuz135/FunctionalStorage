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

public abstract class CompactingInventoryHandler implements IItemHandler, INBTSerializable<CompoundTag> {

    public static String PARENT = "Parent";
    public static String BIG_ITEMS = "BigItems";
    public static String STACK = "Stack";
    public static String AMOUNT = "Amount";

    public static final int TOTAL_AMOUNT = 512 * 9 * 9;

    private int amount;
    private ItemStack parent;
    private List<CompactingUtil.Result> resultList;

    public CompactingInventoryHandler(){
        this.resultList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            this.resultList.add(i, new CompactingUtil.Result(ItemStack.EMPTY, 1));
        }
        this.parent = ItemStack.EMPTY;
    }

    @Override
    public int getSlots() {
        return 3;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        CompactingUtil.Result bigStack = this.resultList.get(slot);
        ItemStack copied = bigStack.getResult().copy();
        copied.setCount(this.amount / bigStack.getNeeded());
        return copied;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (isItemValid(slot, stack)) {
            CompactingUtil.Result result = this.resultList.get(slot);
            int inserted = Math.min(getSlotLimit(slot) * result.getNeeded() - amount, stack.getCount() * result.getNeeded());
            inserted = (int) (Math.floor(inserted / result.getNeeded()) * result.getNeeded());
            if (!simulate){
                this.amount = Math.min(this.amount + inserted, TOTAL_AMOUNT * getMultiplier());
                onChange();
            }
            if (inserted == stack.getCount() * result.getNeeded() || isVoid()) return ItemStack.EMPTY;
            return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - inserted / result.getNeeded());

        }
        return stack;
    }


    public boolean isSetup(){
        return !this.resultList.get(this.resultList.size() -1).getResult().isEmpty();
    }

    public void setup(CompactingUtil compactingUtil){
        this.resultList = compactingUtil.getResults();
        this.parent = compactingUtil.getResults().get(2).getResult();
        onChange();
    }

    public void reset(){
        if (isLocked()) return;
        this.resultList.forEach(result -> {
            result.setResult(ItemStack.EMPTY);
            result.setNeeded(1);
        });
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        if (slot < 3){
            CompactingUtil.Result bigStack = this.resultList.get(slot);
            if (bigStack.getResult().isEmpty()) return ItemStack.EMPTY;
            int stackAmount = bigStack.getNeeded() * amount;
            if (stackAmount >= this.amount) {
                ItemStack out = bigStack.getResult().copy();
                int newAmount = (int) Math.floor(this.amount / bigStack.getNeeded());
                if (!simulate) {
                    this.amount -= (newAmount * bigStack.getNeeded());
                    if (this.amount == 0) reset();
                    onChange();
                }
                out.setCount(newAmount);
                return out;
            } else {
                if (!simulate) {
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
        return (int) Math.min(Integer.MAX_VALUE, Math.floor((TOTAL_AMOUNT * getMultiplier()) / this.resultList.get(slot).getNeeded()));
    }

    public int getSlotLimitBase(int slot){
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(TOTAL_AMOUNT / this.resultList.get(slot).getNeeded()));
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slot < 3){
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

    public abstract boolean isLocked();

    public List<CompactingUtil.Result> getResultList() {
        return resultList;
    }

    public ItemStack getParent() {
        return parent;
    }
}
