package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.world.EnderSavedData;
import net.minecraft.nbt.CompoundTag;

public class EnderInventoryHandler extends BigInventoryHandler implements ILockable {

    public static String NBT_LOCKED = "Locked";
    public static String NBT_VOID = "Void";

    private final EnderSavedData manager;
    private String frequency;
    private boolean locked;
    private boolean voidItems;

    public EnderInventoryHandler(String frequency, EnderSavedData manager) {
        super(FunctionalStorage.DrawerType.X_1);
        this.manager = manager;
        this.frequency = frequency;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = super.serializeNBT();
        compoundTag.putBoolean(NBT_LOCKED, this.locked);
        compoundTag.putBoolean(NBT_VOID, this.voidItems);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        this.locked = nbt.getBoolean(NBT_LOCKED);
        this.voidItems = nbt.getBoolean(NBT_VOID);
    }

    //TODO Implement all of these methods
    @Override
    public void onChange() {
        manager.setDirty();
    }

    @Override
    public int getSlotLimit(int slot) {
        double stackSize = 1;
        if (!getStoredStacks().get(slot).getStack().isEmpty()) {
            stackSize = getStoredStacks().get(slot).getStack().getMaxStackSize() / 64D;
        }
        return (int) Math.floor(Math.min(Integer.MAX_VALUE, FunctionalStorage.DrawerType.X_1.getSlotAmount() * 4) * stackSize);
    }

    @Override
    public int getMultiplier() {
        return 1;
    }

    @Override
    public boolean isVoid() {
        return voidItems;
    }

    @Override
    public boolean hasDowngrade() {
        return false;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        manager.setDirty();
    }

    public void setVoidItems(boolean voidItems) {
        this.voidItems = voidItems;
        manager.setDirty();
    }
}
