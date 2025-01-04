package com.buuz135.functionalstorage.inventory.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler.BigStack;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.component.SizeProvider;
import com.buuz135.functionalstorage.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static com.buuz135.functionalstorage.inventory.BigInventoryHandler.AMOUNT;
import static com.buuz135.functionalstorage.inventory.BigInventoryHandler.BIG_ITEMS;
import static com.buuz135.functionalstorage.inventory.BigInventoryHandler.STACK;

public class DrawerStackItemHandler implements IItemHandler, INBTSerializable<CompoundTag> {

    private List<BigInventoryHandler.BigStack> storedStacks;
    private ItemStack stack;
    private FunctionalStorage.DrawerType type;
    private int size;
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
            this.isCreative = tile.contains("isCreative") && tile.getBoolean("isCreative");
            var access = Utils.registryAccess();
            deserializeNBT(access, tile.getCompound("handler"));

            var upgrades = new ItemStackHandler();
            upgrades.deserializeNBT(access, tile.getCompound("storageUpgrades"));
            size = SizeProvider.calculate(upgrades, FSAttachments.ITEM_STORAGE_MODIFIER, drawerType.getSlotAmount());

            for (Tag tag : tile.getCompound("utilityUpgrades").getList("Items", Tag.TAG_COMPOUND)) {
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
            bigStack.put(STACK, this.storedStacks.get(i).getStack().saveOptional(provider));
            bigStack.putInt(AMOUNT, this.storedStacks.get(i).getAmount());
            items.put(i + "", bigStack);
        }
        compoundTag.put(BIG_ITEMS, items);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, CompoundTag nbt) {
        for (String allKey : nbt.getCompound(BIG_ITEMS).getAllKeys()) {
            this.storedStacks.get(Integer.parseInt(allKey)).setStack(Utils.deserialize(provider, nbt.getCompound(BIG_ITEMS).getCompound(allKey).getCompound(STACK)));
            this.storedStacks.get(Integer.parseInt(allKey)).setAmount(nbt.getCompound(BIG_ITEMS).getCompound(allKey).getInt(AMOUNT));
        }
    }

    @Override
    public int getSlots() {
        return type.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        BigStack bigStack = this.storedStacks.get(slot);
        if (isCreative) {
            return bigStack.getStack().copyWithCount(Integer.MAX_VALUE);
        }
        ItemStack copied = bigStack.getStack().copy();
        copied.setCount(bigStack.getAmount());
        return copied;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (isValid(slot, stack)) {
            BigStack bigStack = this.storedStacks.get(slot);
            int inserted = Math.min(getSlotLimit(slot) - bigStack.getAmount(), stack.getCount());
            if (!simulate) {
                bigStack.setStack(stack);
                bigStack.setAmount(Math.min(bigStack.getAmount() + inserted, getSlotLimit(slot)));
                onChange();
            }
            if (inserted == stack.getCount() || isVoid()) return ItemStack.EMPTY;
            return stack.copyWithCount(stack.getCount() - inserted);
        }
        return stack;
    }

    private boolean isVoid() {
        return true;
    }

    private void onChange() {
        stack.set(FSAttachments.TILE, new CompoundTag());
        stack.get(FSAttachments.TILE).put("handler", serializeNBT(Utils.registryAccess()));
    }

    private boolean isValid(int slot, @Nonnull ItemStack stack) {
        if (slot < type.getSlots()) {
            BigStack bigStack = this.storedStacks.get(slot);
            ItemStack fl = bigStack.getStack();
            return fl.isEmpty() || ItemStack.isSameItemSameComponents(fl, stack);
        }
        return false;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        if (slot < type.getSlots()) {
            BigStack bigStack = this.storedStacks.get(slot);
            if (bigStack.getStack().isEmpty()) return ItemStack.EMPTY;
            if (bigStack.getAmount() <= amount) {
                ItemStack out = bigStack.getStack().copy();
                int newAmount = bigStack.getAmount();
                if (!simulate) {
                    if (!isLocked()) bigStack.setStack(ItemStack.EMPTY);
                    bigStack.setAmount(0);
                    onChange();
                }
                out.setCount(newAmount);
                return out;
            } else {
                if (!simulate) {
                    bigStack.setAmount(bigStack.getAmount() - amount);
                    onChange();
                }
                return bigStack.getStack().copyWithCount(amount);
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean isLocked() {
        return true;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (isCreative) return Integer.MAX_VALUE;

        var stored = getStackInSlot(slot);
        long maxSize = Item.DEFAULT_MAX_STACK_SIZE;
        if (!stored.isEmpty()) {
            maxSize = stored.getMaxStackSize();
        }

        return (int) Math.min(Integer.MAX_VALUE, size * maxSize);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return !stack.isEmpty();
    }

    public List<BigStack> getStoredStacks() {
        return storedStacks;
    }

    public boolean isCreative() {
        return isCreative;
    }
}
