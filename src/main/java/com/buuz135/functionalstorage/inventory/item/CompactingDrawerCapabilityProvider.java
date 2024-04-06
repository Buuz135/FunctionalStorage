package com.buuz135.functionalstorage.inventory.item;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompactingDrawerCapabilityProvider implements ICapabilityProvider {

    private final ItemStack stack;
    private final CompactingStackItemHandler drawerStackItemHandler;
    private final LazyOptional<IItemHandler> itemHandler;

    public CompactingDrawerCapabilityProvider(ItemStack stack, int slots) {
        this.stack = stack;
        this.drawerStackItemHandler = new CompactingStackItemHandler(stack, slots);
        this.itemHandler = LazyOptional.of(() -> this.drawerStackItemHandler);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap.equals(Capabilities.ITEM_HANDLER)) return this.itemHandler.cast();
        return LazyOptional.empty();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap.equals(Capabilities.ITEM_HANDLER)) return this.itemHandler.cast();
        return LazyOptional.empty();
    }
}
