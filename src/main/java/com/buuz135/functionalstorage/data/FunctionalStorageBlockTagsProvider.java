package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

public class FunctionalStorageBlockTagsProvider extends BlockTagsProvider {

    public FunctionalStorageBlockTagsProvider(DataGenerator p_126530_, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_126530_, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        TagAppender<Block> tTagAppender = this.tag(BlockTags.MINEABLE_WITH_AXE);
        for (FunctionalStorage.DrawerType drawerType : FunctionalStorage.DRAWER_TYPES.keySet()) {
            for (RegistryObject<Block> blockRegistryObject : FunctionalStorage.DRAWER_TYPES.get(drawerType).stream().map(Pair::getLeft).collect(Collectors.toList())) {
                tTagAppender.add(blockRegistryObject.get());
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
        ;
    }
}
