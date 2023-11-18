package com.buuz135.functionalstorage.compat.jei;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.recipe.CustomCompactingRecipe;
import com.hrznstudio.titanium.util.RecipeUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    public static ResourceLocation PLUGIN_ID = new ResourceLocation(FunctionalStorage.MOD_ID, "main");

    private CompactingRecipeCategory compactingRecipeCategory;

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IModPlugin.super.registerCategories(registration);
        registration.addRecipeCategories(compactingRecipeCategory = new CompactingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(CompactingRecipeCategory.TYPE, RecipeUtil.getRecipes(Minecraft.getInstance().level, (RecipeType<CustomCompactingRecipe>) FunctionalStorage.CUSTOM_COMPACTING_RECIPE_TYPE.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        IModPlugin.super.registerRecipeCatalysts(registration);
        registration.addRecipeCatalyst(new ItemStack(FunctionalStorage.COMPACTING_DRAWER.getKey().get()), CompactingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(FunctionalStorage.FRAMED_COMPACTING_DRAWER.getKey().get()), CompactingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(FunctionalStorage.SIMPLE_COMPACTING_DRAWER.getKey().get()), CompactingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(FunctionalStorage.FRAMED_SIMPLE_COMPACTING_DRAWER.getKey().get()), CompactingRecipeCategory.TYPE);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }
}
