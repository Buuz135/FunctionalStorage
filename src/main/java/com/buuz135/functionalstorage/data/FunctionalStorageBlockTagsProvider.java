package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.tuple.Pair;
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
            for (RegistryObject<Block> blockRegistryObject : FunctionalStorage.DRAWER_TYPES.get(drawerType).stream().map(Pair::getLeft).collect(Collectors.toList())) {
                tTagAppender.add(blockRegistryObject.getKey());
            }
        }
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(FunctionalStorage.COMPACTING_DRAWER.getLeft().get())
                .add(FunctionalStorage.DRAWER_CONTROLLER.getLeft().get())
                .add(FunctionalStorage.ARMORY_CABINET.getLeft().get())
                .add(FunctionalStorage.ENDER_DRAWER.getLeft().get())
                .add(FunctionalStorage.FRAMED_COMPACTING_DRAWER.getLeft().get())
                .add(FunctionalStorage.FLUID_DRAWER_1.getLeft().get())
                .add(FunctionalStorage.FLUID_DRAWER_2.getLeft().get())
                .add(FunctionalStorage.FLUID_DRAWER_4.getLeft().get())
                .add(FunctionalStorage.CONTROLLER_EXTENSION.getLeft().get())
                .add(FunctionalStorage.SIMPLE_COMPACTING_DRAWER.getLeft().get())
                .add(FunctionalStorage.FRAMED_DRAWER_CONTROLLER.getLeft().get())
                .add(FunctionalStorage.FRAMED_CONTROLLER_EXTENSION.getLeft().get())
        ;
    }

}
