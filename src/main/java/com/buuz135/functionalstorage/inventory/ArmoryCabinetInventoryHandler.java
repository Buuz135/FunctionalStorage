package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.util.StorageTags;
import com.buuz135.functionalstorage.util.Utils;
import com.hrznstudio.titanium.nbthandler.INBTSerializable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class ArmoryCabinetInventoryHandler extends SnapshotJournal<List<ArmoryCabinetInventoryHandler.StoredItem>> implements ResourceHandler<ItemResource>, INBTSerializable<CompoundTag> {

    public List<ItemStack> stackList;
    private List<ItemResource> resourceList;

    public ArmoryCabinetInventoryHandler() {
        this.stackList = create();
        this.resourceList = createResources();
    }

    public int getSlots() {
        return FunctionalStorageConfig.ARMORY_CABINET_SIZE;
    }

    @NotNull
    public ItemStack getStackInSlot(int slot) {
        if (slot < this.stackList.size()) {
            return this.stackList.get(slot);
        }
        return ItemStack.EMPTY;
    }

    public abstract void onChange();

    public int getSlotLimit(int slot) {
        return 1;
    }

    public boolean isItemValid(int slot, ItemResource resource) {
        return isCertifiedResource(resource);
    }

    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (slot < 0 || slot >= this.stackList.size()) return;
        this.stackList.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(Math.min(1, stack.getCount())));
        this.resourceList.set(slot, ItemResource.of(stack));
        onChange();
    }

    private boolean canInsert(int slot, ItemResource resource) {
        return slot >= 0 && slot < stackList.size() && !resource.isEmpty() && stackList.get(slot).isEmpty() && isCertifiedResource(resource);
    }

    private boolean isCertifiedResource(ItemResource resource) {
        ItemStack stack = resource.toStack();
        if (stack.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(stack)) != null) return false;
        if (resource.typeHolder().is(StorageTags.ARMORY_CABINET_INSERTABLE)) return true;
        if (resource.getMaxStackSize() > 1) return false;
        return stack.isDamageableItem() || stack.isEnchantable() || resource.has(DataComponents.JUKEBOX_PLAYABLE) || resource.has(DataComponents.EQUIPPABLE) || resource.is(Items.ENCHANTED_BOOK);
    }

    @Override
    public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        for (int i = 0; i < this.stackList.size(); i++) {
            ItemStack stack = this.stackList.get(i);
            if (!stack.isEmpty()) {
                compoundTag.put(String.valueOf(i), Utils.serialize(provider, stack));
            }
        }
        return compoundTag;
    }

    private List<ItemStack> create() {
        List<ItemStack> stackList = new ArrayList<>();
        for (int i = 0; i < FunctionalStorageConfig.ARMORY_CABINET_SIZE; i++) {
            stackList.add(ItemStack.EMPTY);
        }
        return stackList;
    }

    private List<ItemResource> createResources() {
        List<ItemResource> resources = new ArrayList<>();
        for (int i = 0; i < FunctionalStorageConfig.ARMORY_CABINET_SIZE; i++) resources.add(ItemResource.EMPTY);
        return resources;
    }

    @Override
    public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, CompoundTag nbt) {
        this.stackList = create();
        this.resourceList = createResources();
        for (String allKey : nbt.keySet()) {
            int pos = Integer.parseInt(allKey);
            if (pos < this.stackList.size()) {
                ItemStack stack = Utils.deserialize(provider, nbt.getCompoundOrEmpty(allKey));
                this.stackList.set(pos, stack);
                this.resourceList.set(pos, ItemResource.of(stack));
            }
        }
    }

    @Override
    public int size() {
        return getSlots();
    }

    @Override
    public ItemResource getResource(int index) {
        return index >= 0 && index < resourceList.size() ? resourceList.get(index) : ItemResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        return getStackInSlot(index).isEmpty() ? 0 : 1;
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return resource.isEmpty() || canInsert(index, resource) ? 1 : 0;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return isItemValid(index, resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || !canInsert(index, resource)) return 0;
        updateSnapshots(transaction);
        stackList.set(index, resource.toStack(1));
        resourceList.set(index, resource);
        return 1;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || index < 0 || index >= stackList.size() || !resource.matches(stackList.get(index))) return 0;
        updateSnapshots(transaction);
        stackList.set(index, ItemStack.EMPTY);
        resourceList.set(index, ItemResource.EMPTY);
        return 1;
    }

    @Override
    protected List<StoredItem> createSnapshot() {
        List<StoredItem> snapshot = new ArrayList<>(stackList.size());
        for (int i = 0; i < stackList.size(); i++)
            snapshot.add(new StoredItem(stackList.get(i).copy(), resourceList.get(i)));
        return snapshot;
    }

    @Override
    protected void revertToSnapshot(List<StoredItem> snapshot) {
        stackList = new ArrayList<>(snapshot.size());
        resourceList = new ArrayList<>(snapshot.size());
        for (StoredItem item : snapshot) {
            stackList.add(item.stack().copy());
            resourceList.add(item.resource());
        }
    }

    @Override
    protected void onRootCommit(List<StoredItem> originalState) {
        super.onRootCommit(originalState);
        onChange();
    }

    protected record StoredItem(ItemStack stack, ItemResource resource) {
    }
}
