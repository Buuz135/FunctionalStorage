package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class DrawerControllerBlock extends StorageControllerBlock<DrawerControllerTile> {

    public DrawerControllerBlock() {
        super("storage_controller", Properties.copy(Blocks.IRON_BLOCK), DrawerControllerTile.class);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (p_155268_, p_155269_) -> new DrawerControllerTile(this, (BlockEntityType<DrawerControllerTile>) FunctionalStorage.DRAWER_CONTROLLER.getRight().get(), p_155268_, p_155269_);
    }

    @Override
    public void registerRecipe(Consumer<FinishedRecipe> consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalStorage.DRAWER_CONTROLLER.getLeft().get())
                .pattern("IBI").pattern("CDC").pattern("IBI")
                .define('I', Tags.Items.STONE)
                .define('B', Tags.Items.STORAGE_BLOCKS_QUARTZ)
                .define('C', StorageTags.DRAWER)
                .define('D', Items.COMPARATOR)
                .save(consumer);
    }
}
