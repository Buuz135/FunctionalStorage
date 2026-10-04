package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.util.CompactingUtil;
import com.buuz135.functionalstorage.util.StorageTags;
import com.buuz135.functionalstorage.util.Utils;
import com.hrznstudio.titanium.nbthandler.INBTSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class CompactingInventoryHandler extends SnapshotJournal<Integer> implements ResourceHandler<ItemResource>, INBTSerializable<CompoundTag>, ILockable {

    public static final String PARENT = "Parent";
    public static final String BIG_ITEMS = "BigItems";
    public static final String STACK = "Stack";
    public static final String AMOUNT = "Amount";
    public static final String CONFIGURED_SLOTS = "ConfiguredSlots";

    private int amount;
    private ItemStack parent;
    private List<CompactingUtil.Result> resultList;
    private final int slots;
    private int configuredSlots;

    public CompactingInventoryHandler(int slots) {
        this.resultList = new ArrayList<>();
        this.slots = slots;
        for (int i = 0; i < slots; i++) {
            this.resultList.add(i, new CompactingUtil.Result(ItemStack.EMPTY, 1));
        }
        this.configuredSlots = slots;
        this.parent = ItemStack.EMPTY;
    }

    public int getSlots() {
        if (isVoid()) return this.slots + 1;
        return this.slots;
    }

    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        if (slot >= this.slots) return ItemStack.EMPTY;
        CompactingUtil.Result bigStack = this.resultList.get(slot);
        ItemStack copied = bigStack.getResult().copy();
        copied.setCount(isCreative() ? Integer.MAX_VALUE : this.amount / bigStack.getNeeded());
        return copied;
    }

    private boolean isVoidValid(ItemResource resource) {
        for (CompactingUtil.Result result : this.resultList) {
            if (resource.equals(result.getResource())) return true;
        }
        return false;
    }

    public boolean isSetup() {
        return resultList.stream().anyMatch(result -> !result.getResult().isEmpty());
    }

    public void setup(CompactingUtil compactingUtil) {
        this.resultList = compactingUtil.getResults();
        this.parent = compactingUtil.getResults().get(0).getResult();
        if (this.parent.isEmpty()) {
            this.parent = compactingUtil.getResults().get(1).getResult();
        }
        if (this.parent.isEmpty() && compactingUtil.getResults().size() >= 3) {
            this.parent = compactingUtil.getResults().get(2).getResult();
        }
        this.configuredSlots = (int) this.resultList.stream().filter(result -> !result.getResult().isEmpty()).count();
        onChange();
    }

    public void setupWithRearrangedResults(List<CompactingUtil.Result> rearrangedResults) {
        this.resultList = rearrangedResults;
        this.parent = rearrangedResults.get(0).getResult();
        if (this.parent.isEmpty()) {
            this.parent = rearrangedResults.get(1).getResult();
        }
        if (this.parent.isEmpty() && rearrangedResults.size() >= 3) {
            this.parent = rearrangedResults.get(2).getResult();
        }
        this.configuredSlots = (int) this.resultList.stream().filter(result -> !result.getResult().isEmpty()).count();
        onChange();
    }

    public void reset() {
        if (isLocked()) return;
        this.resultList.forEach(result -> {
            result.setResult(ItemStack.EMPTY);
            result.setNeeded(1);
        });
    }

    public int getAmount() {
        return amount;
    }

    public int getSlotLimit(int slot) {
        if (isCreative()) return Integer.MAX_VALUE;
        if (slot == this.slots) return Integer.MAX_VALUE;
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(getTotalAmount() / this.resultList.get(slot).getNeeded()));
    }

    public int getSlotLimitBase(int slot) {
        if (slot == this.slots) return Integer.MAX_VALUE;
        return (int) Math.min(Integer.MAX_VALUE, Math.floor((configuredSlots == 2 ? 64 * 9d : 64 * 9d * 9) / this.resultList.get(slot).getNeeded()));
    }

    public boolean isItemValid(int slot, ItemResource resource) {
        return isSetup() && !resource.isEmpty() && !resource.typeHolder().is(StorageTags.DRAWER_STORAGE_DENYLIST);
    }

    private boolean canInsert(int slot, ItemResource resource) {
        if (resource.typeHolder().is(StorageTags.DRAWER_STORAGE_DENYLIST)) {
            return false;
        }
        if (slot < this.slots) {
            CompactingUtil.Result bigStack = this.resultList.get(slot);
            return !bigStack.getResource().isEmpty() && resource.equals(bigStack.getResource());
        }
        return false;
    }

    @Override
    public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put(PARENT, Utils.serialize(provider, this.getParent()));
        compoundTag.putInt(AMOUNT, this.amount);
        CompoundTag items = new CompoundTag();
        for (int i = 0; i < this.resultList.size(); i++) {
            CompoundTag bigStack = new CompoundTag();
            bigStack.put(STACK, Utils.serialize(provider, this.resultList.get(i).getResult()));
            bigStack.putInt(AMOUNT, this.resultList.get(i).getNeeded());
            items.put(i + "", bigStack);
        }
        compoundTag.put(BIG_ITEMS, items);
        compoundTag.putInt(CONFIGURED_SLOTS, this.configuredSlots);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, CompoundTag nbt) {
        this.parent = Utils.deserialize(provider, nbt.getCompoundOrEmpty(PARENT));
        this.amount = nbt.getIntOr(AMOUNT, 0);
        CompoundTag items = nbt.getCompoundOrEmpty(BIG_ITEMS);
        for (String allKey : items.keySet()) {
            CompoundTag entry = items.getCompoundOrEmpty(allKey);
            this.resultList.get(Integer.parseInt(allKey)).setResult(Utils.deserialize(provider, entry.getCompoundOrEmpty(STACK)));
            this.resultList.get(Integer.parseInt(allKey)).setNeeded(Math.max(1, entry.getIntOr(AMOUNT, 1)));
        }
        this.configuredSlots = nbt.getIntOr(CONFIGURED_SLOTS, 0);
    }

    public double getTotalAmount() {
        return configuredSlots == 2 ? 64 * 9d * getMultiplier() : 64 * 9d * 9 * getMultiplier();
    }

    public abstract void onChange();

    public abstract float getMultiplier();

    public abstract boolean isVoid();

    public List<CompactingUtil.Result> getResultList() {
        return resultList;
    }

    public ItemStack getParent() {
        return parent;
    }

    public abstract boolean isCreative();

    @Override
    public int size() {
        return getSlots();
    }

    @Override
    public ItemResource getResource(int index) {
        return index >= slots ? ItemResource.EMPTY : resultList.get(index).getResource();
    }

    @Override
    public long getAmountAsLong(int index) {
        return index >= slots ? 0 : isCreative() && !getResource(index).isEmpty() ? Integer.MAX_VALUE : amount / resultList.get(index).getNeeded();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return resource.isEmpty() || canInsert(index, resource) ? getSlotLimit(index) : 0;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return isItemValid(index, resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int requested, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, requested);
        if (requested == 0) return 0;
        if (isVoid() && index == slots && isVoidValid(resource) || isCreative() && isVoidValid(resource))
            return requested;
        if (!canInsert(index, resource)) return 0;
        CompactingUtil.Result result = resultList.get(index);
        long availableBaseUnits = (long) getSlotLimit(index) * result.getNeeded() - amount;
        int accepted = Math.min(requested, (int) Math.max(0, availableBaseUnits / result.getNeeded()));
        if (isVoid()) accepted = requested;
        if (accepted <= 0) return 0;
        updateSnapshots(transaction);
        amount = (int) Math.min((long) amount + (long) accepted * result.getNeeded(), (int) Math.floor(getTotalAmount()));
        return accepted;
    }

    @Override
    public int extract(int index, ItemResource resource, int requested, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, requested);
        if (requested == 0 || index >= slots || !resource.equals(getResource(index))) return 0;
        CompactingUtil.Result result = resultList.get(index);
        int extracted = Math.min(requested, amount / result.getNeeded());
        if (extracted <= 0) return 0;
        updateSnapshots(transaction);
        if (!isCreative()) {
            amount -= extracted * result.getNeeded();
            if (amount == 0) reset();

        }
        return extracted;
    }

    @Override
    protected void onRootCommit(Integer originalState) {
        super.onRootCommit(originalState);
        onChange();
    }

    @Override
    protected Integer createSnapshot() {
        return amount;
    }

    @Override
    protected void revertToSnapshot(Integer snapshot) {
        amount = snapshot;
    }
}
