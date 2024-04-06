package com.buuz135.functionalstorage.recipe;

import com.hrznstudio.titanium.util.TagUtil;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.buuz135.functionalstorage.FunctionalStorage.MOD_ID;

public class DrawerlessWoodIngredient extends Ingredient {
    public static final Codec<DrawerlessWoodIngredient> CODEC = Codec.unit(DrawerlessWoodIngredient::new);
    public static Holder<IngredientType<?>> TYPE;
    public static final ResourceLocation NAME = new ResourceLocation(MOD_ID, "woodless");

    private List<Item> woodless;

    public DrawerlessWoodIngredient() {
        super(Stream.empty());
    }

    @Override
    public ItemStack[] getItems() {
        return getWoods().stream().map(ItemStack::new).toArray(ItemStack[]::new);
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return getWoods().contains(stack.getItem());
    }

    @Override
    public boolean synchronizeWithContents() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE.value();
    }

    private List<Item> getWoods(){
        if (woodless == null){
            woodless = TagUtil.getAllEntries(BuiltInRegistries.ITEM, ItemTags.PLANKS).stream().filter(item -> !BuiltInRegistries.ITEM.getKey(item).getNamespace().equalsIgnoreCase("minecraft")).collect(Collectors.toList());
            if (woodless.isEmpty()){
                woodless.add(Items.OAK_PLANKS);
            }
        }
        return woodless;
    }

}
