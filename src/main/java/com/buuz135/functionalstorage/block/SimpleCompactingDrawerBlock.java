package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.buuz135.functionalstorage.recipe.TagWithoutComponentIngredient;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimpleCompactingDrawerBlock extends Drawer<SimpleCompactingDrawerTile> {

    public SimpleCompactingDrawerBlock(String name, Properties properties) {
        super(name, properties, SimpleCompactingDrawerTile.class);
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(Drawer.FACING_HORIZONTAL_CUSTOM, Direction.NORTH).setValue(DrawerBlock.LOCKED, false));
    }

    private static List<VoxelShape> getShapes(BlockState state, BlockGetter source, BlockPos pos) {
        List<VoxelShape> boxes = new ArrayList<>();
        Direction facing = state.getValue(Drawer.FACING_HORIZONTAL_CUSTOM);

        // For X_2 type, if facing is UP or DOWN, use FACING_ALL to determine rotation
        if ((facing == Direction.UP || facing == Direction.DOWN)) {
            Direction subfacing = state.getValue(RotatableBlock.FACING_ALL);

            // Use cached rotated shapes
            boxes.addAll(DrawerBlock.CACHED_ROTATED_SHAPES.get(facing).get(subfacing));
        } else {
            // For other types or directions, use the original shapes
            DrawerBlock.CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_2).get(facing).forEach(boxes::add);
        }

        VoxelShape total = Shapes.block();
        boxes.add(total);
        return boxes;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<SimpleCompactingDrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new SimpleCompactingDrawerTile(this, (BlockEntityType<SimpleCompactingDrawerTile>) FunctionalStorage.SIMPLE_COMPACTING_DRAWER.type().get(), blockPos, state);
    }

    @Override
    public List<VoxelShape> getBoundingBoxes(BlockState state, BlockGetter source, BlockPos pos) {
        return getShapes(state, source, pos);
    }

    @Override
    public Collection<VoxelShape> getHitShapes(BlockState state) {
        return DrawerBlock.getDefaultHitShapes(FunctionalStorage.DrawerType.X_2, state);
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .pattern("SSS").pattern("SDP").pattern("SIS")
                .define('S', Tags.Items.STONES)
                .define('P', Blocks.PISTON)
                .define('D', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .define('I', Tags.Items.INGOTS_IRON)
                .save(consumer);
    }
}
