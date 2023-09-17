package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.block.BasicBlock;
import com.hrznstudio.titanium.recipe.generator.TitaniumRecipeProvider;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Consumer;

import static com.buuz135.functionalstorage.FunctionalStorage.*;

public class FunctionalStorageRecipesProvider extends TitaniumRecipeProvider {

    private NonNullLazy<List<Block>> blocksToProcess;

    public FunctionalStorageRecipesProvider(DataGenerator generator, NonNullLazy<List<Block>> blocksToProcess) {
        super(generator);
        this.blocksToProcess = blocksToProcess;
    }

    @Override
    public void register(Consumer<FinishedRecipe> consumer) {
        blocksToProcess.get().stream().map(block -> (BasicBlock) block).forEach(basicBlock -> basicBlock.registerRecipe(consumer));
        TitaniumShapedRecipeBuilder.shapedRecipe(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON).get())
                .pattern("III").pattern("IDI").pattern("III")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('D', StorageTags.DRAWER)
                .save(consumer);
        TitaniumShapedRecipeBuilder.shapedRecipe(VOID_UPGRADE.get())
                .pattern("III").pattern("IDI").pattern("III")
                .define('I', Tags.Items.OBSIDIAN)
                .define('D', StorageTags.DRAWER)
                .save(consumer);
        TitaniumShapedRecipeBuilder.shapedRecipe(CONFIGURATION_TOOL.get())
                .pattern("PPG").pattern("PDG").pattern("PEP")
                .define('P', Items.PAPER)
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('D', StorageTags.DRAWER)
                .define('E', Items.EMERALD)
                .save(consumer);
        TitaniumShapedRecipeBuilder.shapedRecipe(LINKING_TOOL.get())
                .pattern("PPG").pattern("PDG").pattern("PEP")
                .define('P', Items.PAPER)
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('D', StorageTags.DRAWER)
                .define('E', Items.DIAMOND)
                .save(consumer);
        TitaniumShapedRecipeBuilder.shapedRecipe(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.COPPER).get())
                .pattern("IBI").pattern("CDC").pattern("IBI")
                .define('I', Items.COPPER_INGOT)
                .define('B', Items.COPPER_BLOCK)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .define('D', StorageTags.DRAWER)
                .save(consumer);
        TitaniumShapedRecipeBuilder.shapedRecipe(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.GOLD).get())
                .pattern("IBI").pattern("CDC").pattern("BIB")
                .define('I', Tags.Items.INGOTS_GOLD)
                .define('B', Tags.Items.STORAGE_BLOCKS_GOLD)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .define('D', STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.COPPER).get())
                .save(consumer);
        TitaniumShapedRecipeBuilder.shapedRecipe(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.DIAMOND).get())
                .pattern("IBI").pattern("CDC").pattern("IBI")
                .define('I', Tags.Items.GEMS_DIAMOND)
                .define('B', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .define('D', STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.GOLD).get())
                .save(consumer);
        TitaniumShapedRecipeBuilder.shapedRecipe(REDSTONE_UPGRADE.get())
                .pattern("IBI").pattern("CDC").pattern("IBI")
                .define('I', Items.REDSTONE)
                .define('B', Items.REDSTONE_BLOCK)
                .define('C', Items.COMPARATOR)
                .define('D', StorageTags.DRAWER)
                .save(consumer);
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.DIAMOND).get()), Ingredient.of(Items.NETHERITE_INGOT), RecipeCategory.MISC, STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.NETHERITE).get())
                .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                .save(consumer, ForgeRegistries.ITEMS.getKey(STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.NETHERITE).get()));
        TitaniumShapedRecipeBuilder.shapedRecipe(ARMORY_CABINET.getLeft().get())
                .pattern("ICI").pattern("CDC").pattern("IBI")
                .define('I', Tags.Items.STONE)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', StorageTags.DRAWER)
                .define('D', Items.COMPARATOR)
                .save(consumer);
        TitaniumShapedRecipeBuilder.shapedRecipe(PULLING_UPGRADE.get())
                .pattern("ICI").pattern("IDI").pattern("IBI")
                .define('I', Tags.Items.STONE)
                .define('B', Tags.Items.DUSTS_REDSTONE)
                .define('C', Items.HOPPER)
                .define('D', StorageTags.DRAWER)
                .save(consumer);
        TitaniumShapedRecipeBuilder.shapedRecipe(PUSHING_UPGRADE.get())
                .pattern("IBI").pattern("IDI").pattern("IRI")
                .define('I', Tags.Items.STONE)
                .define('B', Tags.Items.DUSTS_REDSTONE)
                .define('R', Items.HOPPER)
                .define('D', StorageTags.DRAWER)
                .save(consumer);
        TitaniumShapedRecipeBuilder.shapedRecipe(COLLECTOR_UPGRADE.get())
                .pattern("IBI").pattern("RDR").pattern("IBI")
                .define('I', Tags.Items.STONE)
                .define('B', Items.HOPPER)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('D', StorageTags.DRAWER)
                .save(consumer);
        TitaniumShapedRecipeBuilder.shapedRecipe(ENDER_DRAWER.getLeft().get())
                .pattern("PPP").pattern("LCL").pattern("PPP")
                .define('P', ItemTags.PLANKS)
                .define('C', Tags.Items.CHESTS_ENDER)
                .define('L', StorageTags.DRAWER)
                .save(consumer);
    }
}
