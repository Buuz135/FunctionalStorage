package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import com.buuz135.functionalstorage.recipe.TagWithoutComponentIngredient;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.datagenerator.loot.block.BasicBlockLootTables;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

public class DrawerControllerBlock extends StorageControllerBlock<DrawerControllerTile> {

    public DrawerControllerBlock() {
        super("storage_controller", Properties.ofFullCopy(Blocks.IRON_BLOCK), DrawerControllerTile.class);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (p_155268_, p_155269_) -> new DrawerControllerTile(this, (BlockEntityType<DrawerControllerTile>) FunctionalStorage.DRAWER_CONTROLLER.type().get(), p_155268_, p_155269_);
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalStorage.DRAWER_CONTROLLER.block().get())
                .pattern("IBI").pattern("CDC").pattern("IBI")
                .define('I', Tags.Items.STONES)
                .define('B', Items.QUARTZ_BLOCK)
                .define('C', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .define('D', Items.COMPARATOR)
                .save(consumer);
    }

    @Override
    public LootTable.Builder getLootTable(@NotNull BasicBlockLootTables blockLootTables) {
        return blockLootTables.droppingSelf(this);
    }
}
