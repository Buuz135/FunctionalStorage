package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.CompactingFramedDrawerBlock;
import com.buuz135.functionalstorage.block.FramedDrawerBlock;
import com.buuz135.functionalstorage.block.FramedDrawerControllerBlock;
import com.buuz135.functionalstorage.block.tile.FramedControllerExtensionTile;
import com.buuz135.functionalstorage.block.tile.FramedDrawerControllerTile;
import com.buuz135.functionalstorage.block.tile.FramedDrawerTile;
import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
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
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof FramedDrawerTile tile) {
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
            if (entity instanceof FramedDrawerControllerTile tile) {
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
            if (entity instanceof FramedControllerExtensionTile tile) {
                FramedDrawerModelData framedDrawerModelData = tile.getFramedDrawerModelData();
                if (framedDrawerModelData != null) {
                    for (Map.Entry<String, Item> entry : framedDrawerModelData.getDesign().entrySet()) {
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
            if (itemStack.getItem() instanceof BlockItem item && (item.getBlock() instanceof FramedDrawerBlock || item.getBlock() instanceof CompactingFramedDrawerBlock || item.getBlock() instanceof FramedDrawerControllerBlock)) {
                FramedDrawerModelData framedDrawerModelData = FramedDrawerBlock.getDrawerModelData(itemStack);
                if (framedDrawerModelData != null) {
                    for (Map.Entry<String, Item> entry: framedDrawerModelData.getDesign().entrySet()) {
                        if (entry.getValue() instanceof BlockItem blockItem) {
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
    public static void blockColors(RegisterColorHandlersEvent.Block event) {
        Block block1 = BuiltInRegistries.BLOCK.get(new ResourceLocation(FunctionalStorage.MOD_ID, "framed_1"));
        event.register(new FramedColors(), block1);
        Block block2 = BuiltInRegistries.BLOCK.get(new ResourceLocation(FunctionalStorage.MOD_ID, "framed_2"));
        event.register(new FramedColors(), block2);
        Block block4 = BuiltInRegistries.BLOCK.get(new ResourceLocation(FunctionalStorage.MOD_ID, "framed_4"));
        event.register(new FramedColors(), block4);

        Block block5 = BuiltInRegistries.BLOCK.get(new ResourceLocation(FunctionalStorage.MOD_ID, "compacting_framed_drawer"));
        event.register(new FramedColors(), block5);

        Block controller = BuiltInRegistries.BLOCK.get(new ResourceLocation(FunctionalStorage.MOD_ID, "framed_storage_controller"));
        event.register(new FramedColors(), controller);

        Block controllerExtension = BuiltInRegistries.BLOCK.get(new ResourceLocation(FunctionalStorage.MOD_ID, "framed_controller_extension"));
        event.register(new FramedColors(), controllerExtension);
    }
}
