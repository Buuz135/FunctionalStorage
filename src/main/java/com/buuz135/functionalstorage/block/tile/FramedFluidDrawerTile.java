package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class FramedFluidDrawerTile extends FluidDrawerTile implements FramedTile{

    @Save
    private FramedDrawerModelData framedDrawerModelData;

    public FramedFluidDrawerTile(BasicTileBlock<FluidDrawerTile> base, BlockEntityType<FluidDrawerTile> blockEntityType, BlockPos pos, BlockState state, FunctionalStorage.DrawerType type) {
        super(base, blockEntityType, pos, state, type);
        this.framedDrawerModelData = new FramedDrawerModelData(new HashMap<>());
    }

    public FramedDrawerModelData getFramedDrawerModelData() {
        return framedDrawerModelData;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState stateOwn, FluidDrawerTile blockEntity) {
        super.serverTick(level, pos, stateOwn, blockEntity);
    }

    public void setFramedDrawerModelData(FramedDrawerModelData framedDrawerModelData) {
        this.framedDrawerModelData = framedDrawerModelData;
        markForUpdate();
        if (level.isClientSide) requestModelDataUpdate();
    }

    @Nonnull
    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(FramedDrawerModelData.FRAMED_PROPERTY, framedDrawerModelData).build();
    }
}
