package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.util.IWoodType;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.datagenerator.loot.block.BasicBlockLootTables;
import com.hrznstudio.titanium.module.DeferredRegistryHelper;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DrawerBlock extends RotatableBlock<DrawerTile> {

    public static HashMap<FunctionalStorage.DrawerType, Multimap<Direction, VoxelShape>> CACHED_SHAPES = new HashMap<>();

    static {
        CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_1, type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.NORTH, Shapes.box(1/16D, 1/16D, 0, 15/16D, 15/16D, 1/16D));
        CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_1, type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.SOUTH, Shapes.box(1/16D, 1/16D, 1/16D, 15/16D, 15/16D, 1));
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
        //TODO Fix 4x4 they are backwards
        for (Direction direction : CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_2).keySet()) {
            for (VoxelShape voxelShape : CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_2).get(direction)) {
                AABB bounding = voxelShape.toAabbs().get(0);
                if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(9/16D, bounding.minY, bounding.minZ ,bounding.maxX, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ , 7/16D, bounding.maxY, bounding.maxZ));
                } else {
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, bounding.minZ , bounding.maxX, bounding.maxY, 7/16D));
                    CACHED_SHAPES.computeIfAbsent(FunctionalStorage.DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, Shapes.box(bounding.minX, bounding.minY, 9/16D,bounding.maxX, bounding.maxY, bounding.maxZ));
                }
            }
        }
    }

    private final FunctionalStorage.DrawerType type;
    private final IWoodType woodType;

    public DrawerBlock(IWoodType woodType, FunctionalStorage.DrawerType type) {
        super(woodType.getName() + "_" + type.getSlots(), Properties.copy(woodType.getPlanks()), DrawerTile.class);
        this.woodType = woodType;
        this.type = type;
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(RotatableBlock.FACING_HORIZONTAL, Direction.NORTH));
    }

    @Override
    public void addAlternatives(DeferredRegistryHelper registry) {
        super.addAlternatives(registry);
    }

    @NotNull
    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<DrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new DrawerTile(this, blockPos, state, type);
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

    @Nonnull
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        return Shapes.box(0, 0, 0, 1,1,1);
    }

    @Override
    public boolean hasCustomBoxes(BlockState state, BlockGetter source, BlockPos pos) {
        return true;
    }

    @Override
    public boolean hasIndividualRenderVoxelShape() {
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
        return TileUtil.getTileEntity(worldIn, pos, DrawerTile.class).map(drawerTile -> drawerTile.onSlotActivated(player, hand, ray.getDirection(), ray.getLocation().x, ray.getLocation().y, ray.getLocation().z, getHit(state, worldIn, pos, player))).orElse(InteractionResult.PASS);
    }

    @Override
    public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
       TileUtil.getTileEntity(worldIn, pos, DrawerTile.class).ifPresent(drawerTile -> drawerTile.onClicked(player, getHit(state, worldIn, pos, player)));
    }

    public int getHit(BlockState state, Level worldIn, BlockPos pos, Player player) {
        HitResult result = RayTraceUtils.rayTraceSimple(worldIn, player, 32, 0);
        if (result instanceof BlockHitResult) {
            VoxelShape hit = RayTraceUtils.rayTraceVoxelShape((BlockHitResult) result, worldIn, player, 32, 0);
            if (hit != null) {
                if (hit.equals(Shapes.block())) return -1;
                List<VoxelShape> shapes = new ArrayList<>();
                shapes.addAll(CACHED_SHAPES.get(type).get(state.getValue(RotatableBlock.FACING_HORIZONTAL)));
                for (int i = 0; i < shapes.size(); i++) {
                    if (Shapes.joinIsNotEmpty(shapes.get(i), hit, BooleanOp.AND)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public LootTable.Builder getLootTable(@Nonnull BasicBlockLootTables blockLootTables) {
        CopyNbtFunction.Builder nbtBuilder = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY);
        nbtBuilder.copy("handler",  "BlockEntityTag.handler");
        return blockLootTables.droppingSelfWithNbt(this, nbtBuilder);
    }

    @Override
    public NonNullList<ItemStack> getDynamicDrops(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        return NonNullList.create();
    }

    @Override
    public void registerRecipe(Consumer<FinishedRecipe> consumer) {
        if (type == FunctionalStorage.DrawerType.X_1){
            TitaniumShapedRecipeBuilder.shapedRecipe(this)
                    .pattern("PPP").pattern("PCP").pattern("PPP")
                    .define('P', woodType.getPlanks())
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);
        }
        if (type == FunctionalStorage.DrawerType.X_2){
            TitaniumShapedRecipeBuilder.shapedRecipe(this)
                    .pattern("PCP").pattern("PPP").pattern("PCP")
                    .define('P', woodType.getPlanks())
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);
        }
        if (type == FunctionalStorage.DrawerType.X_4){
            TitaniumShapedRecipeBuilder.shapedRecipe(this)
                    .pattern("CPC").pattern("PPP").pattern("CPC")
                    .define('P', woodType.getPlanks())
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);
        }
    }

    public FunctionalStorage.DrawerType getType() {
        return type;
    }

    public IWoodType getWoodType() {
        return woodType;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        TileUtil.getTileEntity(worldIn, pos, DrawerTile.class).ifPresent(tile -> {
            if (tile.getControllerPos() != null){
                TileUtil.getTileEntity(worldIn, tile.getControllerPos(), DrawerControllerTile.class).ifPresent(drawerControllerTile -> {
                    drawerControllerTile.addConnectedDrawers(LinkingToolItem.ActionMode.REMOVE, pos);
                });
            }
        });
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
}
