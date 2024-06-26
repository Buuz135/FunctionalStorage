package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.EnderDrawerTile;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.buuz135.functionalstorage.block.DrawerBlock.LOCKED;

public class EnderDrawerBlock extends Drawer<EnderDrawerTile> {

    public EnderDrawerBlock() {
        super("ender_drawer", Properties.ofFullCopy(Blocks.ENDER_CHEST), EnderDrawerTile.class);
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(RotatableBlock.FACING_HORIZONTAL, Direction.NORTH).setValue(LOCKED, false));
    }

    public static final HashMap<String, List<ItemStack>> FREQUENCY_LOOK = new HashMap<>();

    public static List<ItemStack> getFrequencyDisplay(String string){
        return FREQUENCY_LOOK.computeIfAbsent(string, s -> {
            List<Item> minecraftItems = BuiltInRegistries.ITEM.stream().filter(item -> item != Items.AIR && BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("minecraft") && !(item instanceof BlockItem)).collect(Collectors.toList());
            return Arrays.stream(string.split("-")).map(s1 -> new ItemStack(minecraftItems.get(Math.abs(s1.hashCode()) % minecraftItems.size()))).collect(Collectors.toList());
        });
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

    @Override
    public Collection<VoxelShape> getHitShapes(BlockState state) {
        return DrawerBlock.CACHED_SHAPES.get(FunctionalStorage.DrawerType.X_1).get(state.getValue(RotatableBlock.FACING_HORIZONTAL));
    }

    @Override
    protected void copyTo(EnderDrawerTile tile, ItemStack stack) {
        if (!tile.isEverythingEmpty()) {
            stack.set(FSAttachments.TILE, tile.saveWithoutMetadata(tile.getLevel().registryAccess()));
        }
    }

    @Override
    protected void configure(LivingEntity player, EnderDrawerTile tile) {
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (stack.has(FSAttachments.TILE)) {
            MutableComponent text = Component.translatable("linkingtool.ender.frequency");
            tooltipComponents.add(text.withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.literal(""));
            tooltipComponents.add(Component.literal(""));
        }

    }

}
