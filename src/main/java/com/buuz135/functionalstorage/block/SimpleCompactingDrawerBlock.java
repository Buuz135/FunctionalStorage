package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.SimpleCompactingDrawerTile;
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
import net.minecraft.world.phys.shapes.CollisionContext;
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
        registerDefaultState(defaultBlockState().setValue(RotatableBlock.FACING_HORIZONTAL, Direction.NORTH).setValue(DrawerBlock.LOCKED, false));
    }

    private static List<VoxelShape> getShapes(BlockState state, BlockGetter source, BlockPos pos) {
        List<VoxelShape> boxes = new ArrayList<>();
        DrawerBlock.CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_2).get(state.getValue(RotatableBlock.FACING_HORIZONTAL)).forEach(boxes::add);
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
        return DrawerBlock.CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_2).get(state.getValue(RotatableBlock.FACING_HORIZONTAL));
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .pattern("SSS").pattern("SDP").pattern("SIS")
                .define('S', Blocks.STONE)
                .define('P', Blocks.PISTON)
                .define('D', StorageTags.DRAWER)
                .define('I', Tags.Items.INGOTS_IRON)
                .save(consumer);
    }
}
