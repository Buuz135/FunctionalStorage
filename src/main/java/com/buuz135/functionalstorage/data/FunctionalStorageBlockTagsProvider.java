package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.hrznstudio.titanium.module.BlockWithTile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FunctionalStorageBlockTagsProvider extends BlockTagsProvider {

    public FunctionalStorageBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, lookupProvider, modId);
    }

    @Override
    protected void addTags(HolderLookup.Provider p_256380_) {
        TagAppender<Block, Block> tTagAppender = this.tag(BlockTags.MINEABLE_WITH_AXE);
        TagAppender<Block, Block> relocation = this.tag(Tags.Blocks.RELOCATION_NOT_SUPPORTED);
        for (FunctionalStorage.DrawerType drawerType : FunctionalStorage.DRAWER_TYPES.keySet()) {
            for (var blockRegistryObject : FunctionalStorage.DRAWER_TYPES.get(drawerType).stream().map(BlockWithTile::block).collect(Collectors.toList())) {
                tTagAppender.add(blockRegistryObject.get());
                relocation.add(blockRegistryObject.get());
            }
        }
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(FunctionalStorage.COMPACTING_DRAWER.getBlock())
                .add(FunctionalStorage.DRAWER_CONTROLLER.getBlock())
                .add(FunctionalStorage.ARMORY_CABINET.getBlock())
                .add(FunctionalStorage.ENDER_DRAWER.getBlock())
                .add(FunctionalStorage.FRAMED_COMPACTING_DRAWER.getBlock())
                .add(FunctionalStorage.FLUID_DRAWER_1.getBlock())
                .add(FunctionalStorage.FLUID_DRAWER_2.getBlock())
                .add(FunctionalStorage.FLUID_DRAWER_4.getBlock())
                .add(FunctionalStorage.FRAMED_FLUID_DRAWER_1.getBlock())
                .add(FunctionalStorage.FRAMED_FLUID_DRAWER_2.getBlock())
                .add(FunctionalStorage.FRAMED_FLUID_DRAWER_4.getBlock())
                .add(FunctionalStorage.CONTROLLER_EXTENSION.getBlock())
                .add(FunctionalStorage.SIMPLE_COMPACTING_DRAWER.getBlock())
                .add(FunctionalStorage.FRAMED_DRAWER_CONTROLLER.getBlock())
                .add(FunctionalStorage.FRAMED_CONTROLLER_EXTENSION.getBlock())
                .add(FunctionalStorage.FRAMED_SIMPLE_COMPACTING_DRAWER.getBlock());

        this.tag(Tags.Blocks.RELOCATION_NOT_SUPPORTED)
                .add(FunctionalStorage.COMPACTING_DRAWER.getBlock())
                .add(FunctionalStorage.DRAWER_CONTROLLER.getBlock())
                .add(FunctionalStorage.ARMORY_CABINET.getBlock())
                .add(FunctionalStorage.ENDER_DRAWER.getBlock())
                .add(FunctionalStorage.FRAMED_COMPACTING_DRAWER.getBlock())
                .add(FunctionalStorage.FLUID_DRAWER_1.getBlock())
                .add(FunctionalStorage.FLUID_DRAWER_2.getBlock())
                .add(FunctionalStorage.FLUID_DRAWER_4.getBlock())
                .add(FunctionalStorage.FRAMED_FLUID_DRAWER_1.getBlock())
                .add(FunctionalStorage.FRAMED_FLUID_DRAWER_2.getBlock())
                .add(FunctionalStorage.FRAMED_FLUID_DRAWER_4.getBlock())
                .add(FunctionalStorage.CONTROLLER_EXTENSION.getBlock())
                .add(FunctionalStorage.SIMPLE_COMPACTING_DRAWER.getBlock())
                .add(FunctionalStorage.FRAMED_DRAWER_CONTROLLER.getBlock())
                .add(FunctionalStorage.FRAMED_CONTROLLER_EXTENSION.getBlock())
                .add(FunctionalStorage.FRAMED_SIMPLE_COMPACTING_DRAWER.getBlock())
        ;
    }

}
