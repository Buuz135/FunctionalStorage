package com.buuz135.functionalstorage.inventory.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler.BigStack;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.component.SizeProvider;
import com.buuz135.functionalstorage.util.StorageTags;
import com.buuz135.functionalstorage.util.Utils;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
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

import static com.buuz135.functionalstorage.inventory.BigInventoryHandler.AMOUNT;
import static com.buuz135.functionalstorage.inventory.BigInventoryHandler.BIG_ITEMS;
import static com.buuz135.functionalstorage.inventory.BigInventoryHandler.STACK;

public class DrawerStackItemHandler extends SnapshotJournal<List<DrawerStackItemHandler.StoredResource>> implements ResourceHandler<ItemResource>, INBTSerializable<CompoundTag> {

    private List<BigInventoryHandler.BigStack> storedStacks;
    private ItemStack stack;
    private FunctionalStorage.DrawerType type;
    private float size;
    private boolean isVoid;
    private boolean isCreative;

    public DrawerStackItemHandler(ItemStack stack, FunctionalStorage.DrawerType drawerType) {
        this.stack = stack;
        this.storedStacks = new ArrayList<>();
        this.type = drawerType;
        this.size = drawerType.getSlotAmount();
        this.isVoid = false;
        this.isCreative = false;
        for (int i = 0; i < drawerType.getSlots(); i++) {
            this.storedStacks.add(i, new BigInventoryHandler.BigStack(ItemStack.EMPTY, 0));
        }
        if (stack.has(FSAttachments.TILE)) {
            var tile = stack.get(FSAttachments.TILE);
            var titaniumData = tile.contains("TitaniumData") ? tile.getCompoundOrEmpty("TitaniumData") : tile;
            this.isCreative = titaniumData.getBooleanOr("isCreative", false);
            var access = Utils.registryAccess();
            deserializeNBT(access, titaniumData.getCompoundOrEmpty("handler"));

            var upgrades = new InventoryComponent<>("storage_upgrades", 0, 0, 4);
            upgrades.deserializeNBT(access, tile.getCompoundOrEmpty("storageUpgrades"));
            size = SizeProvider.calculateAsFactor(upgrades, FSAttachments.ITEM_STORAGE_MODIFIER, drawerType.getSlotAmount());

            for (Tag tag : titaniumData.getCompoundOrEmpty("utilityUpgrades").getListOrEmpty("Items")) {
                ItemStack itemStack = Utils.deserialize(access, (CompoundTag) tag);
                if (itemStack.getItem().equals(FunctionalStorage.VOID_UPGRADE.get())) {
                    this.isVoid = true;
                }
            }
        }
    }

    @Override
    public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) {
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
    public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, CompoundTag nbt) {
        CompoundTag items = nbt.getCompoundOrEmpty(BIG_ITEMS);
        for (String allKey : items.keySet()) {
            CompoundTag entry = items.getCompoundOrEmpty(allKey);
            this.storedStacks.get(Integer.parseInt(allKey)).setStack(Utils.deserialize(provider, entry.getCompoundOrEmpty(STACK)));
            this.storedStacks.get(Integer.parseInt(allKey)).setAmount(entry.getIntOr(AMOUNT, 0));
        }
    }

    public int getSlots() {
        return type.getSlots();
    }

    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        BigStack bigStack = this.storedStacks.get(slot);
        if (isCreative) {
            return bigStack.getStack().copyWithCount(Integer.MAX_VALUE);
        }
        ItemStack copied = bigStack.getStack().copy();
        copied.setCount(bigStack.getAmount());
        return copied;
    }

    private boolean isVoid() {
        return true;
    }

    private void onChange() {
        stack.set(FSAttachments.TILE, new CompoundTag());
        stack.get(FSAttachments.TILE).put("handler", serializeNBT(Utils.registryAccess()));
    }

    private boolean canInsert(int slot, ItemResource resource) {
        if (resource.typeHolder().is(StorageTags.DRAWER_STORAGE_DENYLIST)) {
            return false;
        }
        if (slot < type.getSlots()) {
            BigStack bigStack = this.storedStacks.get(slot);
            return bigStack.getResource().isEmpty() || bigStack.getResource().equals(resource);
        }
        return false;
    }

    public boolean isLocked() {
        return true;
    }

    public int getSlotLimit(int slot) {
        if (isCreative) return Integer.MAX_VALUE;

        var stored = getStackInSlot(slot);
        long maxSize = Item.DEFAULT_MAX_STACK_SIZE;
        if (!stored.isEmpty()) {
            maxSize = stored.getMaxStackSize();
        }

        return (int) Math.min(Integer.MAX_VALUE, Math.floor(size * maxSize));
    }

    public boolean isItemValid(int slot, ItemResource resource) {
        return !resource.isEmpty() && !resource.typeHolder().is(StorageTags.DRAWER_STORAGE_DENYLIST);
    }

    public List<BigStack> getStoredStacks() {
        return storedStacks;
    }

    public boolean isCreative() {
        return isCreative;
    }

    @Override public int size() { return getSlots(); }
    @Override public ItemResource getResource(int index) { return storedStacks.get(index).getResource(); }
    @Override public long getAmountAsLong(int index) { return isCreative && !getResource(index).isEmpty() ? Integer.MAX_VALUE : storedStacks.get(index).getAmount(); }
    @Override public long getCapacityAsLong(int index, ItemResource resource) { return resource.isEmpty() || canInsert(index, resource) ? getSlotLimit(index) : 0; }
    @Override public boolean isValid(int index, ItemResource resource) { return isItemValid(index, resource); }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) return 0;
        if (!canInsert(index, resource)) return 0;
        BigStack stored = storedStacks.get(index);
        int inserted = Math.min(getSlotLimit(index) - stored.getAmount(), amount);
        if (isVoid()) inserted = amount;
        if (inserted <= 0) return 0;
        updateSnapshots(transaction);
        if (stored.getResource().isEmpty()) stored.setResource(resource);
        stored.setAmount((int) Math.min((long) stored.getAmount() + inserted, getSlotLimit(index)));
        onChange();
        return inserted;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || !resource.equals(getResource(index))) return 0;
        BigStack stored = storedStacks.get(index);
        int extracted = Math.min(amount, stored.getAmount());
        if (extracted <= 0) return 0;
        updateSnapshots(transaction);
        if (!isCreative) {
            int remaining = stored.getAmount() - extracted;
            stored.setAmount(remaining);
            if (remaining == 0 && !isLocked()) stored.setResource(ItemResource.EMPTY);
            onChange();
        }
        return extracted;
    }

    @Override
    protected List<StoredResource> createSnapshot() {
        List<StoredResource> snapshot = new ArrayList<>(storedStacks.size());
        for (BigStack stored : storedStacks) snapshot.add(new StoredResource(stored.getResource(), stored.getAmount()));
        return snapshot;
    }

    @Override
    protected void revertToSnapshot(List<StoredResource> snapshot) {
        for (int slot = 0; slot < storedStacks.size(); slot++) {
            StoredResource wanted = snapshot.get(slot);
            storedStacks.get(slot).setResource(wanted.resource());
            storedStacks.get(slot).setAmount(wanted.amount());
        }
        onChange();
    }

    protected record StoredResource(ItemResource resource, int amount) {}
}
