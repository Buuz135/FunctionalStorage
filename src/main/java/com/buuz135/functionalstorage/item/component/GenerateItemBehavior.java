package com.buuz135.functionalstorage.item.component;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;

public record GenerateItemBehavior(ItemStack item) implements FunctionalUpgradeBehavior {
    public static final MapCodec<GenerateItemBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            ItemStack.STRICT_CODEC.fieldOf("item").forGetter(GenerateItemBehavior::item)
    ).apply(in, GenerateItemBehavior::new));

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack, int upgradeSlot) {
        var capability = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        if (capability != null) {
            ItemHandlerHelper.insertItem(capability, item.copy(), false);
        }
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }

    @Override
    public List<Component> getTooltip() {
        var list = FunctionalUpgradeBehavior.super.getTooltip();
        list.add(Component.translatable("functionalupgrade.desc.generate_item", item.getCount(), Component.translatable(item.getDescriptionId())));
        return list;
    }
}

