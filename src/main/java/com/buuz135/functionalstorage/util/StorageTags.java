package com.buuz135.functionalstorage.util;

import com.buuz135.functionalstorage.FunctionalStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

public class StorageTags {

    public static TagKey<Item> DRAWER = ItemTags.create(new ResourceLocation(FunctionalStorage.MOD_ID, "drawer"));
    public static TagKey<Item> IGNORE_CRAFTING_CHECK = ItemTags.create(new ResourceLocation(FunctionalStorage.MOD_ID, "ignore_crafting_check"));

}
