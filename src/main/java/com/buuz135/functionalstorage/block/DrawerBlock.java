package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.client.item.DrawerISTER;
import com.buuz135.functionalstorage.inventory.item.DrawerStackItemHandler;
import com.buuz135.functionalstorage.recipe.DrawerlessWoodIngredient;
import com.buuz135.functionalstorage.util.IWoodType;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.module.BlockWithTile;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.tab.TitaniumTab;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class DrawerBlock extends Drawer<DrawerTile> {

    public static final HashMap<FunctionalStorage.DrawerType, Multimap<Direction, VoxelShape>> CACHED_SHAPES = new HashMap<>();
    // Cache for rotated shapes - stores shapes for X_2 type with UP/DOWN facing and different subfacings
    public static final HashMap<Direction, HashMap<Direction, List<VoxelShape>>> CACHED_ROTATED_SHAPES = new HashMap<>();

    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");

    static {
        CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_1, type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.NORTH, Shapes.box(1/16D, 1/16D, 0, 15/16D, 15/16D, 1/16D));
        CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_1, type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.SOUTH, Shapes.box(1/16D, 1/16D, 15/16D, 15/16D, 15/16D, 1));
        CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_1, type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.WEST, Shapes.box(0, 1/16D, 1/16D, 1/16D, 15/16D, 15/16D));
        CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_1, type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.EAST, Shapes.box(15/16D, 1/16D, 1/16D, 1, 15/16D, 15/16D));
        CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_1, type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.UP, Shapes.box(1/16D, 15/16D, 1/16D, 15/16D, 1, 15/16D));
        CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_1, type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.DOWN, Shapes.box(1/16D, 0, 1/16D, 15/16D, 1/16D, 15/16D));

        // Initialize the rotated shapes cache
        CACHED_ROTATED_SHAPES.put(Direction.UP, new HashMap<>());
        CACHED_ROTATED_SHAPES.put(Direction.DOWN, new HashMap<>());
        for (Direction direction : CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_1).keySet()) {
            for (VoxelShape voxelShape : CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_1).get(direction)) {
                AABB bounding = voxelShape.toAabbs().get(0);
                if (direction == Direction.UP) {
                    // For vertical directions, split horizontally for X_2
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_2, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ, 7/16D, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_2, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(9/16D, bounding.minY, bounding.minZ, bounding.maxX, bounding.maxY, bounding.maxZ));
                } else if (direction == Direction.DOWN) {
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_2, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(9/16D, bounding.minY, bounding.minZ, bounding.maxX, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_2, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ, 7/16D, bounding.maxY, bounding.maxZ));
                } else {
                    // For horizontal directions, split vertically for X_2
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_2, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ, bounding.maxX, 7/16D, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_2, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, 9/16D, bounding.minZ, bounding.maxX, bounding.maxY, bounding.maxZ));
                }
            }
        }
        for (Direction direction : CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_2).keySet()) {
            for (VoxelShape voxelShape : CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_2).get(direction)) {
                AABB bounding = voxelShape.toAabbs().get(0);
                if (direction == Direction.UP) {
                    // For UP direction, create a 2x2 grid
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, 9/16D, bounding.maxX, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ, bounding.maxX, bounding.maxY, 7/16D));

                } else if (direction == Direction.DOWN) {
                    // For DOWN direction, create a 2x2 grid
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, 9/16D, bounding.maxX, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ, bounding.maxX, bounding.maxY, 7/16D));
                } else if (direction == Direction.SOUTH) {
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(9/16D, bounding.minY, bounding.minZ, bounding.maxX, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ, 7/16D, bounding.maxY, bounding.maxZ));
                } else if (direction == Direction.NORTH) {
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ, 7/16D, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(9/16D, bounding.minY, bounding.minZ, bounding.maxX, bounding.maxY, bounding.maxZ));
                } else if (direction == Direction.EAST) {
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ, bounding.maxX, bounding.maxY, 7/16D));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, 9/16D, bounding.maxX, bounding.maxY, bounding.maxZ));
                } else {
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, 9/16D, bounding.maxX, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ, bounding.maxX, bounding.maxY, 7/16D));
                }
            }
        }

        for (Direction facing : new Direction[]{Direction.UP, Direction.DOWN}) {

            for (Direction subfacing : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                CACHED_ROTATED_SHAPES.get(facing).put(subfacing, new ArrayList<>());
            }

            Collection<VoxelShape> originalShapes = CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_2).get(facing);
            for (VoxelShape shape : originalShapes) {
                AABB bounds = shape.toAabbs().get(0);

                VoxelShape rotatedShape = Shapes.box(bounds.minZ, bounds.minY, bounds.minX, bounds.maxZ, bounds.maxY, bounds.maxX);

                CACHED_ROTATED_SHAPES.get(facing).get(Direction.NORTH).add(rotatedShape);
                CACHED_ROTATED_SHAPES.get(facing).get(Direction.SOUTH).add(rotatedShape);


                CACHED_ROTATED_SHAPES.get(facing).get(Direction.EAST).add(shape);
                CACHED_ROTATED_SHAPES.get(facing).get(Direction.WEST).add(shape);
            }
        }
    }

    private final FunctionalStorage.DrawerType type;
    private final IWoodType woodType;

    public DrawerBlock(IWoodType woodType, FunctionalStorage.DrawerType type, BlockBehaviour.Properties properties) {
        super(woodType.getName() + "_" + type.getSlots(), properties, DrawerTile.class);
        this.woodType = woodType;
        this.type = type;
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(Drawer.FACING_HORIZONTAL_CUSTOM, Direction.NORTH).setValue(Drawer.FACING_ALL, Direction.DOWN).setValue(LOCKED, false));
    }
    @Override
    public BlockEntityType.BlockEntitySupplier<DrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new DrawerTile(this, (BlockEntityType<DrawerTile>) FunctionalStorage.DRAWER_TYPES.get(type).stream().filter(registryObjectRegistryObjectPair -> registryObjectRegistryObjectPair.getBlock() == this).map(BlockWithTile::type).findFirst().get().get(), blockPos, state, type, woodType);
    }

    @Override
    public List<VoxelShape> getBoundingBoxes(BlockState state, BlockGetter source, BlockPos pos) {
        return getShapes(state, source, pos, this.type);
    }

    private static List<VoxelShape> getShapes(BlockState state, BlockGetter source, BlockPos pos, FunctionalStorage.DrawerType type){
        List<VoxelShape> boxes = new ArrayList<>();
        Direction facing = state.getValue(Drawer.FACING_HORIZONTAL_CUSTOM);

        // For X_2 type, if facing is UP or DOWN, use FACING_ALL to determine rotation
        if (type == FunctionalStorage.DrawerType.X_2 && (facing == Direction.UP || facing == Direction.DOWN)) {
            Direction subfacing = state.getValue(RotatableBlock.FACING_ALL);

            // Use cached rotated shapes
            boxes.addAll(CACHED_ROTATED_SHAPES.get(facing).get(subfacing));
        } else {
            // For other types or directions, use the original shapes
            CACHED_SHAPES.get(type).get(facing).forEach(boxes::add);
        }

        VoxelShape total = Shapes.block();
        boxes.add(total);
        return boxes;
    }

    @Override
    public Collection<VoxelShape> getHitShapes(BlockState state) {
        return getDefaultHitShapes(this.type, state);
    }

    public static Collection<VoxelShape> getDefaultHitShapes(FunctionalStorage.DrawerType type, BlockState state) {
        Direction facing = state.getValue(Drawer.FACING_HORIZONTAL_CUSTOM);
        if (type == FunctionalStorage.DrawerType.X_4 && (facing == Direction.UP || facing == Direction.DOWN)) {
            Direction subfacing = state.getValue(RotatableBlock.FACING_ALL);
            if (facing == Direction.UP){
                var shapes = DrawerBlock.CACHED_SHAPES.get(type).get(facing).stream().toList();
                if (subfacing == Direction.WEST) {
                    return Arrays.asList(shapes.get(3), shapes.get(2), shapes.get(1), shapes.get(0));
                }
                if (subfacing == Direction.SOUTH) {
                    return Arrays.asList(shapes.get(1), shapes.get(3), shapes.get(0), shapes.get(2));
                }
                if (subfacing == Direction.NORTH) {
                    return Arrays.asList(shapes.get(2), shapes.get(0), shapes.get(3), shapes.get(1));
                }
            }
            if (facing == Direction.DOWN){
                var shapes = DrawerBlock.CACHED_SHAPES.get(type).get(facing).stream().toList();
                if (subfacing == Direction.WEST) {
                    return Arrays.asList(shapes.get(1), shapes.get(0), shapes.get(3), shapes.get(2));
                }
                if (subfacing == Direction.SOUTH) {
                    return Arrays.asList(shapes.get(0), shapes.get(2), shapes.get(1), shapes.get(3));
                }
                if (subfacing == Direction.NORTH) {
                    return Arrays.asList(shapes.get(3), shapes.get(1), shapes.get(2), shapes.get(0));
                }
                if (subfacing == Direction.EAST) {
                    return Arrays.asList(shapes.get(2), shapes.get(3), shapes.get(0), shapes.get(1));
                }
            }
        }
        // For X_2 type, if facing is UP or DOWN, use FACING_ALL to determine rotation
        if (type == FunctionalStorage.DrawerType.X_2 && (facing == Direction.UP || facing == Direction.DOWN)) {
            Direction subfacing = state.getValue(RotatableBlock.FACING_ALL);
            if (facing == Direction.UP && (subfacing == Direction.NORTH || subfacing == Direction.WEST)) return CACHED_ROTATED_SHAPES.get(facing).get(subfacing).reversed();
            if (facing == Direction.DOWN && (subfacing == Direction.SOUTH || subfacing == Direction.EAST)) return CACHED_ROTATED_SHAPES.get(facing).get(subfacing).reversed();

            // Use cached rotated shapes
            return CACHED_ROTATED_SHAPES.get(facing).get(subfacing);
        } else {
            return DrawerBlock.CACHED_SHAPES.get(type).get(facing);
        }
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        if (type == FunctionalStorage.DrawerType.X_1) {
            if (woodType.getName().equals("oak")){
                TitaniumShapedRecipeBuilder.shapedRecipe(this)
                        .setName(com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "oak_drawer_alternate_x1"))
                        .pattern("PPP").pattern("PCP").pattern("PPP")
                        .define('P', new DrawerlessWoodIngredient().toVanilla())
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .save(consumer);
            } else {
                TitaniumShapedRecipeBuilder.shapedRecipe(this)
                        .pattern("PPP").pattern("PCP").pattern("PPP")
                        .define('P', woodType.getPlanks())
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .save(consumer);
            }
        }
        if (type == FunctionalStorage.DrawerType.X_2){
            if (woodType.getName().equals("oak")){
                TitaniumShapedRecipeBuilder.shapedRecipe(this, 2)
                        .setName(com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "oak_drawer_alternate_x2"))
                        .pattern("PCP").pattern("PPP").pattern("PCP")
                        .define('P', new DrawerlessWoodIngredient().toVanilla())
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .save(consumer);
            } else {
                TitaniumShapedRecipeBuilder.shapedRecipe(this, 2)
                        .pattern("PCP").pattern("PPP").pattern("PCP")
                        .define('P', woodType.getPlanks())
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .save(consumer);
            }
        }
        if (type == FunctionalStorage.DrawerType.X_4){
            if (woodType.getName().equals("oak")){
                TitaniumShapedRecipeBuilder.shapedRecipe(this, 4)
                        .setName(com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "oak_drawer_alternate_x4"))
                        .pattern("CPC").pattern("PPP").pattern("CPC")
                        .define('P', new DrawerlessWoodIngredient().toVanilla())
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .save(consumer);
            } else {
                TitaniumShapedRecipeBuilder.shapedRecipe(this, 4)
                        .pattern("CPC").pattern("PPP").pattern("CPC")
                        .define('P', woodType.getPlanks())
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .save(consumer);
            }
        }
    }

    public FunctionalStorage.DrawerType getType() {
        return type;
    }

    public IWoodType getWoodType() {
        return woodType;
    }

    public static class DrawerItem extends BlockItem{

        private final DrawerBlock drawerBlock;

        public DrawerItem(DrawerBlock drawerBlock, Properties properties, TitaniumTab tab) {
            super(drawerBlock, properties);
            this.drawerBlock = drawerBlock;
        }

        @Nullable
        public IItemHandler initCapabilities(ItemStack stack) {
            return new DrawerStackItemHandler(stack, this.drawerBlock.getType());
        }

        @Override
        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            consumer.accept(new IClientItemExtensions() {
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return switch (drawerBlock.getType()){
                        case X_2 -> DrawerISTER.SLOT_2;
                        case X_4 -> DrawerISTER.SLOT_4;
                        default -> DrawerISTER.SLOT_1;
                    };
                }
            });
        }

    }

}
