package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.util.StorageTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class FunctionalStorageBlockTagsProvider extends BlockTagsProvider {

    public FunctionalStorageBlockTagsProvider(DataGenerator p_126530_, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_126530_, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        TagAppender<Block> tTagAppender = this.tag(BlockTags.MINEABLE_WITH_AXE);
        for (FunctionalStorage.DrawerType drawerType : FunctionalStorage.DRAWER_TYPES.keySet()) {
            for (RegistryObject<Block> blockRegistryObject : FunctionalStorage.DRAWER_TYPES.get(drawerType)) {
                tTagAppender.add(blockRegistryObject.get());
            }
        }
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(FunctionalStorage.COMPACTING_DRAWER.get()).add(FunctionalStorage.DRAWER_CONTROLLER.get()).add(FunctionalStorage.ARMORY_CABINET.get()).add(FunctionalStorage.ENDER_DRAWER.get());
    }
}
