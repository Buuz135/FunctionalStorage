package com.buuz135.functionalstorage.fluid;

import com.buuz135.functionalstorage.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public abstract class BigFluidHandler implements IFluidHandler, INBTSerializable<CompoundTag> {

    private CustomFluidTank[] tanks;
    private FluidStack[] filterStack;
    private int capacity;

    public BigFluidHandler(int size, int capacity) {
        this.tanks = new CustomFluidTank[size];
        this.filterStack = new FluidStack[size];
        for (int i = 0; i < this.tanks.length; i++) {
            this.filterStack[i] = FluidStack.EMPTY;
            int finalI = i;
            this.tanks[i] = new CustomFluidTank(capacity, fluidStack -> {
                if (isDrawerLocked()) {
                    return FluidStack.isSameFluidSameComponents(fluidStack, this.filterStack[finalI]);
                }
                return true;
            });
        }
        this.capacity = capacity;
    }

    public CustomFluidTank[] getTankList() {
        return this.tanks;
    }

    @Override
    public int getTanks() {
        return this.tanks.length;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return this.tanks[tank].getFluidInTank(0);
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.tanks[tank].getTankCapacity(0);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.tanks[tank].isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        for (CustomFluidTank tank : tanks) {
            if (!tank.getFluid().isEmpty() && tank.fill(resource, FluidAction.SIMULATE) != 0) {
                int ret = tank.fill(resource, action);
                if (action == FluidAction.EXECUTE) onChange();
                return ret;
            }
        }
        for (CustomFluidTank tank : tanks) {
            if (tank.getFluid().isEmpty() && tank.fill(resource, FluidAction.SIMULATE) != 0) {
                int ret = tank.fill(resource, action);
                if (action == FluidAction.EXECUTE) onChange();
                return ret;
            }
        }
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        for (CustomFluidTank tank : tanks) {
            if (!tank.getFluid().isEmpty() && FluidStack.isSameFluidSameComponents(tank.getFluid(), resource) && !tank.drain(resource, FluidAction.SIMULATE).isEmpty()) {
                FluidStack ret = tank.drain(resource, action);
                if (action == FluidAction.EXECUTE) onChange();
                return ret;
            }
        }
        for (CustomFluidTank tank : tanks) {
            if (!tank.drain(resource, FluidAction.SIMULATE).isEmpty()) {
                FluidStack ret = tank.drain(resource, action);
                if (action == FluidAction.EXECUTE) onChange();
                return ret;
            }
        }
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        for (CustomFluidTank tank : tanks) {
            if (!tank.drain(maxDrain, FluidAction.SIMULATE).isEmpty()) {
                FluidStack ret = tank.drain(maxDrain, action);
                if (action == FluidAction.EXECUTE) onChange();
                return ret;
            }
        }
        return FluidStack.EMPTY;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        for (CustomFluidTank tank : this.tanks) {
            tank.setCapacity(capacity);
            if (!tank.getFluid().isEmpty()) tank.getFluid().setAmount(Math.min(tank.getFluidAmount(), capacity));
        }
    }

    @Override
    public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        for (int i = 0; i < this.tanks.length; i++) {
            compoundTag.put(i + "", this.tanks[i].writeToNBT(provider, new CompoundTag()));
            compoundTag.put("Locked" + i, FluidStack.OPTIONAL_CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, provider), this.filterStack[i]).getOrThrow());
        }
        compoundTag.putInt("Capacity", this.capacity);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, CompoundTag nbt) {
        this.capacity = nbt.getInt("Capacity");
        for (int i = 0; i < this.tanks.length; i++) {
            this.tanks[i].readFromNBT(provider, nbt.getCompound(i + ""));
            this.tanks[i].setCapacity(this.capacity);
            this.filterStack[i] = Utils.deserializeFluid(provider, nbt.getCompound("Locked" + i));
        }
    }

    public abstract void onChange();

    public abstract boolean isDrawerLocked();

    public abstract boolean isDrawerVoid();

    public abstract boolean isDrawerCreative();

    public void lockHandler() {
        for (int i = 0; i < this.tanks.length; i++) {
            this.filterStack[i] = this.tanks[i].getFluid().copy();
            if (!this.filterStack[i].isEmpty()) this.filterStack[i].setAmount(1);
        }
    }

    public FluidStack[] getFilterStack() {
        return filterStack;
    }

    public class CustomFluidTank extends FluidTank {


        public CustomFluidTank(int capacity) {
            super(capacity);
        }

        public CustomFluidTank(int capacity, Predicate<FluidStack> validator) {
            super(capacity, validator);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int amount = super.fill(resource, action);
            if (isDrawerVoid()
                    && ((isDrawerLocked() && isFluidValid(resource)) || (!getFluid().isEmpty() && FluidStack.isSameFluidSameComponents(getFluid(), resource))))
                return resource.getAmount();
            return amount;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            FluidStack stack = super.getFluidInTank(tank);
            if (!stack.isEmpty() && isDrawerCreative()) stack.setAmount(Integer.MAX_VALUE);
            return stack;
        }

        @Override
        public int getTankCapacity(int tank) {
            return isDrawerCreative() ? Integer.MAX_VALUE : super.getTankCapacity(tank);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (isDrawerCreative()) return resource.copy();
            return super.drain(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack fluidStack = super.drain(maxDrain, action);
            if (isDrawerCreative()) fluidStack.setAmount(maxDrain);
            return fluidStack;
        }

        @Override
        public int getCapacity() {
            return isDrawerCreative() ? Integer.MAX_VALUE : super.getCapacity();
        }

        @Override
        public @NotNull FluidStack getFluid() {
            FluidStack stack = super.getFluid();
            if (!stack.isEmpty() && isDrawerCreative()) stack.setAmount(Integer.MAX_VALUE);
            return stack;
        }

        @Override
        public int getFluidAmount() {
            return isDrawerCreative() ? Integer.MAX_VALUE : super.getFluidAmount();
        }
    }
}
