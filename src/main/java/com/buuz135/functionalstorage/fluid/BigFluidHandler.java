package com.buuz135.functionalstorage.fluid;

import com.buuz135.functionalstorage.util.StorageTags;
import com.buuz135.functionalstorage.util.Utils;
import com.hrznstudio.titanium.nbthandler.INBTSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

public abstract class BigFluidHandler extends SnapshotJournal<List<BigFluidHandler.StoredFluid>> implements ResourceHandler<FluidResource>, INBTSerializable<CompoundTag> {

    private final FluidResource[] tankResources;
    private final FluidResource[] filterResources;
    private final int[] amounts;
    private int capacity;

    public BigFluidHandler(int size, int capacity) {
        this.tankResources = new FluidResource[size];
        this.filterResources = new FluidResource[size];
        this.amounts = new int[size];
        for (int i = 0; i < size; i++) {
            tankResources[i] = FluidResource.EMPTY;
            filterResources[i] = FluidResource.EMPTY;
        }
        this.capacity = capacity;
    }

    public int getTanks() { return tankResources.length; }

    public FluidStack getFluidInTank(int tank) {
        return tankResources[tank].toStack(!tankResources[tank].isEmpty() && isDrawerCreative() ? Integer.MAX_VALUE : amounts[tank]);
    }

    public int getTankCapacity(int tank) { return isDrawerCreative() ? Integer.MAX_VALUE : capacity; }

    public boolean isFluidValid(int tank, FluidResource resource) {
        if (resource.isEmpty() || resource.typeHolder().is(StorageTags.FLUID_DRAWER_STORAGE_DENYLIST)) return false;
        return !isDrawerLocked() || resource.equals(filterResources[tank]);
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        for (int i = 0; i < tankResources.length; i++) {
            amounts[i] = Math.min(amounts[i], capacity);
        }
    }

    @Override
    public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < tankResources.length; i++) {
            tag.put(Integer.toString(i), FluidStack.OPTIONAL_CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, provider), tankResources[i].toStack(amounts[i])).getOrThrow());
            tag.put("Locked" + i, FluidStack.OPTIONAL_CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, provider), filterResources[i].toStack(1)).getOrThrow());
        }
        tag.putInt("Capacity", capacity);
        return tag;
    }

    @Override
    public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, CompoundTag nbt) {
        capacity = nbt.getIntOr("Capacity", capacity);
        for (int i = 0; i < tankResources.length; i++) {
            FluidStack tank = Utils.deserializeFluid(provider, nbt.getCompoundOrEmpty(Integer.toString(i)));
            FluidStack filter = Utils.deserializeFluid(provider, nbt.getCompoundOrEmpty("Locked" + i));
            tankResources[i] = FluidResource.of(tank);
            amounts[i] = tank.getAmount();
            filterResources[i] = FluidResource.of(filter);
        }
    }

    public abstract void onChange();
    public abstract boolean isDrawerLocked();
    public abstract boolean isDrawerVoid();
    public abstract boolean isDrawerCreative();

    public void lockHandler() {
        for (int i = 0; i < tankResources.length; i++) {
            filterResources[i] = tankResources[i];
        }
    }

    public FluidStack[] getFilterStack() {
        FluidStack[] stacks = new FluidStack[filterResources.length];
        for (int i = 0; i < stacks.length; i++) stacks[i] = filterResources[i].toStack(1);
        return stacks;
    }

    public void setFilterResource(int tank, FluidResource resource) { filterResources[tank] = resource; }

    @Override public int size() { return getTanks(); }
    @Override public FluidResource getResource(int index) { return tankResources[index]; }
    @Override public long getAmountAsLong(int index) { return isDrawerCreative() && !tankResources[index].isEmpty() ? Integer.MAX_VALUE : amounts[index]; }
    @Override public long getCapacityAsLong(int index, FluidResource resource) { return resource.isEmpty() || isValid(index, resource) ? getTankCapacity(index) : 0; }
    @Override public boolean isValid(int index, FluidResource resource) { return isFluidValid(index, resource); }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || !isFluidValid(index, resource)) return 0;
        FluidResource stored = tankResources[index];
        if (!stored.isEmpty() && !stored.equals(resource)) return 0;
        int inserted = isDrawerCreative() ? amount : Math.min(amount, capacity - amounts[index]);
        if (isDrawerVoid() && (inserted > 0 || !stored.isEmpty())) inserted = amount;
        if (inserted <= 0) return 0;
        updateSnapshots(transaction);
        if (stored.isEmpty()) tankResources[index] = resource;
        if (!isDrawerCreative()) amounts[index] = (int) Math.min(capacity, (long) amounts[index] + inserted);
        else if (amounts[index] == 0) amounts[index] = 1;
        onChange();
        return inserted;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || !resource.equals(tankResources[index])) return 0;
        int extracted = Math.min(amount, isDrawerCreative() ? amount : amounts[index]);
        if (extracted <= 0) return 0;
        updateSnapshots(transaction);
        if (!isDrawerCreative()) {
            amounts[index] -= extracted;
            if (amounts[index] == 0) tankResources[index] = FluidResource.EMPTY;
            onChange();
        }
        return extracted;
    }

    @Override
    protected List<StoredFluid> createSnapshot() {
        List<StoredFluid> snapshot = new ArrayList<>(tankResources.length);
        for (int i = 0; i < tankResources.length; i++) snapshot.add(new StoredFluid(tankResources[i], amounts[i]));
        return snapshot;
    }

    @Override
    protected void revertToSnapshot(List<StoredFluid> snapshot) {
        for (int i = 0; i < tankResources.length; i++) {
            tankResources[i] = snapshot.get(i).resource();
            amounts[i] = snapshot.get(i).amount();
        }
    }

    protected record StoredFluid(FluidResource resource, int amount) {}
}
