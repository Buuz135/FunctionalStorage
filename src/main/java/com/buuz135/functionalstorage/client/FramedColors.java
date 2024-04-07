package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.CompactingFramedDrawerBlock;
import com.buuz135.functionalstorage.block.FramedBlock;
import com.buuz135.functionalstorage.block.FramedDrawerBlock;
import com.buuz135.functionalstorage.block.FramedDrawerControllerBlock;
import com.buuz135.functionalstorage.block.tile.FramedTile;
import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Mod.EventBusSubscriber(modid = FunctionalStorage.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FramedColors implements BlockColor, ItemColor {

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (level != null && pos != null && tintIndex == 0) {
            if (level.getBlockEntity(pos) instanceof FramedTile tile) {
                FramedDrawerModelData framedDrawerModelData = tile.getFramedDrawerModelData();
                if (framedDrawerModelData != null) {
                    for (Map.Entry<String, Item> entry: framedDrawerModelData.getDesign().entrySet()) {
                        if (entry.getValue() instanceof BlockItem blockItem) {
                            if (BuiltInRegistries.ITEM.getKey(blockItem).getNamespace().equals(FunctionalStorage.MOD_ID))
                                continue;
                            BlockState state1 = blockItem.getBlock().defaultBlockState();
                            int color = Minecraft.getInstance().getBlockColors().getColor(state1, level, pos, tintIndex);
                            if (color != -1)
                                return color;
                        }
                    }
                }
            }
        }
        return 0xFFFFFF;
    }

    @Override
    public int getColor(ItemStack itemStack, int tintIndex) {
        if (tintIndex == 0) {
            if (itemStack.getItem() instanceof BlockItem item && item.getBlock() instanceof FramedBlock) {
                FramedDrawerModelData framedDrawerModelData = FramedDrawerBlock.getDrawerModelData(itemStack);
                if (framedDrawerModelData != null) {
                    for (Map.Entry<String, Item> entry: framedDrawerModelData.getDesign().entrySet()) {
                        if (entry.getValue() instanceof BlockItem) {
                            int color = Minecraft.getInstance().getItemColors().getColor(itemStack, tintIndex);
                            if (color != -1)
                                return color;
                        }
                    }
                }
            }
        }
        return 0xFFFFFF;
    }

    @SubscribeEvent
    static void blockColors(RegisterColorHandlersEvent.Block event) {
        final var instance = new FramedColors();
        FunctionalStorage.FRAMED_BLOCKS.forEach(bl -> event.register(instance, bl));
    }

    @SubscribeEvent
    static void itemColors(RegisterColorHandlersEvent.Item event) {
        final var instance = new FramedColors();
        FunctionalStorage.FRAMED_BLOCKS.forEach(bl -> event.register(instance, bl));
    }
}
