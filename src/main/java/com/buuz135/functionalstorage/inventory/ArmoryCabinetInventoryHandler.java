package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class ArmoryCabinetInventoryHandler implements IItemHandler, INBTSerializable<CompoundTag> {

    public List<ItemStack> stackList;

    public ArmoryCabinetInventoryHandler() {
        this.stackList = create();
    }

    @Override
    public int getSlots() {
        return FunctionalStorageConfig.ARMORY_CABINET_SIZE;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < this.stackList.size()){
            return this.stackList.get(slot);
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (isValid(slot, stack)) {
            if (!simulate) {
                this.stackList.set(slot, stack.copyWithCount(1));
                onChange();
            }

            return stack.getCount() > 1 ? stack.copyWithCount(stack.getCount() - 1) : ItemStack.EMPTY;
        }
        return stack;
    }

    public abstract void onChange();

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        var inSlot = this.stackList.get(slot).copy();
        if (amount == 0 || inSlot.isEmpty()) return inSlot;
        if (!simulate) {
            stackList.set(slot, ItemStack.EMPTY);
            onChange();
        }
        return inSlot;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return isCertifiedStack(stack);
    }

    private boolean isValid(int slot, @NotNull ItemStack stack) {
        return !stack.isEmpty() && this.stackList.get(slot).isEmpty() && isCertifiedStack(stack);
    }

    private boolean isCertifiedStack(ItemStack stack){
        if (stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).isPresent()) return false;
        if (stack.getMaxStackSize() > 1) return false;
        return stack.hasTag() || stack.isDamageableItem() || stack.isEnchantable() || stack.getItem() instanceof RecordItem || stack.getItem() instanceof HorseArmorItem;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        for (int i = 0; i < this.stackList.size(); i++) {
            ItemStack stack = this.stackList.get(i);
            if (!stack.isEmpty()){
                compoundTag.put(i + "", stack.serializeNBT());
            }
        }
        return compoundTag;
    }

    private List<ItemStack> create(){
        List<ItemStack> stackList = new ArrayList<>();
        for (int i = 0; i < FunctionalStorageConfig.ARMORY_CABINET_SIZE; i++) {
            stackList.add(ItemStack.EMPTY);
        }
        return stackList;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.stackList = create();
        for (String allKey : nbt.getAllKeys()) {
            int pos = Integer.parseInt(allKey);
            if (pos < this.stackList.size()){
                this.stackList.set(pos, ItemStack.of(nbt.getCompound(allKey)));
            }
        }
    }
}
