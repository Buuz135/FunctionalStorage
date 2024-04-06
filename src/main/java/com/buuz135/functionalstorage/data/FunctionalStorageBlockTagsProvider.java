package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.hrznstudio.titanium.module.BlockWithTile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FunctionalStorageBlockTagsProvider extends BlockTagsProvider {

    public FunctionalStorageBlockTagsProvider(DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator.getPackOutput(), lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider p_256380_) {
        TagAppender<Block> tTagAppender = this.tag(BlockTags.MINEABLE_WITH_AXE);
        for (FunctionalStorage.DrawerType drawerType : FunctionalStorage.DRAWER_TYPES.keySet()) {
            for (var blockRegistryObject : FunctionalStorage.DRAWER_TYPES.get(drawerType).stream().map(BlockWithTile::block).collect(Collectors.toList())) {
                tTagAppender.add(blockRegistryObject.getKey());
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
                .add(FunctionalStorage.CONTROLLER_EXTENSION.getBlock())
                .add(FunctionalStorage.SIMPLE_COMPACTING_DRAWER.getBlock())
                .add(FunctionalStorage.FRAMED_DRAWER_CONTROLLER.getBlock())
                .add(FunctionalStorage.FRAMED_CONTROLLER_EXTENSION.getBlock())
                .add(FunctionalStorage.FRAMED_SIMPLE_COMPACTING_DRAWER.getBlock())
        ;
    }

}
