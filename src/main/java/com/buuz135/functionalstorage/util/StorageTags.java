package com.buuz135.functionalstorage.util;

import com.buuz135.functionalstorage.FunctionalStorage;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class StorageTags {

    public static final TagKey<Item> DRAWER = ItemTags.create(Utils.resourceLocation(FunctionalStorage.MOD_ID, "drawer"));
    public static final TagKey<Item> FLUID_DRAWER = ItemTags.create(Utils.resourceLocation(FunctionalStorage.MOD_ID, "fluid_drawer"));
    public static final TagKey<Item> ARMORY_CABINET_INSERTABLE = ItemTags.create(Utils.resourceLocation(FunctionalStorage.MOD_ID, "armory_cabinet_insertable"));
    public static final TagKey<Item> IGNORE_CRAFTING_CHECK = ItemTags.create(Utils.resourceLocation(FunctionalStorage.MOD_ID, "ignore_crafting_check"));
    public static final TagKey<Item> DRAWER_STORAGE_DENYLIST = ItemTags.create(Utils.resourceLocation(FunctionalStorage.MOD_ID, "drawer_storage_denylist"));
    public static final TagKey<Item> CREATIVE_VENDING_UPGRADE_INCOMPATIBLE = ItemTags.create(Utils.resourceLocation(FunctionalStorage.MOD_ID, "creative_vending_upgrade_incompatible"));
    public static final TagKey<Fluid> FLUID_DRAWER_STORAGE_DENYLIST = FluidTags.create(Utils.resourceLocation(FunctionalStorage.MOD_ID, "fluid_drawer_storage_denylist"));

}
