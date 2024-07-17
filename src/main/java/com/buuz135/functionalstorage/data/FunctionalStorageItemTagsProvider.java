package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.module.BlockWithTile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class FunctionalStorageItemTagsProvider extends ItemTagsProvider {


    public FunctionalStorageItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagLookup<Block>> lookupCompletableFuture, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, completableFuture, lookupCompletableFuture, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider p_256380_) {
        var tTagAppender = this.tag(StorageTags.DRAWER);
        for (FunctionalStorage.DrawerType drawerType : FunctionalStorage.DRAWER_TYPES.keySet()) {
            FunctionalStorage.DRAWER_TYPES.get(drawerType).stream().map(BlockWithTile::block)
                    .forEach(ho -> tTagAppender.add(ho.get().asItem()));
        }
        for (FunctionalStorage.DrawerType drawerType : FunctionalStorage.DRAWER_TYPES.keySet()) {
            var byTypeAppender = this.tag(drawerType.getTag());
            FunctionalStorage.DRAWER_TYPES.get(drawerType).stream().map(BlockWithTile::block)
                    .forEach(ho -> byTypeAppender.add(ho.get().asItem()));
        }
    }

    @Override
    public String getName()
    {
        return "Functional Storage Item Tags";
    }

}
