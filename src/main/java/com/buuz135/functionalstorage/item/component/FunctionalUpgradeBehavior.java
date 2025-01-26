package com.buuz135.functionalstorage.item.component;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.List;

public interface FunctionalUpgradeBehavior {

    ResourceKey<Registry<MapCodec<? extends FunctionalUpgradeBehavior>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "functional_upgrade_behavior"));
    Registry<MapCodec<? extends FunctionalUpgradeBehavior>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).sync(true).create();

    Codec<FunctionalUpgradeBehavior> CODEC = ResourceLocation.CODEC
            .dispatch(t -> REGISTRY.wrapAsHolder(t.codec()).getKey().location(), REGISTRY::get);

    default void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack, int upgradeSlot) {

    }

    default int getRedstoneSignal(Level level, BlockPos blockPos, BlockState state, ControllableDrawerTile<?> drawer, Direction direction, ItemStack upgradeStack, int upgradeSlot) {
        return -1;
    }

    default boolean canConnectRedstone(Level level, BlockPos blockPos, BlockState state, ControllableDrawerTile<?> drawer, Direction direction, ItemStack upgradeStack, int upgradeSlot) {
        return false;
    }

    default public List<Component> getTooltip(){
        return new ArrayList<>();
    }

    MapCodec<? extends FunctionalUpgradeBehavior> codec();
}
