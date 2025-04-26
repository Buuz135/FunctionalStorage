package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.FramedSimpleCompactingDrawerTile;
import com.buuz135.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.recipe.CopyComponentsRecipe;
import com.buuz135.functionalstorage.recipe.TagWithoutComponentIngredient;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.Tags;

public class FramedSimpleCompactingDrawerBlock extends SimpleCompactingDrawerBlock implements FramedBlock {
    public FramedSimpleCompactingDrawerBlock(String name) {
        super(name, Properties.ofFullCopy(Blocks.STONE).noOcclusion().isViewBlocking((p_61036_, p_61037_, p_61038_) -> false));
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<SimpleCompactingDrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new FramedSimpleCompactingDrawerTile(this, (BlockEntityType<SimpleCompactingDrawerTile>) FunctionalStorage.FRAMED_SIMPLE_COMPACTING_DRAWER.type().get(), blockPos, state);
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .pattern("SSS").pattern("SDP").pattern("SIS")
                .define('S', Tags.Items.NUGGETS_IRON)
                .define('P', Blocks.PISTON)
                .define('D', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .define('I', Tags.Items.INGOTS_IRON)
                .save(consumer);

        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .pattern("SSS").pattern("SDS").pattern(" S ")
                .define('S', Tags.Items.NUGGETS_IRON)
                .define('D', FunctionalStorage.SIMPLE_COMPACTING_DRAWER)
                .save(CopyComponentsRecipe.output(
                        consumer, 4, FSAttachments.TILE.get()
                ), builtInRegistryHolder().unwrapKey().get().location().withSuffix("_from_simple"));
    }

    
}
