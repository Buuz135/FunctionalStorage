package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.util.CustomFramedDrawerModelData;
import com.buuz135.functionalstorage.util.DrawerWoodType;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.HashMap;

public class FramedDrawerTile extends DrawerTile implements FramedTile {
    @Save
    private CustomFramedDrawerModelData framedDrawerModelData;

    public FramedDrawerTile(BasicTileBlock<DrawerTile> base, BlockEntityType<DrawerTile> blockEntityType, BlockPos pos, BlockState state, FunctionalStorage.DrawerType type) {
        super(base, blockEntityType, pos, state, type, DrawerWoodType.FRAMED);
        this.framedDrawerModelData = new CustomFramedDrawerModelData(new HashMap<>());
    }

    public CustomFramedDrawerModelData getFramedDrawerModelData() {
        return framedDrawerModelData;
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
