package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedTile;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.datagenerator.loot.block.BasicBlockLootTables;
import com.hrznstudio.titanium.nbthandler.NBTManager;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
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

    public static final DirectionProperty FACING_HORIZONTAL_CUSTOM = DirectionProperty.create("subfacing", Direction.values());

    public Drawer(String name, Properties properties, Class<T> tileClass) {
        super(name, properties, tileClass);
    }

    @Nullable
    public ControllableDrawerTile<?> getBlockEntityAt(BlockGetter level, BlockPos pos) {
        return TileUtil.getTileEntity(level, pos, ControllableDrawerTile.class).orElse(null);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> def) {
        if (this.getRotationType() == RotationType.FOUR_WAY) {
            super.createBlockStateDefinition(def);
        } else {
            def.add(DrawerBlock.FACING_HORIZONTAL_CUSTOM);
            def.add(RotatableBlock.FACING_ALL);
        }
        def.add(DrawerBlock.LOCKED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (this.getRotationType() == RotationType.FOUR_WAY) {
            return super.getStateForPlacement(context);
        }
        var direction = context.getNearestLookingDirection().getOpposite();
        if (direction.getAxis().isHorizontal()) {
            var blockstate = this.defaultBlockState().setValue(Drawer.FACING_HORIZONTAL_CUSTOM, direction).setValue(RotatableBlock.FACING_ALL, Direction.DOWN);
            return blockstate;
        }
        var blockstate  = this.defaultBlockState().setValue(Drawer.FACING_HORIZONTAL_CUSTOM, direction).setValue(RotatableBlock.FACING_ALL, direction == Direction.DOWN ? context.getHorizontalDirection().getOpposite()  : context.getHorizontalDirection());
        return blockstate;
    }

    @NotNull
    @Override
    public RotationType getRotationType() {
        return RotationType.TWENTY_FOUR_WAY;
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
        var entity = getBlockEntityAt(worldIn, pos);
        if (entity != null) {
            var result = entity.onSlotActivated(player, hand, ray.getDirection(), ray.getLocation().x, ray.getLocation().y, ray.getLocation().z, getHit(state, worldIn, player));
            if (result == InteractionResult.SUCCESS) {
                return ItemInteractionResult.SUCCESS;
            } else if (result.consumesAction()) {
                return ItemInteractionResult.CONSUME;
            } else {
                // TODO - validate if this is ok
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
        }
        return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult ray) {
        var entity = getBlockEntityAt(worldIn, pos);
        if (entity != null) {
            return entity.onSlotActivated(player, InteractionHand.MAIN_HAND, ray.getDirection(), ray.getLocation().x, ray.getLocation().y, ray.getLocation().z, getHit(state, worldIn, player));
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
            var compound = NBTManager.getInstance().writeTileEntity(tile, new CompoundTag());
            compound.put("storageUpgrades", tile.getStorageUpgrades().serializeNBT(tile.getLevel().registryAccess()));
            stack.set(FSAttachments.TILE, compound);
        }
        if (tile.isLocked()) {
            stack.set(FSAttachments.LOCKED, tile.isLocked());
        }
        if (tile instanceof FramedTile framedDrawerTile && framedDrawerTile.getFramedDrawerModelData() != null) {
            stack.set(FSAttachments.STYLE, framedDrawerTile.getFramedDrawerModelData().serializeNBT(tile.getLevel().registryAccess()));
        }
    }

    protected void copyFrom(ItemStack stack, T tile) {
        tile.setLocked(stack.getOrDefault(FSAttachments.LOCKED, false));
        if (stack.has(FSAttachments.TILE)) {
            tile.loadAdditional(stack.get(FSAttachments.TILE), tile.getLevel().registryAccess());
            tile.markForUpdate();
        }
        if (stack.has(FSAttachments.STYLE) && tile instanceof FramedTile framed) {
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
                var comp = stack.get(FSAttachments.FUNCTIONAL_BEHAVIOR);
                if (comp != null && comp.canConnectRedstone(tile.getLevel(), pos, state, tile, direction, stack, i)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter blockGetter, BlockPos blockPos, Direction dir) {
        ControllableDrawerTile<?> tile = TileUtil.getTileEntity(blockGetter, blockPos, ControllableDrawerTile.class).orElse(null);
        if (tile != null){
            for (int i = 0; i < tile.getUtilityUpgrades().getSlots(); i++) {
                ItemStack stack = tile.getUtilityUpgrades().getStackInSlot(i);
                var comp = stack.get(FSAttachments.FUNCTIONAL_BEHAVIOR);
                if (comp != null) {
                    var signal = comp.getRedstoneSignal(tile.getLevel(), blockPos, state, tile, dir, stack, i);
                    if (signal >= 0) {
                        return signal;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (stack.has(FSAttachments.TILE)) {
            MutableComponent text = Component.translatable("drawer.block.contents");
            tooltipComponents.add(text.withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.literal(""));
            tooltipComponents.add(Component.literal(""));
            CompoundTag tile = stack.get(FSAttachments.TILE);
            tooltipComponents.add(Component.translatable("drawer.block.upgrades").withStyle(ChatFormatting.GRAY));
            var anyupgrade = false;
            if (tile.contains("isCreative") && tile.getBoolean("isCreative")) {
                tooltipComponents.add(Component.literal("- ").withStyle(ChatFormatting.GRAY).append(Component.translatable("drawer.block.upgrades.is_creative").withStyle(ChatFormatting.LIGHT_PURPLE)));
                anyupgrade = true;
            }
            if (tile.contains("isVoid") && tile.getBoolean("isVoid")) {
                tooltipComponents.add(Component.literal("- ").withStyle(ChatFormatting.GRAY).append(Component.translatable("drawer.block.upgrades.is_void").withStyle(ChatFormatting.BLUE)));
                anyupgrade = true;
            }
            if (!anyupgrade) {
                tooltipComponents.add(Component.literal("- ").withStyle(ChatFormatting.GRAY).append(Component.translatable("drawer.block.upgrades.none").withStyle(ChatFormatting.GRAY)));
            }
        }

        if (this instanceof FramedBlock) {
            tooltipComponents.add(Component.translatable("frameddrawer.use").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        BlockEntity entity = level.getBlockEntity(pos);
        ItemStack stack = new ItemStack(this);
        if (entity instanceof FramedTile framedDrawerTile && framedDrawerTile.getFramedDrawerModelData() != null && !framedDrawerTile.getFramedDrawerModelData().getDesign().isEmpty()) {
            stack.set(FSAttachments.STYLE, framedDrawerTile.getFramedDrawerModelData().serializeNBT(level.registryAccess()));
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
