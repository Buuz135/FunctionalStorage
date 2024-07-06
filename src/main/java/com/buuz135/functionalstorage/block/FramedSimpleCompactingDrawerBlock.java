package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.FramedSimpleCompactingDrawerTile;
import com.buuz135.functionalstorage.block.tile.SimpleCompactingDrawerTile;
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
                .define('S', Items.IRON_NUGGET)
                .define('P', Blocks.PISTON)
                .define('D', StorageTags.DRAWER)
                .define('I', Tags.Items.INGOTS_IRON)
                .save(consumer);
    }

    
}
