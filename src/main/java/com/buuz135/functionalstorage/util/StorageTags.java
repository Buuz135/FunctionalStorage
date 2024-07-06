package com.buuz135.functionalstorage.util;

import com.buuz135.functionalstorage.FunctionalStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class StorageTags {

    public static final TagKey<Item> DRAWER = ItemTags.create(com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "drawer"));
    public static final TagKey<Item> IGNORE_CRAFTING_CHECK = ItemTags.create(com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "ignore_crafting_check"));

}
