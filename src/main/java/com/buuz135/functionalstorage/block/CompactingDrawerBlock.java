package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.CompactingDrawerTile;
import com.buuz135.functionalstorage.inventory.item.CompactingStackItemHandler;
import com.buuz135.functionalstorage.util.StorageTags;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CompactingDrawerBlock extends Drawer<CompactingDrawerTile> {

    public static Multimap<Direction, VoxelShape> CACHED_SHAPES = MultimapBuilder.hashKeys().arrayListValues().build();

    static {
        CACHED_SHAPES.put(Direction.NORTH, Shapes.box(1/16D, 1/16D, 0, 7/16D, 7/16D, 1/16D));
        CACHED_SHAPES.put(Direction.NORTH, Shapes.box(9/16D, 1/16D, 0, 15/16D, 7/16D, 1/16D));
        CACHED_SHAPES.put(Direction.NORTH, Shapes.box(1/16D, 9/16D, 0, 15/16D, 15/16D, 1/16D));
        CACHED_SHAPES.put(Direction.SOUTH, Shapes.box(9/16D, 1/16D, 15/16D, 15/16D, 7/16D, 1));
        CACHED_SHAPES.put(Direction.SOUTH, Shapes.box(1/16D, 1/16D, 15/16D, 7/16D, 7/16D, 1));
        CACHED_SHAPES.put(Direction.SOUTH, Shapes.box(1/16D, 9/16D, 15/16D, 15/16D, 15/16D, 1));
        CACHED_SHAPES.put(Direction.EAST, Shapes.box(15/16D, 1/16D, 1/16D, 1, 7/16D, 7/16D));
        CACHED_SHAPES.put(Direction.EAST, Shapes.box(15/16D, 1/16D, 9/16D, 1, 7/16D, 15/16D));
        CACHED_SHAPES.put(Direction.EAST, Shapes.box(15/16D, 9/16D, 1/16D, 1, 15/16D, 15/16D));
        CACHED_SHAPES.put(Direction.WEST, Shapes.box(0, 1/16D, 9/16D, 1/16D, 7/16D, 15/16D));
        CACHED_SHAPES.put(Direction.WEST, Shapes.box(0, 1/16D, 1/16D, 1/16D, 7/16D, 7/16D));
        CACHED_SHAPES.put(Direction.WEST, Shapes.box(0, 9/16D, 1/16D, 1/16D, 15/16D, 15/16D));
    }


    public CompactingDrawerBlock(String name, Properties properties) {
        super(name, properties, CompactingDrawerTile.class);
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(RotatableBlock.FACING_HORIZONTAL, Direction.NORTH).setValue(DrawerBlock.LOCKED, false));
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<CompactingDrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new CompactingDrawerTile(this, (BlockEntityType<CompactingDrawerTile>) FunctionalStorage.COMPACTING_DRAWER.type().get(), blockPos, state);
    }

    @Override
    public List<VoxelShape> getBoundingBoxes(BlockState state, BlockGetter source, BlockPos pos) {
        return getShapes(state, source, pos);
    }

    private static List<VoxelShape> getShapes(BlockState state, BlockGetter source, BlockPos pos){
        List<VoxelShape> boxes = new ArrayList<>();
        CACHED_SHAPES.get(state.getValue(RotatableBlock.FACING_HORIZONTAL)).forEach(boxes::add);
        VoxelShape total = Shapes.block();
        boxes.add(total);
        return boxes;
    }

    @Override
    public Collection<VoxelShape> getHitShapes(BlockState state) {
        return CACHED_SHAPES.get(state.getValue(RotatableBlock.FACING_HORIZONTAL));
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .pattern("SSS").pattern("PDP").pattern("SIS")
                .define('S', Blocks.STONE)
                .define('P', Blocks.PISTON)
                .define('D', StorageTags.DRAWER)
                .define('I', Tags.Items.INGOTS_IRON)
                .save(consumer);
    }

    public static class CompactingDrawerItem extends BlockItem {

        private final int slots;

        public CompactingDrawerItem(Block p_40565_, net.minecraft.world.item.Item.Properties p_40566_, int slots) {
            super(p_40565_, p_40566_);
            this.slots = slots;
        }

        public IItemHandler initCapabilities(ItemStack stack) {
            return new CompactingStackItemHandler(stack, slots);
        }
    }
}
