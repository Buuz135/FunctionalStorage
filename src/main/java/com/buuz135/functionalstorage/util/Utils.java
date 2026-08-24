package com.buuz135.functionalstorage.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class Utils {
    public static Identifier resourceLocation(String toParse) {
        return Identifier.parse(toParse);
    }

    public static Identifier resourceLocation(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    public static ItemStack deserialize(HolderLookup.Provider provider, CompoundTag tag) {
        return ItemStack.OPTIONAL_CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, provider), tag).getOrThrow().getFirst();
    }

    public static CompoundTag serialize(HolderLookup.Provider provider, ItemStack stack) {
        Tag encoded = ItemStack.OPTIONAL_CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, provider), stack).getOrThrow();
        return encoded instanceof CompoundTag compound ? compound : new CompoundTag();
    }

    public static FluidStack deserializeFluid(HolderLookup.Provider provider, CompoundTag tag) {
        return FluidStack.OPTIONAL_CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, provider), tag).getOrThrow().getFirst();
    }

    public static RegistryAccess registryAccess() {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            return ServerLifecycleHooks.getCurrentServer().registryAccess();
        }
        return Minecraft.getInstance().level.registryAccess();
    }
}
