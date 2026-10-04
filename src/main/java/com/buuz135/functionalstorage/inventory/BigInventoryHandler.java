package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.util.StorageTags;
import com.buuz135.functionalstorage.util.Utils;
import com.hrznstudio.titanium.nbthandler.INBTSerializable;
import net.minecraft.core.HolderLookup;
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

public abstract class BigInventoryHandler extends SnapshotJournal<List<BigInventoryHandler.StoredResource>> implements ResourceHandler<ItemResource>, INBTSerializable<CompoundTag>, ILockable {

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
    public int size() {
        if (isVoid()) return type.getSlots() + 1;
        return type.getSlots();
    }

    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        if (type.getSlots() == slot) return ItemStack.EMPTY;
        BigStack bigStack = this.storedStacks.get(slot);
        if (isCreative()) {
            return bigStack.slotStack.copyWithCount(Integer.MAX_VALUE);
        }
        return bigStack.slotStack;
    }

    public int getSlotLimit(int slot) {
        if (isCreative()) return Integer.MAX_VALUE;
        if (type.getSlots() == slot) return Integer.MAX_VALUE;
        double stackSize = 1;
        if (!getStoredStacks().get(slot).getStack().isEmpty()) {
            stackSize = getStoredStacks().get(slot).getStack().getMaxStackSize() / 64D;
        }
        return (int) Math.floor(getTotalAmount() * stackSize);
    }

    public int getSlotLimit(int slot, ItemResource resource) {
        if (isCreative()) return Integer.MAX_VALUE;
        if (type.getSlots() == slot) return Integer.MAX_VALUE;
        double stackSize = 1;
        if (!resource.isEmpty()) {
            stackSize = resource.getMaxStackSize() / 64D;
        }
        return (int) Math.floor(getTotalAmount() * stackSize);
    }

    public boolean isItemValid(int slot, ItemResource resource) {
        return !resource.isEmpty() && !resource.typeHolder().is(StorageTags.DRAWER_STORAGE_DENYLIST);
    }

    private boolean canInsert(int slot, ItemResource resource) {
        if (resource.typeHolder().is(StorageTags.DRAWER_STORAGE_DENYLIST)) {
            return false;
        }
        if (slot < type.getSlots()) {
            BigStack bigStack = this.storedStacks.get(slot);
            ItemResource stored = bigStack.getResource();
            if (isLocked() && stored.isEmpty()) return false;
            return stored.isEmpty() || stored.equals(resource);
        }
        return false;
    }

    private boolean isVoidValid(ItemResource resource) {
        for (BigStack storedStack : this.storedStacks) {
            if (storedStack.getResource().equals(resource)) return true;
        }
        return false;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag items = new CompoundTag();
        for (int i = 0; i < this.storedStacks.size(); i++) {
            CompoundTag bigStack = new CompoundTag();
            bigStack.put(STACK, Utils.serialize(provider, this.storedStacks.get(i).getStack()));
            bigStack.putInt(AMOUNT, this.storedStacks.get(i).getAmount());
            items.put(i + "", bigStack);
        }
        compoundTag.put(BIG_ITEMS, items);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        CompoundTag items = nbt.getCompoundOrEmpty(BIG_ITEMS);
        for (String allKey : items.keySet()) {
            CompoundTag entry = items.getCompoundOrEmpty(allKey);
            this.storedStacks.get(Integer.parseInt(allKey)).setStack(Utils.deserialize(provider, entry.getCompoundOrEmpty(STACK)));
            this.storedStacks.get(Integer.parseInt(allKey)).setAmount(entry.getIntOr(AMOUNT, 0));
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

    @Override
    public ItemResource getResource(int index) {
        return index == type.getSlots() ? ItemResource.EMPTY : storedStacks.get(index).getResource();
    }

    @Override
    public long getAmountAsLong(int index) {
        if (index == type.getSlots()) return 0;
        return isCreative() && !storedStacks.get(index).getResource().isEmpty() ? Integer.MAX_VALUE : storedStacks.get(index).getAmount();
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
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) return 0;
        if (isVoid() && index == type.getSlots() && isVoidValid(resource) || isCreative() && isVoidValid(resource)) return amount;
        if (!canInsert(index, resource)) return 0;
        BigStack stored = storedStacks.get(index);
        int inserted = Math.min(getSlotLimit(index, resource) - stored.getAmount(), amount);
        if (isVoid()) inserted = amount;
        if (inserted <= 0) return 0;
        updateSnapshots(transaction);
        if (stored.getResource().isEmpty()) stored.setResource(resource);
        stored.setAmount((int) Math.min((long) stored.getAmount() + inserted, getSlotLimit(index, resource)));
        return inserted;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || index >= type.getSlots()) return 0;
        BigStack stored = storedStacks.get(index);
        if (!resource.equals(stored.getResource())) return 0;
        int extracted = Math.min(amount, Math.min(getSlotLimit(index), stored.getAmount()));
        if (extracted <= 0) return 0;
        updateSnapshots(transaction);
        if (!isCreative()) {
            int remaining = stored.getAmount() - extracted;
            stored.setAmount(remaining);
            if (remaining == 0 && !isLocked()) stored.setResource(ItemResource.EMPTY);
        }
        return extracted;
    }

    @Override
    protected List<StoredResource> createSnapshot() {
        List<StoredResource> snapshot = new ArrayList<>(type.getSlots());
        for (BigStack stored : storedStacks) snapshot.add(new StoredResource(stored.getResource(), stored.getAmount()));
        return snapshot;
    }

    @Override
    protected void revertToSnapshot(List<StoredResource> snapshot) {
        for (int slot = 0; slot < type.getSlots(); slot++) {
            StoredResource wanted = snapshot.get(slot);
            BigStack target = storedStacks.get(slot);
            target.setResource(wanted.resource());
            target.setAmount(wanted.amount());
        }
    }

    @Override
    protected void onRootCommit(List<StoredResource> originalState) {
        super.onRootCommit(originalState);
        onChange();
    }

    protected record StoredResource(ItemResource resource, int amount) {}

    public static class BigStack {

        private ItemStack stack;
        private ItemStack slotStack;
        private ItemResource resource;
        private int amount;

        public BigStack(ItemStack stack, int amount) {
            this.stack = stack.copy();
            this.resource = ItemResource.of(stack);
            this.amount = amount;
            this.slotStack = stack.copyWithCount(amount);
        }

        public ItemStack getStack() {
            return stack;
        }

        public void setStack(ItemStack stack) {
            this.stack = stack.copy();
            this.resource = ItemResource.of(stack);
            this.slotStack = stack.copyWithCount(amount);
        }

        public ItemResource getResource() {
            return resource;
        }

        public void setResource(ItemResource resource) {
            this.resource = resource;
            this.stack = resource.toStack(resource.isEmpty() ? 0 : resource.getMaxStackSize());
            this.slotStack = resource.toStack(amount);
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
            this.slotStack.setCount(amount);
        }

        public ItemStack getSlotStack() {
            return slotStack;
        }
    }
}
