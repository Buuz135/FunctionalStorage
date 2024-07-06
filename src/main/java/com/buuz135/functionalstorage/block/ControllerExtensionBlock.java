package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllerExtensionTile;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.datagenerator.loot.block.BasicBlockLootTables;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.BlockTagIngredient;
import org.jetbrains.annotations.NotNull;

public class ControllerExtensionBlock extends StorageControllerExtensionBlock<ControllerExtensionTile> {

    public ControllerExtensionBlock() {
        super("controller_extension", Properties.ofFullCopy(Blocks.IRON_BLOCK), ControllerExtensionTile.class);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (p_155268_, p_155269_) -> new ControllerExtensionTile(this, (BlockEntityType<ControllerExtensionTile>) FunctionalStorage.CONTROLLER_EXTENSION.type().get(), p_155268_, p_155269_);
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalStorage.CONTROLLER_EXTENSION.block().get())
                .pattern("IBI").pattern("CDC").pattern("IBI")
                .define('I', Tags.Items.STONES)
                .define('B', Items.QUARTZ_BLOCK)
                .define('C', StorageTags.DRAWER)
                .define('D', Items.REPEATER)
                .save(consumer);
    }

    @Override
    public LootTable.Builder getLootTable(@NotNull BasicBlockLootTables blockLootTables) {
        return blockLootTables.droppingSelf(this);
    }

}
