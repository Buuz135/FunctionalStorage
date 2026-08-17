package com.buuz135.functionalstorage.recipe;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;

public class CustomCompactingRecipe implements Recipe<CraftingInput> {
    public static final MapCodec<CustomCompactingRecipe> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            ItemStackTemplate.CODEC.fieldOf("lower_input").forGetter(CustomCompactingRecipe::lowerTemplate),
            ItemStackTemplate.CODEC.fieldOf("higher_input").forGetter(CustomCompactingRecipe::higherTemplate)
    ).apply(in, CustomCompactingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CustomCompactingRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemStackTemplate.STREAM_CODEC, CustomCompactingRecipe::lowerTemplate,
            ItemStackTemplate.STREAM_CODEC, CustomCompactingRecipe::higherTemplate,
            CustomCompactingRecipe::new);

    private final ItemStackTemplate lowerInput;
    private final ItemStackTemplate higherInput;

    public CustomCompactingRecipe(ItemStackTemplate lowerInput, ItemStackTemplate higherInput) {
        this.lowerInput = lowerInput;
        this.higherInput = higherInput;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<CustomCompactingRecipe> getSerializer() {
        return (RecipeSerializer<CustomCompactingRecipe>) (RecipeSerializer<?>) FunctionalStorage.CUSTOM_COMPACTING_RECIPE_SERIALIZER.value();
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeType<CustomCompactingRecipe> getType() {
        return (RecipeType<CustomCompactingRecipe>) (RecipeType<?>) FunctionalStorage.CUSTOM_COMPACTING_RECIPE_TYPE.value();
    }

    public void save(RecipeOutput output, Identifier id) {
        output.accept(ResourceKey.create(Registries.RECIPE, id), this, null);
    }

    public void save(RecipeOutput output) {
        save(output, BuiltInRegistries.ITEM.getKey(higherInput.item().value()));
    }

    public ItemStackTemplate lowerTemplate() {
        return lowerInput;
    }

    public ItemStackTemplate higherTemplate() {
        return higherInput;
    }

    public ItemStack lowerStack() {
        return lowerInput.create();
    }

    public ItemStack higherStack() {
        return higherInput.create();
    }
}
