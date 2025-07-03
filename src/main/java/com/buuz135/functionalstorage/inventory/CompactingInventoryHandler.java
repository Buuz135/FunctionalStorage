package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.util.CompactingUtil;
import com.buuz135.functionalstorage.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class CompactingInventoryHandler implements IItemHandler, INBTSerializable<CompoundTag>, ILockable {

    public static final String PARENT = "Parent";
    public static final String BIG_ITEMS = "BigItems";
    public static final String STACK = "Stack";
    public static final String AMOUNT = "Amount";

    public int totalAmount;

    private int amount;
    private ItemStack parent;
    private List<CompactingUtil.Result> resultList;
    private int slots;

    public CompactingInventoryHandler(int slots) {
        this.resultList = new ArrayList<>();
        this.slots = slots;
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
        if (slot >= this.slots) return ItemStack.EMPTY;
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
                this.amount = Math.min(this.amount + inserted, (int)Math.floor(getTotalAmount()));
                onChange();
            }
            if (inserted == stack.getCount() * result.getNeeded() || isVoid()) return ItemStack.EMPTY;
            return stack.copyWithCount(stack.getCount() - inserted / result.getNeeded());

        }
        return stack;
    }

    private boolean isVoidValid(ItemStack stack) {
        for (CompactingUtil.Result result : this.resultList) {
            if (ItemStack.isSameItemSameComponents(result.getResult(), stack)) return true;
        }
        return false;
    }

    public boolean isSetup() {
        return resultList.stream().anyMatch(result -> !result.getResult().isEmpty());
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

    public void setupWithRearrangedResults(List<CompactingUtil.Result> rearrangedResults){
        this.resultList = rearrangedResults;
        this.parent = rearrangedResults.get(0).getResult();
        if (this.parent.isEmpty()) {
            this.parent = rearrangedResults.get(1).getResult();
        }
        if (this.parent.isEmpty() && rearrangedResults.size() >= 3) {
            this.parent = rearrangedResults.get(2).getResult();
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
            if (!isCreative() && stackAmount >= this.amount) {
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
                return bigStack.getResult().copyWithCount(amount);
            }


        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (isCreative()) return Integer.MAX_VALUE;
        if (slot == this.slots) return Integer.MAX_VALUE;
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(getTotalAmount() / this.resultList.get(slot).getNeeded()));
    }

    public int getSlotLimitBase(int slot) {
        if (slot == this.slots) return Integer.MAX_VALUE;
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(64 * 9 * 9f / this.resultList.get(slot).getNeeded()));
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return isSetup() && !stack.isEmpty();
    }

    private boolean isValid(int slot, @Nonnull ItemStack stack){
        if (slot < this.slots) {
            CompactingUtil.Result bigStack = this.resultList.get(slot);
            ItemStack fl = bigStack.getResult();
            return !fl.isEmpty() && ItemStack.isSameItemSameComponents(fl, stack);
        }
        return false;
    }

    @Override
    public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put(PARENT, this.getParent().saveOptional(provider));
        compoundTag.putInt(AMOUNT, this.amount);
        CompoundTag items = new CompoundTag();
        for (int i = 0; i < this.resultList.size(); i++) {
            CompoundTag bigStack = new CompoundTag();
            bigStack.put(STACK, this.resultList.get(i).getResult().saveOptional(provider));
            bigStack.putInt(AMOUNT, this.resultList.get(i).getNeeded());
            items.put(i + "", bigStack);
        }
        compoundTag.put(BIG_ITEMS, items);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, CompoundTag nbt) {
        this.parent = Utils.deserialize(provider, nbt.getCompound(PARENT));
        this.amount = nbt.getInt(AMOUNT);
        for (String allKey : nbt.getCompound(BIG_ITEMS).getAllKeys()) {
            this.resultList.get(Integer.parseInt(allKey)).setResult(Utils.deserialize(provider, nbt.getCompound(BIG_ITEMS).getCompound(allKey).getCompound(STACK)));
            this.resultList.get(Integer.parseInt(allKey)).setNeeded(Math.max(1, nbt.getCompound(BIG_ITEMS).getCompound(allKey).getInt(AMOUNT)));
        }
    }

    public double getTotalAmount() {
        return slots == 2 ? 64 * 9d * getMultiplier() : 64 * 9d * 9 * getMultiplier();
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

    public abstract boolean isCreative();
}
