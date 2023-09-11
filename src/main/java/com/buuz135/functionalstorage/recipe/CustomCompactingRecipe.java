package com.buuz135.functionalstorage.recipe;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.hrznstudio.titanium.recipe.serializer.GenericSerializer;
import com.hrznstudio.titanium.recipe.serializer.SerializableRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class CustomCompactingRecipe extends SerializableRecipe {

    public static List<CustomCompactingRecipe> RECIPES = new ArrayList<>();

    public ItemStack lower_input = ItemStack.EMPTY;
    public ItemStack higher_input = ItemStack.EMPTY;

    public CustomCompactingRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    public CustomCompactingRecipe(ResourceLocation resourceLocation, ItemStack lower_input, ItemStack higher_input) {
        super(resourceLocation);
        this.lower_input = lower_input;
        this.higher_input = higher_input;
        RECIPES.add(this);
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public GenericSerializer<? extends SerializableRecipe> getSerializer() {
        return (GenericSerializer<? extends SerializableRecipe>) FunctionalStorage.CUSTOM_COMPACTING_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return FunctionalStorage.CUSTOM_COMPACTING_RECIPE_TYPE.get();
    }

    public ItemStack getLower_input() {
        return lower_input;
    }

    public ItemStack getHigher_input() {
        return higher_input;
    }
}
