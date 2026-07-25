package com.buuz135.functionalstorage.inventory.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.component.SizeProvider;
import com.buuz135.functionalstorage.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class FluidDrawerStackItemHandler implements IFluidHandlerItem {

    private final ItemStack container;
    private final FunctionalStorage.DrawerType type;
    private final BigFluidHandler fluidHandler;
    private boolean isVoid;
    private boolean isCreative;

    public FluidDrawerStackItemHandler(ItemStack container, FunctionalStorage.DrawerType type) {
        this.container = container;
        this.type = type;
        this.fluidHandler = new BigFluidHandler(type.getSlots(), getTankCapacity(getStorageMultiplier())) {
            @Override
            public void onChange() {
                FluidDrawerStackItemHandler.this.onChange();
            }

            @Override
            public boolean isDrawerLocked() {
                return FluidDrawerStackItemHandler.this.isLocked();
            }

            @Override
            public boolean isDrawerVoid() {
                return FluidDrawerStackItemHandler.this.isVoid;
            }

            @Override
            public boolean isDrawerCreative() {
                return FluidDrawerStackItemHandler.this.isCreative;
            }
        };

        if (container.has(FSAttachments.TILE)) {
            var access = Utils.registryAccess();
            CompoundTag tile = container.get(FSAttachments.TILE);
            this.isCreative = tile.contains("isCreative") && tile.getBoolean("isCreative");
            this.isVoid = tile.contains("isVoid") && tile.getBoolean("isVoid");

            if (tile.contains("fluidHandler")) {
                this.fluidHandler.deserializeNBT(access, tile.getCompound("fluidHandler"));
            }

            var storageUpgrades = getStorageUpgrades(tile);
            for (int i = 0; i < storageUpgrades.getSlots(); i++) {
                if (storageUpgrades.getStackInSlot(i).is(FunctionalStorage.CREATIVE_UPGRADE.get())) {
                    this.isCreative = true;
                }
            }

            if (tile.contains("utilityUpgrades")) {
                for (Tag tag : tile.getCompound("utilityUpgrades").getList("Items", Tag.TAG_COMPOUND)) {
                    ItemStack upgrade = Utils.deserialize(access, (CompoundTag) tag);
                    if (upgrade.is(FunctionalStorage.VOID_UPGRADE.get())) {
                        this.isVoid = true;
                    }
                }
            }
        }

        this.fluidHandler.setCapacity(getTankCapacity(getStorageMultiplier()));
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return fluidHandler.getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return fluidHandler.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return fluidHandler.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return fluidHandler.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1) {
            return 0;
        }
        return fluidHandler.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1) {
            return FluidStack.EMPTY;
        }
        return fluidHandler.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1) {
            return FluidStack.EMPTY;
        }
        return fluidHandler.drain(maxDrain, action);
    }

    private void onChange() {
        CompoundTag tile = container.getOrDefault(FSAttachments.TILE, new CompoundTag()).copy();
        tile.put("fluidHandler", fluidHandler.serializeNBT(Utils.registryAccess()));
        container.set(FSAttachments.TILE, tile);
    }

    private boolean isLocked() {
        return container.getOrDefault(FSAttachments.LOCKED, false);
    }

    private int getTankCapacity(float storageMultiplier) {
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(storageMultiplier * 1000L));
    }

    private float getStorageMultiplier() {
        if (!container.has(FSAttachments.TILE)) {
            return type.getSlotAmount();
        }
        return SizeProvider.calculateAsFactor(getStorageUpgrades(container.get(FSAttachments.TILE)), FSAttachments.FLUID_STORAGE_MODIFIER, type.getSlotAmount());
    }

    private ItemStackHandler getStorageUpgrades(CompoundTag tile) {
        var storageUpgrades = new ItemStackHandler(4);
        if (tile.contains("storageUpgrades")) {
            storageUpgrades.deserializeNBT(Utils.registryAccess(), tile.getCompound("storageUpgrades"));
        }
        return storageUpgrades;
    }
}
