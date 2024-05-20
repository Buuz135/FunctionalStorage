package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.buuz135.functionalstorage.client.item.DrawerISTER;
import com.buuz135.functionalstorage.inventory.item.DrawerCapabilityProvider;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.recipe.DrawerlessWoodIngredient;
import com.buuz135.functionalstorage.util.IWoodType;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.datagenerator.loot.block.BasicBlockLootTables;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.tab.TitaniumTab;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DrawerBlock extends RotatableBlock<DrawerTile> {

    public static HashMap<FunctionalStorage.DrawerType, Multimap<Direction, VoxelShape>> CACHED_SHAPES = new HashMap<>();

    public static BooleanProperty LOCKED = BooleanProperty.create("locked");

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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_206840_1_) {
        super.createBlockStateDefinition(p_206840_1_);
        p_206840_1_.add(LOCKED);
    }

    @NotNull
    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<DrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new DrawerTile(this, (BlockEntityType<DrawerTile>) FunctionalStorage.DRAWER_TYPES.get(type).stream().filter(registryObjectRegistryObjectPair -> registryObjectRegistryObjectPair.getLeft().get().equals(this)).map(Pair::getRight).findFirst().get().get(), blockPos, state, type, woodType);
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
        //CopyNbtFunction.Builder nbtBuilder = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY);
        //nbtBuilder.copy("handler",  "BlockEntityTag.handler");
        //nbtBuilder.copy("storageUpgrades",  "BlockEntityTag.storageUpgrades");
        //nbtBuilder.copy("utilityUpgrades",  "BlockEntityTag.utilityUpgrades");
        //return blockLootTables.droppingSelfWithNbt(this, nbtBuilder);
        return blockLootTables.droppingNothing();
    }


    @Override
    public List<ItemStack> getDrops(BlockState p_60537_, LootParams.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity drawerTile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (drawerTile instanceof DrawerTile tile) {
            if (!tile.isEverythingEmpty()) {
                stack.getOrCreateTag().put("Tile", drawerTile.saveWithoutMetadata());
            }
            if (tile.isLocked()){
                stack.getOrCreateTag().putBoolean("Locked", tile.isLocked());
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public NonNullList<ItemStack> getDynamicDrops(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        return NonNullList.create();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState p_49849_, @Nullable LivingEntity p_49850_, ItemStack stack) {
        super.setPlacedBy(level, pos, p_49849_, p_49850_, stack);
        BlockEntity entity = level.getBlockEntity(pos);
        if (stack.hasTag()) {
            if (stack.getTag().contains("Tile")){
                if (entity instanceof ControllableDrawerTile tile) {
                    entity.load(stack.getTag().getCompound("Tile"));
                    tile.markForUpdate();
                }
            }
            if (stack.getTag().contains("Locked")){
                if (entity instanceof ControllableDrawerTile tile) {
                    tile.setLocked(stack.getTag().getBoolean("Locked"));
                }
            }
        }
        var offhand = p_49850_.getOffhandItem();
        if (offhand.is(FunctionalStorage.CONFIGURATION_TOOL.get())) {
            var action = ConfigurationToolItem.getAction(offhand);
            if (entity instanceof ControllableDrawerTile tile) {
                if (action == ConfigurationToolItem.ConfigurationAction.LOCKING) {
                    tile.setLocked(true);
                } else if (action.getMax() == 1) {
                    tile.getDrawerOptions().setActive(action, false);
                } else {
                    tile.getDrawerOptions().setAdvancedValue(action, 1);
                }
            }
        }
    }

    @Override
    public void registerRecipe(Consumer<FinishedRecipe> consumer) {
        if (type == FunctionalStorage.DrawerType.X_1) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this)
                    .pattern("PPP").pattern("PCP").pattern("PPP")
                    .define('P', woodType.getPlanks())
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);
            if (woodType.getName().equals("oak")){
                TitaniumShapedRecipeBuilder.shapedRecipe(this)
                        .setName(new ResourceLocation(FunctionalStorage.MOD_ID, "oak_drawer_alternate_x1"))
                        .pattern("PPP").pattern("PCP").pattern("PPP")
                        .define('P', new DrawerlessWoodIngredient())
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .save(consumer);
            }
        }
        if (type == FunctionalStorage.DrawerType.X_2){
            TitaniumShapedRecipeBuilder.shapedRecipe(this, 2)
                    .pattern("PCP").pattern("PPP").pattern("PCP")
                    .define('P', woodType.getPlanks())
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);
            if (woodType.getName().equals("oak")){
                TitaniumShapedRecipeBuilder.shapedRecipe(this, 2)
                        .setName(new ResourceLocation(FunctionalStorage.MOD_ID, "oak_drawer_alternate_x2"))
                        .pattern("PCP").pattern("PPP").pattern("PCP")
                        .define('P', new DrawerlessWoodIngredient())
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .save(consumer);
            }
        }
        if (type == FunctionalStorage.DrawerType.X_4){
            TitaniumShapedRecipeBuilder.shapedRecipe(this, 4)
                    .pattern("CPC").pattern("PPP").pattern("CPC")
                    .define('P', woodType.getPlanks())
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);
            if (woodType.getName().equals("oak")){
                TitaniumShapedRecipeBuilder.shapedRecipe(this, 4)
                        .setName(new ResourceLocation(FunctionalStorage.MOD_ID, "oak_drawer_alternate_x4"))
                        .pattern("CPC").pattern("PPP").pattern("CPC")
                        .define('P', new DrawerlessWoodIngredient())
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

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())){
            TileUtil.getTileEntity(worldIn, pos, DrawerTile.class).ifPresent(tile -> {
                if (tile.getControllerPos() != null) {
                    TileUtil.getTileEntity(worldIn, tile.getControllerPos(), StorageControllerTile.class).ifPresent(drawerControllerTile -> {
                        drawerControllerTile.addConnectedDrawers(LinkingToolItem.ActionMode.REMOVE, pos);
                    });
                }
            });
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public void appendHoverText(ItemStack p_49816_, @Nullable BlockGetter p_49817_, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag p_49819_) {
        super.appendHoverText(p_49816_, p_49817_, tooltip, p_49819_);
        if (p_49816_.hasTag() && p_49816_.getTag().contains("Tile")) {
            MutableComponent text = Component.translatable("drawer.block.contents");
            tooltip.add(text.withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal(""));
        }
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    public boolean isSignalSource(BlockState p_60571_) {
        return true;
    }

    @Override
    public int getSignal(BlockState p_60483_, BlockGetter blockGetter, BlockPos blockPos, Direction p_60486_) {
        ItemControllableDrawerTile tile = TileUtil.getTileEntity(blockGetter, blockPos, ItemControllableDrawerTile.class).orElse(null);
        if (tile != null){
            for (int i = 0; i < tile.getUtilityUpgrades().getSlots(); i++) {
                ItemStack stack = tile.getUtilityUpgrades().getStackInSlot(i);
                if (stack.getItem().equals(FunctionalStorage.REDSTONE_UPGRADE.get())){
                    int redstoneSlot = stack.getOrCreateTag().getInt("Slot");
                    if (redstoneSlot < tile.getStorage().getSlots()) {
                        int amount = tile.getStorage().getStackInSlot(redstoneSlot).getCount() * 14 / tile.getStorage().getSlotLimit(redstoneSlot);
                        return amount + (amount > 0 ? 1 : 0);
                    }
                }
            }
        }
        return 0;
    }


    public static class DrawerItem extends BlockItem{

        private DrawerBlock drawerBlock;

        public DrawerItem(DrawerBlock drawerBlock, Properties properties, TitaniumTab tab) {
            super(drawerBlock, properties);
            this.drawerBlock = drawerBlock;
            tab.getTabList().add(this);
        }

        @Override
        public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
            return super.getTooltipImage(stack);
        }

        @Nullable
        @Override
        public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
            return new DrawerCapabilityProvider(stack, this.drawerBlock.getType());
        }

        @OnlyIn(Dist.CLIENT)
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
