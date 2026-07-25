/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.buuz135.functionalstorage.recipe;

import com.buuz135.functionalstorage.util.Utils;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.List;
import java.util.stream.Stream;

import static com.buuz135.functionalstorage.FunctionalStorage.MOD_ID;


public class TagWithoutComponentIngredient implements ICustomIngredient {

    public static final MapCodec<TagWithoutComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            TagKey.codec(BuiltInRegistries.ITEM.key()).fieldOf("tag").forGetter(TagWithoutComponentIngredient::getTag))
                    .apply(builder, TagWithoutComponentIngredient::new));
    public static Holder<IngredientType<?>> TYPE;
    public static final ResourceLocation NAME = Utils.resourceLocation(MOD_ID, "tag_without_component");

    private final TagKey<Item> tag;

    public TagWithoutComponentIngredient(TagKey<Item> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(tag) && stack.getComponentsPatch().isEmpty();
    }

    public List<ItemStack> getListOfItems(){
        List<ItemStack> list = Lists.newArrayList();

        for(Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
            list.add(new ItemStack(holder));
        }

        if (list.isEmpty()) {
            ItemStack itemStack = new ItemStack(Blocks.BARRIER);
            itemStack.set(DataComponents.CUSTOM_NAME, Component.literal("Empty Tag: " + String.valueOf(this.tag.location())));
            list.add(itemStack);
        }
        return list;
    }

    @Override
    public Stream<ItemStack> getItems() {
        return getListOfItems().stream();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE.value();
    }

    public TagKey<Item> getTag() {
        return tag;
    }
}
