package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.util.CustomFramedDrawerModelData;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class FramedDrawerControllerTile extends StorageControllerTile<FramedDrawerControllerTile> implements FramedTile {

    @Save
    private CustomFramedDrawerModelData framedDrawerModelData;

    public FramedDrawerControllerTile(BasicTileBlock<FramedDrawerControllerTile> base, BlockEntityType<FramedDrawerControllerTile> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state);
        this.framedDrawerModelData = new CustomFramedDrawerModelData(new HashMap<>());
    }

    public CustomFramedDrawerModelData getFramedDrawerModelData() { return framedDrawerModelData; }

    public void setFramedDrawerModelData(CustomFramedDrawerModelData framedDrawerModelData) {
        this.framedDrawerModelData = framedDrawerModelData;
        requestModelDataUpdate();
        markForUpdate();
    }

    @Override
    public ModelData getModelData() {
        return ModelData.of(CustomFramedDrawerModelData.MODEL_PROPERTY, framedDrawerModelData);
    }

    @NotNull
    @Override
    public FramedDrawerControllerTile getSelf() { return this; }
}
