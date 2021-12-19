package com.buuz135.functionalstorage.util;

import com.buuz135.functionalstorage.FunctionalStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

public class StorageTags {

    public static Tags.IOptionalNamedTag<Item> DRAWER = ItemTags.createOptional(new ResourceLocation(FunctionalStorage.MOD_ID, "drawer"));

}
