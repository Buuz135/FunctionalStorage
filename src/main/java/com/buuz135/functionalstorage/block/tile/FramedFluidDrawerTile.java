package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.util.CustomFramedDrawerModelData;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.HashMap;

public class FramedFluidDrawerTile extends FluidDrawerTile implements FramedTile{

    @Save
    private CustomFramedDrawerModelData framedDrawerModelData;

    public FramedFluidDrawerTile(BasicTileBlock<FluidDrawerTile> base, BlockEntityType<FluidDrawerTile> blockEntityType, BlockPos pos, BlockState state, FunctionalStorage.DrawerType type) {
        super(base, blockEntityType, pos, state, type);
        this.framedDrawerModelData = new CustomFramedDrawerModelData(new HashMap<>());
    }

    public CustomFramedDrawerModelData getFramedDrawerModelData() {
        return framedDrawerModelData;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState stateOwn, FluidDrawerTile blockEntity) {
        super.serverTick(level, pos, stateOwn, blockEntity);
    }

    public void setFramedDrawerModelData(CustomFramedDrawerModelData framedDrawerModelData) {
        this.framedDrawerModelData = framedDrawerModelData;
        requestModelDataUpdate();
        markForUpdate();
    }

    @Override
    public ModelData getModelData() {
        return ModelData.of(CustomFramedDrawerModelData.MODEL_PROPERTY, framedDrawerModelData);
    }

}
