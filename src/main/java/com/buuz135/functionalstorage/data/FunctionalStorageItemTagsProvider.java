package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.module.BlockWithTile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
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
        this.tag(FunctionalStorage.DrawerType.X_1.getTag()).add(FunctionalStorage.FLUID_DRAWER_1.getBlock().asItem());
        this.tag(FunctionalStorage.DrawerType.X_2.getTag()).add(FunctionalStorage.FLUID_DRAWER_2.getBlock().asItem());
        this.tag(FunctionalStorage.DrawerType.X_4.getTag()).add(FunctionalStorage.FLUID_DRAWER_4.getBlock().asItem());
        this.tag(StorageTags.FLUID_DRAWER).add(FunctionalStorage.FLUID_DRAWER_1.getBlock().asItem(), FunctionalStorage.FLUID_DRAWER_2.getBlock().asItem(), FunctionalStorage.FLUID_DRAWER_4.getBlock().asItem());
        this.tag(StorageTags.ARMORY_CABINET_INSERTABLE)
                .addTag(ItemTags.SWORDS)
                .addTag(ItemTags.AXES)
                .addTag(ItemTags.HOES)
                .addTag(ItemTags.PICKAXES)
                .addTag(ItemTags.SHOVELS)
                .addTag(ItemTags.HEAD_ARMOR)
                .addTag(ItemTags.CHEST_ARMOR)
                .addTag(ItemTags.LEG_ARMOR)
                .addTag(ItemTags.FOOT_ARMOR)
                .addTag(ItemTags.BOW_ENCHANTABLE)
                .addTag(ItemTags.CROSSBOW_ENCHANTABLE)
                .addTag(ItemTags.TRIDENT_ENCHANTABLE)
                .addTag(ItemTags.MACE_ENCHANTABLE)
                .add(Items.ELYTRA, Items.SHIELD, Items.FISHING_ROD, Items.FLINT_AND_STEEL, Items.SHEARS, Items.BRUSH, Items.ENCHANTED_BOOK);
    }

    @Override
    public String getName()
    {
        return "Functional Storage Item Tags";
    }

}
