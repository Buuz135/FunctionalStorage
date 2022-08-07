package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.CompactingDrawerTile;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DrawerControllerBlock extends RotatableBlock<DrawerControllerTile> {

    public DrawerControllerBlock() {
        super("storage_controller", Properties.copy(Blocks.IRON_BLOCK), DrawerControllerTile.class);
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(RotatableBlock.FACING_HORIZONTAL, Direction.NORTH).setValue(DrawerBlock.LOCKED, false));
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (p_155268_, p_155269_) -> new DrawerControllerTile(this, (BlockEntityType<DrawerControllerTile>) FunctionalStorage.DRAWER_CONTROLLER.getRight().get(), p_155268_, p_155269_);
    }

    @NotNull
    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_206840_1_) {
        super.createBlockStateDefinition(p_206840_1_);
        p_206840_1_.add(DrawerBlock.LOCKED);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
        return TileUtil.getTileEntity(worldIn, pos, DrawerControllerTile.class).map(drawerTile -> drawerTile.onSlotActivated(player, hand, ray.getDirection(), ray.getLocation().x, ray.getLocation().y, ray.getLocation().z)).orElse(InteractionResult.PASS);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        TileUtil.getTileEntity(worldIn, pos, DrawerControllerTile.class).ifPresent(drawerControllerTile -> {
            drawerControllerTile.getConnectedDrawers().getConnectedDrawers().stream()
                    .map(BlockPos::of)
                    .map(blockPos -> TileUtil.getTileEntity(worldIn, blockPos, ControllableDrawerTile.class))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(controllableDrawerTile -> controllableDrawerTile.setControllerPos(null));
        });
        super.onRemove(state, worldIn, pos, newState, isMoving);

    }
}
