package com.buuz135.functionalstorage.util;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.recipe.CustomCompactingRecipe;
import com.hrznstudio.titanium.util.RecipeUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;


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
        this.recipes = (List<CustomCompactingRecipe>) RecipeUtil.getRecipes(level, FunctionalStorage.CUSTOM_COMPACTING_RECIPE_TYPE.value());
    }

    public void setup(ItemStack stack){
        setup(stack, -1);
    }

    public void setup(ItemStack stack, int slot){

        if (slot < 0) {
            autoSetup(stack);
        } else {
            manualSetup(stack, slot);
        }

        results.stream().filter(result1 -> result1.getResult().getCount() > 0).forEach(result1 -> result1.setNeeded(result1.getNeeded() / result1.getResult().getCount()));
    }

    private void autoSetup(ItemStack stack){
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
                results.addFirst(result);
            } else {
                canFind = false;
            }
        }
        while (results.size() < resultAmount) {
            results.addFirst(new Result(ItemStack.EMPTY, 1));
        }
    }

    private void manualSetup(ItemStack stack, int slot){
        results.clear();
        results.add(new Result(stack, 1));

        Result current = stack.isEmpty() ? new Result(ItemStack.EMPTY, 0) : findLowerTier(stack);
        for (int i = 0; i < slot && !current.getResult().isEmpty(); i++) {
            results.addFirst(current);
            current = findLowerTier(current.getResult());
        }

        current = stack.isEmpty() ? new Result(ItemStack.EMPTY, 0) : findUpperTier(stack);
        for (int i = slot + 1; i < resultAmount && !current.getResult().isEmpty(); i++) {
            if (results.size() > 1) {
                current.setNeeded(current.getNeeded() * results.getLast().getNeeded());
            }
            results.add(current);
            current = findUpperTier(current.getResult());
        }

        for (int i = slot - 1; i >= 0; i--) {
            Result lower = results.get(i);
            int multiplier = lower.getNeeded();

            for (int j = i + 1; j < results.size(); j++) {
                results.get(j).setNeeded(results.get(j).getNeeded() * multiplier);
            }

            lower.setNeeded(1);
        }

        while (results.size() < resultAmount) {
            results.add(Math.max(results.size(), slot), new Result(ItemStack.EMPTY, 1));
        }
    }


    public List<Result> rearrangeResults(ItemStack clickedItemStack, int clickedSlot) {

        List<Result> rearranged = new ArrayList<>(resultAmount);
        for (int i = 0; i < resultAmount; i++) {
            rearranged.add(new Result(ItemStack.EMPTY, 1));
        }

        int clickedIndex = -1;
        for (int i = 0; i < results.size(); i++) {
            if (ItemStack.isSameItem(results.get(i).getResult(), clickedItemStack)) {
                clickedIndex = i;
                break;
            }
        }
        if (clickedIndex < 0) {
            return results;
        }

        List<Result> copy = new ArrayList<>(results);
        Result clickedResult = copy.remove(clickedIndex);

        List<Result> lower  = copy.subList(0, Math.min(clickedIndex, copy.size()));
        List<Result> higher = copy.subList(Math.min(clickedIndex, copy.size()), copy.size());

        int slotsAbove = resultAmount - clickedSlot - 1;

        rearranged.set(clickedSlot, clickedResult);

        for (int i = 0; i < Math.min(clickedSlot, lower.size()); i++) {
            rearranged.set(clickedSlot - 1 - i, lower.get(lower.size() - 1 - i));
        }

        for (int i = 0; i < Math.min(slotsAbove, higher.size()); i++) {
            rearranged.set(clickedSlot + 1 + i, higher.get(i));
        }

        return rearranged;
    }

    public List<Result> getResults() {
        return results;
    }

    private Result findUpperTier(ItemStack stack){
        for (CustomCompactingRecipe recipe : this.recipes) {
            if (ItemStack.isSameItem(recipe.lower_input, stack)) {
                return new Result(recipe.higher_input.copyWithCount(1), recipe.lower_input.getCount());
            }
        }
        //Checking 3x3
        int sizeCheck = 9;
        var container = createContainerAndFill(3, stack);
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
                return new Result(recipe.lower_input.copyWithCount(1), recipe.lower_input.getCount());
            }
        }
        List<ItemStack> candidates = new ArrayList<>();
        Map<ItemStack, Integer> candidatesRate = new HashMap<>();
        for (var rcp : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            var craftingRecipe = rcp.value();
            ItemStack output = craftingRecipe.getResultItem(this.level.registryAccess());
            if (!ItemStack.isSameItem(stack, output)) continue;
            ItemStack match = tryMatch(stack, craftingRecipe.getIngredients());
            if (!match.isEmpty()){
                int recipeSize = craftingRecipe.getIngredients().size();
                if (stack.is(StorageTags.IGNORE_CRAFTING_CHECK)){
                    candidates.add(match);
                    candidatesRate.put(match, recipeSize);
                }
                var container = createContainerAndFill(1, output);
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

    private List<ItemStack> findAllMatchingRecipes(CraftingInput crafting) {
        List<ItemStack> candidates = new ArrayList<>();
        for (var rcp : level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, crafting, level)) {
            var recipe = rcp.value();
            if (recipe.matches(crafting, level)) {
                ItemStack result = recipe.assemble(crafting, this.level.registryAccess());
                if (!result.isEmpty())
                    candidates.add(result);
            }
        }
        return candidates;
    }

    private ItemStack findSimilar(ItemStack reference, List<ItemStack> candidates) {
        ResourceLocation referenceName = BuiltInRegistries.ITEM.getKey(reference.getItem());
        for (ItemStack candidate : candidates) {
            ResourceLocation matchName = BuiltInRegistries.ITEM.getKey(candidate.getItem());
            if (referenceName.getNamespace().equals(matchName.getNamespace()))
                return candidate;
        }
        return !candidates.isEmpty() ? candidates.get(0) : ItemStack.EMPTY;
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

    private CraftingInput createContainerAndFill(int size, ItemStack stack){
        return CraftingInput.of(size, size, IntStream.range(0, size * size)
                .mapToObj(i -> stack.copy()).toList());
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