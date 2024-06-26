package com.buuz135.functionalstorage.compat.jei;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.recipe.CustomCompactingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

public class CompactingRecipeCategory implements IRecipeCategory<CustomCompactingRecipe> {

    public static RecipeType<CustomCompactingRecipe> TYPE = RecipeType.create(FunctionalStorage.MOD_ID, "dissolution", CustomCompactingRecipe.class);

    private final IGuiHelper guiHelper;

    public CompactingRecipeCategory(IGuiHelper guiHelper) {
        this.guiHelper = guiHelper;
    }

    @Override
    public RecipeType<CustomCompactingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Custom Compacting");
    }

    @Override
    public IDrawable getBackground() {
        return guiHelper.createBlankDrawable(64,64);
    }

    @Override
    public IDrawable getIcon() {
        return null;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CustomCompactingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 40).addIngredients(Ingredient.of(recipe.lower_input));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 24, 8).addIngredients(Ingredient.of(recipe.higher_input));
    }

    @Override
    public void draw(CustomCompactingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        //com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "textures/block/compacting_drawer_front.png")
        guiGraphics.blit(com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "textures/block/compacting_drawer_front.png"), 0,0, 64,64, 64, 64, 64,64);
    }
}
