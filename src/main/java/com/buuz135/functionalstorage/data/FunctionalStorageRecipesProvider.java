package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.recipe.CustomCompactingRecipe;
import com.buuz135.functionalstorage.recipe.TagWithoutComponentIngredient;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.block.BasicBlock;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapelessRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.buuz135.functionalstorage.FunctionalStorage.*;

public class FunctionalStorageRecipesProvider extends RecipeProvider {

    private final Lazy<List<Block>> blocksToProcess;

    public FunctionalStorageRecipesProvider(DataGenerator generator, Lazy<List<Block>> blocksToProcess, CompletableFuture<HolderLookup.Provider> prov) {
        super(generator.getPackOutput(), prov);
        this.blocksToProcess = blocksToProcess;
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        blocksToProcess.get().stream().map(block -> (BasicBlock) block).forEach(basicBlock -> basicBlock.registerRecipe(output));
        TitaniumShapedRecipeBuilder.shapedRecipe(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON).get())
                .pattern("III").pattern("IDI").pattern("III")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('D', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(VOID_UPGRADE.get())
                .pattern("III").pattern("IDI").pattern("III")
                .define('I', Tags.Items.OBSIDIANS)
                .define('D', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(CONFIGURATION_TOOL.get())
                .pattern("PPG").pattern("PDG").pattern("PEP")
                .define('P', Items.PAPER)
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('D', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .define('E', Items.EMERALD)
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(LINKING_TOOL.get())
                .pattern("PPG").pattern("PDG").pattern("PEP")
                .define('P', Items.PAPER)
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('D', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .define('E', Items.DIAMOND)
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.COPPER).get())
                .pattern("IBI").pattern("CDC").pattern("IBI")
                .define('I', Items.COPPER_INGOT)
                .define('B', Items.COPPER_BLOCK)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .define('D', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.GOLD).get())
                .pattern("IBI").pattern("CDC").pattern("BIB")
                .define('I', Tags.Items.INGOTS_GOLD)
                .define('B', Tags.Items.STORAGE_BLOCKS_GOLD)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .define('D', STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.COPPER).get())
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.DIAMOND).get())
                .pattern("IBI").pattern("CDC").pattern("IBI")
                .define('I', Tags.Items.GEMS_DIAMOND)
                .define('B', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .define('D', STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.GOLD).get())
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(REDSTONE_UPGRADE.get())
                .pattern("IBI").pattern("CDC").pattern("IBI")
                .define('I', Items.REDSTONE)
                .define('B', Items.REDSTONE_BLOCK)
                .define('C', Items.COMPARATOR)
                .define('D', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .save(output);
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.DIAMOND).get()), Ingredient.of(Items.NETHERITE_INGOT), RecipeCategory.MISC, STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.NETHERITE).get())
                .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                .save(output, BuiltInRegistries.ITEM.getKey(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.NETHERITE).get()));
        TitaniumShapedRecipeBuilder.shapedRecipe(ARMORY_CABINET.getBlock())
                .pattern("ICI").pattern("CDC").pattern("IBI")
                .define('I', Tags.Items.STONES)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .define('D', Items.COMPARATOR)
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(PULLING_UPGRADE.get())
                .pattern("ICI").pattern("IDI").pattern("IBI")
                .define('I', Tags.Items.STONES)
                .define('B', Tags.Items.DUSTS_REDSTONE)
                .define('C', Items.HOPPER)
                .define('D', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(PUSHING_UPGRADE.get())
                .pattern("IBI").pattern("IDI").pattern("IRI")
                .define('I', Tags.Items.STONES)
                .define('B', Tags.Items.DUSTS_REDSTONE)
                .define('R', Items.HOPPER)
                .define('D', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(DRIPPING_UPGRADE.get())
                .pattern("IBI").pattern("IDI").pattern("IRI")
                .define('I', Tags.Items.STONES)
                .define('B', Items.POINTED_DRIPSTONE)
                .define('R', Items.LAVA_BUCKET)
                .define('D', Items.CAULDRON)
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(WATER_GENERATOR_UPGRADE.get())
                .pattern("IBI").pattern("IDI").pattern("IBI")
                .define('I', Tags.Items.STONES)
                .define('B', Items.WATER_BUCKET)
                .define('D', Items.BUCKET)
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(COLLECTOR_UPGRADE.get())
                .pattern("IBI").pattern("RDR").pattern("IBI")
                .define('I', Tags.Items.STONES)
                .define('B', Items.HOPPER)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('D', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .save(output);
        TitaniumShapedRecipeBuilder.shapedRecipe(ENDER_DRAWER.getBlock())
                .pattern("PPP").pattern("LCL").pattern("PPP")
                .define('P', ItemTags.PLANKS)
                .define('C', Tags.Items.CHESTS_ENDER)
                .define('L', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .save(output);
        TitaniumShapelessRecipeBuilder.shapelessRecipe(OBSIDIAN_UPGRADE.get())
                .requires(DRIPPING_UPGRADE.get(), 4)
                .requires(WATER_GENERATOR_UPGRADE.get())
                .save(output);
        new CustomCompactingRecipe(new ItemStack(Items.GLOWSTONE_DUST, 4), new ItemStack(Items.GLOWSTONE)).save(output, com.buuz135.functionalstorage.util.Utils.resourceLocation("functionalstorage:compacting/glowstone"));
        new CustomCompactingRecipe(new ItemStack(Items.MELON_SLICE, 9), new ItemStack(Items.MELON)).save(output, com.buuz135.functionalstorage.util.Utils.resourceLocation("functionalstorage:compacting/melon"));
        new CustomCompactingRecipe(new ItemStack(Items.QUARTZ, 4), new ItemStack(Items.QUARTZ_BLOCK)).save(output, com.buuz135.functionalstorage.util.Utils.resourceLocation("functionalstorage:compacting/quartz"));
        new CustomCompactingRecipe(new ItemStack(Items.ICE, 9), new ItemStack(Items.PACKED_ICE)).save(output, com.buuz135.functionalstorage.util.Utils.resourceLocation("functionalstorage:compacting/ice"));
        new CustomCompactingRecipe(new ItemStack(Items.PACKED_ICE, 9), new ItemStack(Items.BLUE_ICE)).save(output, com.buuz135.functionalstorage.util.Utils.resourceLocation("functionalstorage:compacting/packed_ice"));
        new CustomCompactingRecipe(new ItemStack(Items.AMETHYST_SHARD, 4), new ItemStack(Items.AMETHYST_BLOCK)).save(output, com.buuz135.functionalstorage.util.Utils.resourceLocation("functionalstorage:compacting/amethyst"));
    }
}
