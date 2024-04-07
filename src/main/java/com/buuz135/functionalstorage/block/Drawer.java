package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedDrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedSimpleCompactingDrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.datagenerator.loot.block.BasicBlockLootTables;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Drawer<T extends ControllableDrawerTile<T>> extends RotatableBlock<T> {
    public Drawer(String name, Properties properties, Class<T> tileClass) {
        super(name, properties, tileClass);
    }

    @Nullable
    public ControllableDrawerTile<?> getBlockEntityAt(BlockGetter level, BlockPos pos) {
        return TileUtil.getTileEntity(level, pos, ControllableDrawerTile.class).orElse(null);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> def) {
        super.createBlockStateDefinition(def);
        def.add(DrawerBlock.LOCKED);
    }

    @NotNull
    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Nonnull
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        return Shapes.box(0, 0, 0, 1, 1, 1);
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
        var entity = getBlockEntityAt(level, pos);
        if (entity != null) {
            return entity.onSlotActivated(player, hand, ray.getDirection(), ray.getLocation().x, ray.getLocation().y, ray.getLocation().z, getHit(state, level, player));
        }
        return InteractionResult.PASS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_60537_, LootParams.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity drawerTile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (drawerTile instanceof ControllableDrawerTile<?> tile) {
            copyTo((T) tile, stack);
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public LootTable.Builder getLootTable(@Nonnull BasicBlockLootTables blockLootTables) {
        return blockLootTables.droppingNothing();
    }

    @Override
    public NonNullList<ItemStack> getDynamicDrops(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        return NonNullList.create();
    }

    protected void copyTo(T tile, ItemStack stack) {
        if (!tile.isEverythingEmpty()) {
            stack.setData(FSAttachments.TILE, tile.saveWithoutMetadata());
        }
        if (tile.isLocked()) {
            stack.setData(FSAttachments.LOCKED, tile.isLocked());
        }
        if (tile instanceof FramedTile framedDrawerTile && framedDrawerTile.getFramedDrawerModelData() != null) {
            stack.setData(FSAttachments.STYLE, framedDrawerTile.getFramedDrawerModelData().serializeNBT());
        }
    }

    protected void copyFrom(ItemStack stack, T tile) {
        tile.setLocked(stack.getData(FSAttachments.LOCKED));
        if (stack.hasData(FSAttachments.TILE)) {
            tile.load(stack.getData(FSAttachments.TILE));
            tile.markForUpdate();
        }
        if (stack.hasData(FSAttachments.STYLE) && tile instanceof FramedTile framed) {
            framed.setFramedDrawerModelData(FramedDrawerBlock.getDrawerModelData(stack));
        }
    }

    protected void configure(LivingEntity player, T tile) {
        var offhand = player.getOffhandItem();
        if (offhand.is(FunctionalStorage.CONFIGURATION_TOOL.get())) {
            var action = ConfigurationToolItem.getAction(offhand);
            if (action == ConfigurationToolItem.ConfigurationAction.LOCKING) {
                tile.setLocked(true);
            } else if (action.getMax() == 1) {
                tile.getDrawerOptions().setActive(action, false);
            } else {
                tile.getDrawerOptions().setAdvancedValue(action, 1);
            }
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState p_49849_, @org.jetbrains.annotations.Nullable LivingEntity player, ItemStack stack) {
        super.setPlacedBy(level, pos, p_49849_, player, stack);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof ControllableDrawerTile<?> tile) {
            copyFrom(stack, (T) tile);
            if (player != null) configure(player, (T) tile);
        }
    }

    @Override
    public boolean isSignalSource(BlockState p_60571_) {
        return true;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @org.jetbrains.annotations.Nullable Direction direction) {
        ControllableDrawerTile<?> tile = TileUtil.getTileEntity(level, pos, ControllableDrawerTile.class).orElse(null);
        if (tile != null) {
            for (int i = 0; i < tile.getUtilityUpgrades().getSlots(); i++) {
                ItemStack stack = tile.getUtilityUpgrades().getStackInSlot(i);
                if (stack.is(FunctionalStorage.REDSTONE_UPGRADE.get())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getSignal(BlockState p_60483_, BlockGetter blockGetter, BlockPos blockPos, Direction p_60486_) {
        ItemControllableDrawerTile<?> tile = TileUtil.getTileEntity(blockGetter, blockPos, ItemControllableDrawerTile.class).orElse(null);
        if (tile != null){
            for (int i = 0; i < tile.getUtilityUpgrades().getSlots(); i++) {
                ItemStack stack = tile.getUtilityUpgrades().getStackInSlot(i);
                if (stack.getItem().equals(FunctionalStorage.REDSTONE_UPGRADE.get())){
                    int redstoneSlot = stack.getData(FSAttachments.SLOT);
                    if (redstoneSlot < tile.getStorage().getSlots()) {
                        int amount = tile.getStorage().getStackInSlot(redstoneSlot).getCount() * 14 / tile.getStorage().getSlotLimit(redstoneSlot);
                        return amount + (amount > 0 ? 1 : 0);
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable BlockGetter p_49817_, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag p_49819_) {
        super.appendHoverText(stack, p_49817_, tooltip, p_49819_);
        if (stack.hasData(FSAttachments.TILE)) {
            MutableComponent text = Component.translatable("drawer.block.contents");
            tooltip.add(text.withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal(""));
        }

        if (this instanceof FramedBlock) {
            tooltip.add(Component.translatable("frameddrawer.use").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        BlockEntity entity = level.getBlockEntity(pos);
        ItemStack stack = new ItemStack(this);
        if (entity instanceof FramedTile framedDrawerTile && framedDrawerTile.getFramedDrawerModelData() != null && !framedDrawerTile.getFramedDrawerModelData().getDesign().isEmpty()) {
            stack.setData(FSAttachments.STYLE, framedDrawerTile.getFramedDrawerModelData().serializeNBT());
            return stack;
        }
        if (entity instanceof ControllableDrawerTile<?> tile) {
            copyTo((T) tile, stack);
        }
        return stack;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())){
            TileUtil.getTileEntity(worldIn, pos, ControllableDrawerTile.class).ifPresent(tile -> {
                if (tile.getControllerPos() != null) {
                    TileUtil.getTileEntity(worldIn, tile.getControllerPos(), StorageControllerTile.class).ifPresent(drawerControllerTile -> {
                        drawerControllerTile.addConnectedDrawers(LinkingToolItem.ActionMode.REMOVE, pos);
                    });
                }
            });
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    public abstract Collection<VoxelShape> getHitShapes(BlockState state);

    public int getHit(BlockState state, Level worldIn, Player player) {
        HitResult result = RayTraceUtils.rayTraceSimple(worldIn, player, 32, 0);
        if (result instanceof BlockHitResult) {
            VoxelShape hit = RayTraceUtils.rayTraceVoxelShape((BlockHitResult) result, worldIn, player, 32, 0);
            if (hit != null) {
                if (hit.equals(Shapes.block())) return -1;
                List<VoxelShape> shapes = new ArrayList<>(getHitShapes(state));
                for (int i = 0; i < shapes.size(); i++) {
                    if (Shapes.joinIsNotEmpty(shapes.get(i), hit, BooleanOp.AND)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
}
