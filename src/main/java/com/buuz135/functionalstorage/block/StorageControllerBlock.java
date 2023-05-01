package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public abstract class StorageControllerBlock<T extends StorageControllerTile<T>> extends RotatableBlock<T> {

    public StorageControllerBlock(String name, Properties properties, Class<T> tileClass) {
        super(name, properties, tileClass);
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(RotatableBlock.FACING_HORIZONTAL, Direction.NORTH).setValue(DrawerBlock.LOCKED, false));
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
        return TileUtil.getTileEntity(worldIn, pos, StorageControllerTile.class).map(storageTile -> storageTile.onSlotActivated(player, hand, ray.getDirection(), ray.getLocation().x, ray.getLocation().y, ray.getLocation().z)).orElse(InteractionResult.PASS);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            TileUtil.getTileEntity(worldIn, pos, StorageControllerTile.class).ifPresent(drawerControllerTile -> {
                for (Long connectedDrawer : new ArrayList<>(drawerControllerTile.getConnectedDrawers().getConnectedDrawers())) {
                    BlockEntity blockEntity = worldIn.getBlockEntity(BlockPos.of(connectedDrawer));
                    if (blockEntity instanceof StorageControllerTile) continue;
                    if (blockEntity instanceof ControllableDrawerTile controllableDrawerTile) {
                        controllableDrawerTile.clearControllerPos();
                    }
                }
            });
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
}
