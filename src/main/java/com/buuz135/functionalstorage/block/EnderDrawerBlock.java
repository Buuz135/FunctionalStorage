package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.EnderDrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.datagenerator.loot.block.BasicBlockLootTables;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.buuz135.functionalstorage.block.DrawerBlock.LOCKED;

public class EnderDrawerBlock extends RotatableBlock<EnderDrawerTile> {

    public EnderDrawerBlock() {
        super("ender_drawer", Properties.ofFullCopy(Blocks.ENDER_CHEST), EnderDrawerTile.class);
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(RotatableBlock.FACING_HORIZONTAL, Direction.NORTH).setValue(LOCKED, false));
    }

    public static HashMap<String, List<ItemStack>> FREQUENCY_LOOK = new HashMap<>();

    public static List<ItemStack> getFrequencyDisplay(String string){
        return FREQUENCY_LOOK.computeIfAbsent(string, s -> {
            List<Item> minecraftItems = BuiltInRegistries.ITEM.stream().filter(item -> item != Items.AIR && BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("minecraft") && !(item instanceof BlockItem)).collect(Collectors.toList());
            return Arrays.stream(string.split("-")).map(s1 -> new ItemStack(minecraftItems.get(Math.abs(s1.hashCode()) % minecraftItems.size()))).collect(Collectors.toList());
        });
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
    public BlockEntityType.BlockEntitySupplier<EnderDrawerTile> getTileEntityFactory() {
        return (blockPos, state) -> new EnderDrawerTile(this, (BlockEntityType<EnderDrawerTile>) FunctionalStorage.ENDER_DRAWER.type().get(),blockPos, state);
    }

    @Override
    public List<VoxelShape> getBoundingBoxes(BlockState state, BlockGetter source, BlockPos pos) {
        return getShapes(state, source, pos);
    }

    private static List<VoxelShape> getShapes(BlockState state, BlockGetter source, BlockPos pos){
        List<VoxelShape> boxes = new ArrayList<>();
        DrawerBlock.CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_1).get(state.getValue(RotatableBlock.FACING_HORIZONTAL)).forEach(boxes::add);
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
        return TileUtil.getTileEntity(worldIn, pos, EnderDrawerTile.class).map(drawerTile -> drawerTile.onSlotActivated(player, hand, ray.getDirection(), ray.getLocation().x, ray.getLocation().y, ray.getLocation().z, getHit(state, worldIn, pos, player))).orElse(InteractionResult.PASS);
    }

    @Override
    public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
       TileUtil.getTileEntity(worldIn, pos, EnderDrawerTile.class).ifPresent(drawerTile -> drawerTile.onClicked(player, getHit(state, worldIn, pos, player)));
    }

    public int getHit(BlockState state, Level worldIn, BlockPos pos, Player player) {
        HitResult result = RayTraceUtils.rayTraceSimple(worldIn, player, 32, 0);
        if (result instanceof BlockHitResult) {
            VoxelShape hit = RayTraceUtils.rayTraceVoxelShape((BlockHitResult) result, worldIn, player, 32, 0);
            if (hit != null) {
                if (hit.equals(Shapes.block())) return -1;
                List<VoxelShape> shapes = new ArrayList<>(DrawerBlock.CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_1).get(state.getValue(RotatableBlock.FACING_HORIZONTAL)));
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
        //nbtBuilder.copy("frequency",  "BlockEntityTag.frequency");
        return blockLootTables.droppingNothing();
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_60537_, LootParams.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity drawerTile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (drawerTile instanceof EnderDrawerTile tile) {
            if (!tile.isEverythingEmpty()) {
                stack.getOrCreateTag().put("Tile", drawerTile.saveWithoutMetadata());
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState p_49849_, @Nullable LivingEntity p_49850_, ItemStack stack) {
        super.setPlacedBy(level, pos, p_49849_, p_49850_, stack);
        if (stack.hasTag()) {
            if (stack.getTag().contains("Tile")) {
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof ControllableDrawerTile tile) {
                    entity.load(stack.getTag().getCompound("Tile"));
                    tile.markForUpdate();
                }
            }
        }
    }

    @Override
    public NonNullList<ItemStack> getDynamicDrops(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        return NonNullList.create();
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())){
            TileUtil.getTileEntity(worldIn, pos, EnderDrawerTile.class).ifPresent(tile -> {
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
    public void appendHoverText(ItemStack p_49816_, @Nullable BlockGetter p_49817_, List<Component> tooltip, TooltipFlag p_49819_) {
        super.appendHoverText(p_49816_, p_49817_, tooltip, p_49819_);
        if (p_49816_.hasTag()) {
            MutableComponent text = Component.translatable("linkingtool.ender.frequency");
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
                if (stack.getItem().equals(FunctionalStorage.REDSTONE_UPGRADE.get())) {
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
}
