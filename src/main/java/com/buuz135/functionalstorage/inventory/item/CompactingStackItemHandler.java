package com.buuz135.functionalstorage.inventory.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.component.SizeProvider;
import com.buuz135.functionalstorage.util.CompactingUtil;
import com.buuz135.functionalstorage.util.StorageTags;
import com.buuz135.functionalstorage.util.Utils;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import com.hrznstudio.titanium.nbthandler.INBTSerializable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CompactingStackItemHandler extends SnapshotJournal<Integer> implements ResourceHandler<ItemResource>, INBTSerializable<CompoundTag> {

    public static String PARENT = "Parent";
    public static String BIG_ITEMS = "BigItems";
    public static String STACK = "Stack";
    public static String AMOUNT = "Amount";

    private int amount;
    private ItemStack parent;
    private List<CompactingUtil.Result> resultList;
    private final int slots;
    private float size;
    private boolean isVoid;
    private boolean isCreative;
    private final ItemStack stack;

    public CompactingStackItemHandler(ItemStack stack, int slots) {
        this.stack = stack;
        this.resultList = new ArrayList<>();
        this.slots = slots;
        this.size = 512;
        for (int i = 0; i < slots; i++) {
            this.resultList.add(i, new CompactingUtil.Result(ItemStack.EMPTY, 1));
        }
        this.parent = ItemStack.EMPTY;
        this.isVoid = false;
        this.isCreative = false;
        if (stack.has(FSAttachments.TILE)) {
            var tile = stack.get(FSAttachments.TILE);
            var titaniumData = tile.contains("TitaniumData") ? tile.getCompoundOrEmpty("TitaniumData") : tile;
            deserializeNBT(Utils.registryAccess(), titaniumData.getCompoundOrEmpty("handler"));

            var upgrades = new InventoryComponent<>("storage_upgrades", 0, 0, 4);
            upgrades.deserializeNBT(Utils.registryAccess(), tile.getCompoundOrEmpty("storageUpgrades"));
            size = SizeProvider.calculateAsFactor(upgrades, FSAttachments.ITEM_STORAGE_MODIFIER, size);

            for (Tag tag : tile.getCompoundOrEmpty("storageUpgrades").getListOrEmpty("Items")) {
                ItemStack itemStack = Utils.deserialize(Utils.registryAccess(), (CompoundTag) tag);
                if (itemStack.getItem().equals(FunctionalStorage.CREATIVE_UPGRADE.get())) {
                    this.isCreative = true;
                }
            }
            for (Tag tag : titaniumData.getCompoundOrEmpty("utilityUpgrades").getListOrEmpty("Items")) {
                ItemStack itemStack = Utils.deserialize(Utils.registryAccess(), (CompoundTag) tag);
                if (itemStack.getItem().equals(FunctionalStorage.VOID_UPGRADE.get())) {
                    this.isVoid = true;
                }
            }
        }
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
        return !this.resultList.get(this.resultList.size() - 1).getResult().isEmpty();
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
        onChange();
    }

    public void reset() {

    }

    public int getAmount() {
        return amount;
    }

    public int getSlotLimit(int slot) {
        if (isCreative()) return Integer.MAX_VALUE;
        if (slot == this.slots) return Integer.MAX_VALUE;
        return (int) Math.min(Integer.MAX_VALUE, Math.floor((size * 64 * 9 * 9) / this.resultList.get(slot).getNeeded()));
    }

    public int getSlotLimitBase(int slot) {
        if (slot == this.slots) return Integer.MAX_VALUE;
        return (int) Math.min(Integer.MAX_VALUE, Math.floor((size * 64 * 9 * 9) / this.resultList.get(slot).getNeeded()));
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
    }

    public void onChange() {
        stack.set(FSAttachments.TILE, new CompoundTag());
        stack.get(FSAttachments.TILE).put("handler", serializeNBT(Utils.registryAccess()));
    }

    public boolean isVoid() {
        return isVoid;
    }

    public List<CompactingUtil.Result> getResultList() {
        return resultList;
    }

    public ItemStack getParent() {
        return parent;
    }

    public boolean isCreative() {
        return isCreative;
    }

    @Override public int size() { return getSlots(); }
    @Override public ItemResource getResource(int index) { return index >= slots ? ItemResource.EMPTY : resultList.get(index).getResource(); }
    @Override public long getAmountAsLong(int index) { return index >= slots ? 0 : isCreative() && !getResource(index).isEmpty() ? Integer.MAX_VALUE : amount / resultList.get(index).getNeeded(); }
    @Override public long getCapacityAsLong(int index, ItemResource resource) { return resource.isEmpty() || canInsert(index, resource) ? getSlotLimit(index) : 0; }
    @Override public boolean isValid(int index, ItemResource resource) { return isItemValid(index, resource); }

    @Override
    public int insert(int index, ItemResource resource, int requested, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, requested);
        if (requested == 0) return 0;
        if (isVoid() && index == slots && isVoidValid(resource) || isCreative() && isVoidValid(resource)) return requested;
        if (!canInsert(index, resource)) return 0;
        CompactingUtil.Result result = resultList.get(index);
        long availableBaseUnits = (long) getSlotLimit(index) * result.getNeeded() - amount;
        int accepted = Math.min(requested, (int) Math.max(0, availableBaseUnits / result.getNeeded()));
        if (isVoid()) accepted = requested;
        if (accepted <= 0) return 0;
        updateSnapshots(transaction);
        amount = (int) Math.min((long) amount + (long) accepted * result.getNeeded(), (int) Math.floor(size * 64 * 9 * 9));
        onChange();
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
            onChange();
        }
        return extracted;
    }

    @Override protected Integer createSnapshot() { return amount; }
    @Override protected void revertToSnapshot(Integer snapshot) { amount = snapshot; onChange(); }
}
