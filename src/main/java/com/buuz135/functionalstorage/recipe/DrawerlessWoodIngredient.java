package com.buuz135.functionalstorage.recipe;

import com.hrznstudio.titanium.util.TagUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.buuz135.functionalstorage.FunctionalStorage.MOD_ID;

public class DrawerlessWoodIngredient implements ICustomIngredient {
    public static final MapCodec<DrawerlessWoodIngredient> CODEC = MapCodec.unit(DrawerlessWoodIngredient::new);
    public static Holder<IngredientType<?>> TYPE;
    public static final ResourceLocation NAME = com.buuz135.functionalstorage.util.Utils.resourceLocation(MOD_ID, "woodless");

    private Set<Item> woodless;

    @Override
    public Stream<ItemStack> getItems() {
        return getWoods().stream().map(ItemStack::new);
    }

    @Override
    public boolean test(ItemStack stack) {
        return getWoods().contains(stack.getItem());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE.value();
    }

    private Set<Item> getWoods(){
        if (woodless == null){
            woodless = TagUtil.getAllEntries(BuiltInRegistries.ITEM, ItemTags.PLANKS).stream().filter(item -> !BuiltInRegistries.ITEM.getKey(item).getNamespace().equalsIgnoreCase(ResourceLocation.DEFAULT_NAMESPACE)).collect(Collectors.toSet());
            woodless.add(Items.OAK_PLANKS);
        }
        return woodless;
    }

}
