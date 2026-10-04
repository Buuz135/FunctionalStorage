package com.buuz135.functionalstorage.inventory.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.component.SizeProvider;
import com.buuz135.functionalstorage.util.Utils;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

public class FluidDrawerStackItemHandler implements ResourceHandler<FluidResource> {

    private final ItemAccess itemAccess;
    private final FunctionalStorage.DrawerType type;
    private final BigFluidHandler fluidHandler;
    private boolean isVoid;
    private boolean isCreative;

    public FluidDrawerStackItemHandler(ItemStack container, FunctionalStorage.DrawerType type) {
        this(container, type, ItemAccess.forStack(container));
    }

    public FluidDrawerStackItemHandler(ItemStack container, FunctionalStorage.DrawerType type, ItemAccess itemAccess) {
        this.itemAccess = itemAccess;
        this.type = type;
        this.fluidHandler = new BigFluidHandler(type.getSlots(), getTankCapacity(getStorageMultiplier())) {
            @Override
            public void onChange() {
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
            CompoundTag titaniumData = tile.contains("TitaniumData") ? tile.getCompoundOrEmpty("TitaniumData") : tile;
            this.isCreative = titaniumData.getBooleanOr("isCreative", false);
            this.isVoid = titaniumData.getBooleanOr("isVoid", false);

            if (titaniumData.contains("fluidHandler")) {
                this.fluidHandler.deserializeNBT(access, titaniumData.getCompoundOrEmpty("fluidHandler"));
            }

            var storageUpgrades = getStorageUpgrades(tile);
            for (int i = 0; i < storageUpgrades.getSlots(); i++) {
                if (storageUpgrades.getStackInSlot(i).is(FunctionalStorage.CREATIVE_UPGRADE.get())) {
                    this.isCreative = true;
                }
            }

            if (titaniumData.contains("utilityUpgrades")) {
                for (Tag tag : titaniumData.getCompoundOrEmpty("utilityUpgrades").getListOrEmpty("Items")) {
                    ItemStack upgrade = Utils.deserialize(access, (CompoundTag) tag);
                    if (upgrade.is(FunctionalStorage.VOID_UPGRADE.get())) {
                        this.isVoid = true;
                    }
                }
            }
        }

        this.fluidHandler.setCapacity(getTankCapacity(getStorageMultiplier()));
    }

    public ItemStack getContainer() {
        return itemAccess.getResource().toStack(itemAccess.getAmount());
    }

    public int getTanks() {
        return fluidHandler.getTanks();
    }

    public @NotNull FluidStack getFluidInTank(int tank) {
        return fluidHandler.getFluidInTank(tank);
    }

    public int getTankCapacity(int tank) {
        return fluidHandler.getTankCapacity(tank);
    }

    public boolean isFluidValid(int tank, FluidResource resource) {
        return fluidHandler.isFluidValid(tank, resource);
    }

    @Override
    public int size() {
        return fluidHandler.size();
    }

    @Override
    public FluidResource getResource(int index) {
        return fluidHandler.getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        return fluidHandler.getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return fluidHandler.getCapacityAsLong(index, resource);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return fluidHandler.isValid(index, resource);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return transfer(index, resource, amount, transaction, true);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return transfer(index, resource, amount, transaction, false);
    }

    private int transfer(int index, FluidResource resource, int amount, TransactionContext transaction, boolean insert) {
        if (itemAccess.getAmount() != 1) return 0;
        try (Transaction transfer = Transaction.open(transaction)) {
            int transferred = insert ? fluidHandler.insert(index, resource, amount, transfer) : fluidHandler.extract(index, resource, amount, transfer);
            if (transferred == 0) return 0;
            ItemStack container = getContainer();
            CompoundTag tile = container.getOrDefault(FSAttachments.TILE, new CompoundTag()).copy();
            CompoundTag titaniumData = tile.contains("TitaniumData") ? tile.getCompoundOrEmpty("TitaniumData") : tile;
            titaniumData.put("fluidHandler", fluidHandler.serializeNBT(Utils.registryAccess()));
            container.set(FSAttachments.TILE, tile);
            if (itemAccess.exchange(ItemResource.of(container), 1, transfer) != 1) return 0;
            transfer.commit();
            return transferred;
        }
    }

    private boolean isLocked() {
        return getContainer().getOrDefault(FSAttachments.LOCKED, false);
    }

    private int getTankCapacity(float storageMultiplier) {
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(storageMultiplier * 1000L));
    }

    private float getStorageMultiplier() {
        ItemStack container = getContainer();
        if (!container.has(FSAttachments.TILE)) {
            return type.getSlotAmount();
        }
        return SizeProvider.calculateAsFactor(getStorageUpgrades(container.get(FSAttachments.TILE)), FSAttachments.FLUID_STORAGE_MODIFIER, type.getSlotAmount());
    }

    private InventoryComponent<?> getStorageUpgrades(CompoundTag tile) {
        var storageUpgrades = new InventoryComponent<>("storage_upgrades", 0, 0, 4);
        if (tile.contains("storageUpgrades")) {
            storageUpgrades.deserializeNBT(Utils.registryAccess(), tile.getCompoundOrEmpty("storageUpgrades"));
        }
        return storageUpgrades;
    }
}
