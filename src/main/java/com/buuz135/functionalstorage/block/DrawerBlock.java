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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class DrawerBlock extends Drawer<DrawerTile> {

    public static final HashMap<FunctionalStorage.DrawerType, Multimap<Direction, VoxelShape>> CACHED_SHAPES = new HashMap<>();

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
        for (Direction direction : CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_1).keySet()) {
            for (VoxelShape voxelShape : CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_1).get(direction)) {
                AABB bounding = voxelShape.toAabbs().get(0);
                CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_2, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                        put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ ,bounding.maxX, 7/16D, bounding.maxZ));
                CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_2, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                        put(direction, Shapes.box(bounding.minX, 9/16D, bounding.minZ ,bounding.maxX, bounding.maxY, bounding.maxZ));
            }
        }
        for (Direction direction : CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_2).keySet()) {
            for (VoxelShape voxelShape : CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_2).get(direction)) {
                AABB bounding = voxelShape.toAabbs().get(0);
                if (direction == Direction.SOUTH) {
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(9/16D, bounding.minY, bounding.minZ ,bounding.maxX, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ , 7/16D, bounding.maxY, bounding.maxZ));
                }else if (direction == Direction.NORTH){
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ , 7/16D, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(9/16D, bounding.minY, bounding.minZ ,bounding.maxX, bounding.maxY, bounding.maxZ));
                } else if (direction == Direction.EAST){
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ , bounding.maxX, bounding.maxY, 7/16D));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, 9/16D,bounding.maxX, bounding.maxY, bounding.maxZ));
                } else {
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, 9/16D,bounding.maxX, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ , bounding.maxX, bounding.maxY, 7/16D));
                }
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
        registerDefaultState(defaultBlockState().setValue(RotatableBlock.FACING_HORIZONTAL, Direction.NORTH).setValue(LOCKED, false));
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
        CACHED_SHAPES.get(type).get(state.getValue(RotatableBlock.FACING_HORIZONTAL)).forEach(boxes::add);
        VoxelShape total = Shapes.block();
        boxes.add(total);
        return boxes;
    }

    @Override
    public Collection<VoxelShape> getHitShapes(BlockState state) {
        return DrawerBlock.CACHED_SHAPES.get(type).get(state.getValue(RotatableBlock.FACING_HORIZONTAL));
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
