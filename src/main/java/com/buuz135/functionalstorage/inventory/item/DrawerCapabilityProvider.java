package com.buuz135.functionalstorage.inventory.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DrawerCapabilityProvider implements ICapabilityProvider {

    private final ItemStack stack;
    private final DrawerStackItemHandler drawerStackItemHandler;
    private final LazyOptional<IItemHandler> itemHandler;

    public DrawerCapabilityProvider(ItemStack stack, FunctionalStorage.DrawerType type) {
        this.stack = stack;
        this.drawerStackItemHandler = new DrawerStackItemHandler(stack, type);
        this.itemHandler = LazyOptional.of(() -> this.drawerStackItemHandler);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) return this.itemHandler.cast();
        return LazyOptional.empty();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) return this.itemHandler.cast();
        return LazyOptional.empty();
    }
}
