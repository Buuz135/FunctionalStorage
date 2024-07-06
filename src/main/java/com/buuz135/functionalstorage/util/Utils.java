package com.buuz135.functionalstorage.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class Utils {
    public static ResourceLocation resourceLocation(String toParse) {
        return ResourceLocation.parse(toParse);
    }

    public static ResourceLocation resourceLocation(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static ItemStack deserialize(HolderLookup.Provider provider, CompoundTag tag) {
        return ItemStack.OPTIONAL_CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, provider), tag).getOrThrow().getFirst();
    }

    public static FluidStack deserializeFluid(HolderLookup.Provider provider, CompoundTag tag) {
        return FluidStack.OPTIONAL_CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, provider), tag).getOrThrow().getFirst();
    }

    public static RegistryAccess registryAccess() {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            return ServerLifecycleHooks.getCurrentServer().registryAccess();
        }
        return LogicalSidedProvider.CLIENTWORLD.get(LogicalSide.CLIENT).orElseThrow().registryAccess();
    }
}
