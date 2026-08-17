package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.FramedTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.List;

@EventBusSubscriber(modid = FunctionalStorage.MOD_ID, value = Dist.CLIENT)
public final class FramedColors implements BlockTintSource {
    @Override
    public int color(BlockState state) {
        return 0xFFFFFFFF;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof FramedTile tile) || tile.getFramedDrawerModelData() == null) {
            return color(state);
        }
        for (var entry : tile.getFramedDrawerModelData().getDesign().entrySet()) {
            if (!(entry.getValue() instanceof BlockItem blockItem)
                    || BuiltInRegistries.ITEM.getKey(blockItem).getNamespace().equals(FunctionalStorage.MOD_ID)) {
                continue;
            }
            BlockState material = blockItem.getBlock().defaultBlockState();
            BlockTintSource source = Minecraft.getInstance().getBlockColors().getTintSource(material, 0);
            if (source != null) {
                return source.colorInWorld(material, level, pos);
            }
        }
        return color(state);
    }

    @SubscribeEvent
    static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(new FramedColors()), FunctionalStorage.FRAMED_BLOCKS.toArray(net.minecraft.world.level.block.Block[]::new));
    }
}
