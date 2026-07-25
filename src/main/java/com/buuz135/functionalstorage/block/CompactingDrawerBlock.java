package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.CompactingDrawerTile;
import com.buuz135.functionalstorage.client.item.CompactingDrawerISTER;
import com.buuz135.functionalstorage.inventory.item.CompactingStackItemHandler;
import com.buuz135.functionalstorage.recipe.TagWithoutComponentIngredient;
import com.buuz135.functionalstorage.util.StorageTags;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.*;

import java.util.function.Consumer;

public class CompactingDrawerBlock extends Drawer<CompactingDrawerTile> {

    public static Multimap<Direction, VoxelShape> CACHED_SHAPES = MultimapBuilder.hashKeys().arrayListValues().build();
    // Cache for rotated shapes - stores shapes for UP/DOWN facing and different subfacings
    public static final HashMap<Direction, HashMap<Direction, List<VoxelShape>>> CACHED_ROTATED_SHAPES = new HashMap<>();

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
        CACHED_SHAPES.put(Direction.UP, Shapes.box(1/16D, 15/16D, 1/16D, 7/16D, 1, 7/16D));
        CACHED_SHAPES.put(Direction.UP, Shapes.box(9/16D, 15/16D, 1/16D, 15/16D, 1, 7/16D));
        CACHED_SHAPES.put(Direction.UP, Shapes.box(1/16D, 15/16D, 9/16D, 15/16D, 1, 15/16D));
        CACHED_SHAPES.put(Direction.DOWN, Shapes.box(1/16D, 0, 1/16D, 7/16D, 1/16D, 7/16D));
        CACHED_SHAPES.put(Direction.DOWN, Shapes.box(9/16D, 0, 1/16D, 15/16D, 1/16D, 7/16D));
        CACHED_SHAPES.put(Direction.DOWN, Shapes.box(1/16D, 0, 9/16D, 15/16D, 1/16D, 15/16D));

        // Initialize the rotated shapes cache
        CACHED_ROTATED_SHAPES.put(Direction.UP, new HashMap<>());
        CACHED_ROTATED_SHAPES.put(Direction.DOWN, new HashMap<>());

        for (Direction facing : new Direction[]{Direction.UP, Direction.DOWN}) {
            // Initialize subfacing maps
            for (Direction subfacing : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                CACHED_ROTATED_SHAPES.get(facing).put(subfacing, new ArrayList<>());
            }

            // Get the original shapes for this facing
            Collection<VoxelShape> originalShapes = CACHED_SHAPES.get(facing);
            for (VoxelShape shape : originalShapes) {
                AABB bounds = shape.toAabbs().get(0);

                // For SOUTH subfacing - use original shape
                CACHED_ROTATED_SHAPES.get(facing).get(Direction.SOUTH).add(shape);

                // For EAST subfacing - rotate once from original
                VoxelShape eastShape = Shapes.box(bounds.minZ, bounds.minY, bounds.minX, bounds.maxZ, bounds.maxY, bounds.maxX);
                CACHED_ROTATED_SHAPES.get(facing).get(Direction.EAST).add(eastShape);

                // For NORTH subfacing - mirror of SOUTH (flip X coordinates)
                AABB southBounds = shape.toAabbs().get(0);
                VoxelShape northShape = Shapes.box(southBounds.minX, southBounds.minY, 1 - southBounds.maxZ,
                        southBounds.maxX, southBounds.maxY, 1 - southBounds.minZ);
                CACHED_ROTATED_SHAPES.get(facing).get(Direction.NORTH).add(northShape);

                // For WEST subfacing - mirror of EAST (flip Z coordinates)
                AABB eastBounds = eastShape.toAabbs().get(0);
                VoxelShape westShape = Shapes.box(1 - eastBounds.maxX, eastBounds.minY, eastBounds.minZ,
                        1 - eastBounds.minX, eastBounds.maxY, eastBounds.maxZ);
                CACHED_ROTATED_SHAPES.get(facing).get(Direction.WEST).add(westShape);
            }
        }
    }


    public CompactingDrawerBlock(String name, Properties properties) {
        super(name, properties, CompactingDrawerTile.class);
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(Drawer.FACING_HORIZONTAL_CUSTOM, Direction.NORTH).setValue(DrawerBlock.LOCKED, false));
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
        Direction facing = state.getValue(Drawer.FACING_HORIZONTAL_CUSTOM);

        // For UP or DOWN facing, use FACING_ALL to determine rotation
        if (facing == Direction.UP || facing == Direction.DOWN) {
            Direction subfacing = state.getValue(RotatableBlock.FACING_ALL);

            // Use cached rotated shapes
            boxes.addAll(CACHED_ROTATED_SHAPES.get(facing).get(subfacing));
        } else {
            // For horizontal directions, use the original shapes
            CACHED_SHAPES.get(facing).forEach(boxes::add);
        }

        VoxelShape total = Shapes.block();
        boxes.add(total);
        return boxes;
    }

    @Override
    public Collection<VoxelShape> getHitShapes(BlockState state) {
        Direction facing = state.getValue(Drawer.FACING_HORIZONTAL_CUSTOM);

        // For UP or DOWN facing, use FACING_ALL to determine rotation
        if (facing == Direction.UP || facing == Direction.DOWN) {
            Direction subfacing = state.getValue(RotatableBlock.FACING_ALL);
            if (facing == Direction.UP) {
                var shapes = CACHED_ROTATED_SHAPES.get(facing).get(subfacing).stream().toList();
                if (subfacing == Direction.EAST || subfacing == Direction.NORTH) {
                    return Arrays.asList(shapes.get(1), shapes.get(0), shapes.get(2));
                }
            }
            if (facing == Direction.DOWN) {
                var shapes = CACHED_ROTATED_SHAPES.get(facing).get(subfacing).stream().toList();
                if (subfacing == Direction.WEST || subfacing == Direction.SOUTH) {
                    return Arrays.asList(shapes.get(1), shapes.get(0), shapes.get(2));
                }
            }

            // Use cached rotated shapes
            return CACHED_ROTATED_SHAPES.get(facing).get(subfacing);
        } else {
            // For horizontal directions, use the original shapes
            return CACHED_SHAPES.get(facing);
        }
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .pattern("SSS").pattern("PDP").pattern("SIS")
                .define('S', Tags.Items.STONES)
                .define('P', Blocks.PISTON)
                .define('D', new TagWithoutComponentIngredient(StorageTags.DRAWER).toVanilla())
                .define('I', Tags.Items.INGOTS_IRON)
                .save(consumer);
    }

    public static class CompactingDrawerItem extends BlockItem {
        private final int slots;

        public CompactingDrawerItem(Block block, net.minecraft.world.item.Item.Properties properties, int slots) {
            super(block, properties);
            this.slots = slots;
        }

        public IItemHandler initCapabilities(ItemStack stack) {
            return new CompactingStackItemHandler(stack, slots);
        }

        @Override
        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            consumer.accept(new IClientItemExtensions() {
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return getBlock() instanceof SimpleCompactingDrawerBlock ? CompactingDrawerISTER.SIMPLE : CompactingDrawerISTER.NORMAL;
                }
            });
        }
    }
}
