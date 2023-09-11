package com.buuz135.functionalstorage.util;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.recipe.CustomCompactingRecipe;
import com.hrznstudio.titanium.util.RecipeUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;


/**
 * Some code in this class is from the original Storage Drawers
 * findSimilar
 * findAllMatchingRecipes
 * tryMatch
 * findLowerTier
 */
public class CompactingUtil {

    private final Level level;
    private List<Result> results;
    private final int resultAmount;
    private final List<CustomCompactingRecipe> recipes;

    public CompactingUtil(Level level, int resultAmount) {
        this.level = level;
        this.resultAmount = resultAmount;
        this.results = new ArrayList<>();
        this.recipes = (List<CustomCompactingRecipe>) RecipeUtil.getRecipes(level, FunctionalStorage.CUSTOM_COMPACTING_RECIPE_TYPE.get());
    }

    public void setup(ItemStack stack){
        results.add(new Result(stack, 1));
        Result result = findUpperTier(stack);
        if (!result.getResult().isEmpty()){
            results.add(result);
            if (results.size() < this.resultAmount) {
                result = findUpperTier(result.getResult());
                if (!result.getResult().isEmpty()) {
                    result.setNeeded(result.getNeeded() * this.results.get(this.results.size() - 1).getNeeded());
                    results.add(result);
                }
            }
        }
        boolean canFind = true;
        while (canFind && results.size() < resultAmount) {
            result = findLowerTier(results.get(0).getResult());
            if (!result.getResult().isEmpty()) {
                for (Result result1 : results) {
                    result1.setNeeded(result1.getNeeded() * result.getNeeded());
                }
                result.setNeeded(1);
                results.add(0, result);
            } else {
                canFind = false;
            }
        }
        while (results.size() < resultAmount) {
            results.add(0, new Result(ItemStack.EMPTY, 1));
        }
        results.stream().filter(result1 -> result1.getResult().getCount() > 0).forEach(result1 -> result1.setNeeded(result1.getNeeded() / result1.getResult().getCount()));
    }

    public List<Result> getResults() {
        return results;
    }

    private Result findUpperTier(ItemStack stack){
        for (CustomCompactingRecipe recipe : this.recipes) {
            if (ItemStack.isSameItem(recipe.lower_input, stack)) {
                return new Result(ItemHandlerHelper.copyStackWithSize(recipe.higher_input, 1), recipe.lower_input.getCount());
            }
        }
        //Checking 3x3
        int sizeCheck = 9;
        CraftingContainer container = createContainerAndFill(3, stack);
        List<ItemStack> outputs = findAllMatchingRecipes(container);
        List<ItemStack> realOutputs = new ArrayList<>();
        if (outputs.size() == 0){
            //Checking 2x2
            sizeCheck = 4;
            container = createContainerAndFill(2, stack);
            outputs = findAllMatchingRecipes(container);
        }
        if (stack.is(StorageTags.IGNORE_CRAFTING_CHECK)){
            realOutputs = outputs;
        }else if (outputs.size() > 0){
            for (ItemStack output : outputs) {
                container = createContainerAndFill(1, output);
                List<ItemStack> reversed = findAllMatchingRecipes(container);
                for (ItemStack reversedStack : reversed) {
                    if (reversedStack.getCount() != sizeCheck || !ItemStack.isSameItem(reversedStack, stack)) {
                        continue;
                    }
                    realOutputs.add(output);
                }
            }
        }

        ItemStack similar = findSimilar(stack, realOutputs);
        if (!similar.isEmpty()){
            return new Result(similar, sizeCheck);
        }
        if (realOutputs.size() > 0){
            return new Result(realOutputs.get(0), sizeCheck);
        }
        return new Result(ItemStack.EMPTY, 0);
    }

    private Result findLowerTier(ItemStack stack){
        for (CustomCompactingRecipe recipe : this.recipes) {
            if (ItemStack.isSameItem(recipe.higher_input, stack)) {
                return new Result(ItemHandlerHelper.copyStackWithSize(recipe.lower_input, 1), recipe.lower_input.getCount());
            }
        }
        List<ItemStack> candidates = new ArrayList<>();
        Map<ItemStack, Integer> candidatesRate = new HashMap<>();
        for (CraftingRecipe craftingRecipe : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            ItemStack output = craftingRecipe.getResultItem(this.level.registryAccess());
            if (!ItemStack.isSameItem(stack, output)) continue;
            ItemStack match = tryMatch(stack, craftingRecipe.getIngredients());
            if (!match.isEmpty()){
                int recipeSize = craftingRecipe.getIngredients().size();
                if (stack.is(StorageTags.IGNORE_CRAFTING_CHECK)){
                    candidates.add(match);
                    candidatesRate.put(match, recipeSize);
                }
                CraftingContainer container = createContainerAndFill(1, output);
                List<ItemStack> matchStacks = findAllMatchingRecipes(container);
                for (ItemStack matchStack : matchStacks) {
                    if (ItemStack.isSameItem(match, matchStack) && matchStack.getCount() == recipeSize) {
                        candidates.add(match);
                        candidatesRate.put(match, recipeSize);
                        break;
                    }
                }
            }
        }

        ItemStack similar = findSimilar(stack, candidates);
        if (!similar.isEmpty()){
            return new Result(similar, candidatesRate.get(similar));
        }
        if (!candidates.isEmpty()) {
            return new Result(candidates.get(0).copy(), candidatesRate.get(candidates.get(0)));
        }
        return new Result(ItemStack.EMPTY, 0);
    }

    private List<ItemStack> findAllMatchingRecipes(CraftingContainer crafting) {
        List<ItemStack> candidates = new ArrayList<>();
        for (CraftingRecipe recipe : level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, crafting, level)) {
            if (recipe.matches(crafting, level)) {
                ItemStack result = recipe.assemble(crafting, this.level.registryAccess());
                if (!result.isEmpty())
                    candidates.add(result);
            }
        }
        return candidates;
    }

    private ItemStack findSimilar(ItemStack reference, List<ItemStack> candidates) {
        ResourceLocation referenceName = ForgeRegistries.ITEMS.getKey(reference.getItem());
        if (referenceName != null) {
            for (ItemStack candidate : candidates) {
                ResourceLocation matchName = ForgeRegistries.ITEMS.getKey(candidate.getItem());
                if (matchName != null) {
                    if (referenceName.getNamespace().equals(matchName.getNamespace()))
                        return candidate;
                }
            }
        }
        return candidates.size() > 0 ? candidates.get(0) : ItemStack.EMPTY;
    }


    private ItemStack tryMatch(ItemStack stack, NonNullList<Ingredient> ingredients) {
        if (ingredients.size() != 9 && ingredients.size() != 4)
            return ItemStack.EMPTY;

        Ingredient refIngredient = ingredients.get(0);
        ItemStack[] refMatchingStacks = refIngredient.getItems();
        if (refMatchingStacks.length == 0)
            return ItemStack.EMPTY;

        for (int i = 1, n = ingredients.size(); i < n; i++) {
            Ingredient ingredient = ingredients.get(i);
            ItemStack match = ItemStack.EMPTY;

            for (ItemStack ingItemMatch : refMatchingStacks) {
                if (ingredient.test(ingItemMatch)) {
                    match = ingItemMatch;
                    break;
                }
            }

            if (match.isEmpty())
                return ItemStack.EMPTY;
        }

        ItemStack match = findSimilar(stack, Arrays.asList(refMatchingStacks));
        if (match.isEmpty())
            match = refMatchingStacks[0];

        return match;
    }

    private CraftingContainer createContainerAndFill(int size, ItemStack stack){
        CraftingContainer inventoryCrafting = new TransientCraftingContainer(new AbstractContainerMenu(null, 0) {
            @Override
            public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player playerIn) {
                return false;
            }
        }, size, size);
        for (int i = 0; i < size * size; i++) {
            inventoryCrafting.setItem(i, stack.copy());
        }
        return inventoryCrafting;
    }

    public static class Result{

        private ItemStack result;
        private int needed;

        public Result(ItemStack result, int needed) {
            this.result = result;
            this.needed = needed;
        }

        public ItemStack getResult() {
            return result;
        }

        public void setResult(ItemStack result) {
            this.result = result;
        }

        public int getNeeded() {
            return needed;
        }

        public void setNeeded(int needed) {
            this.needed = needed;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "result=" + result +
                    ", needed=" + needed +
                    '}';
        }
    }

}
