package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class FramedControllerExtensionTile extends StorageControllerExtensionTile<FramedControllerExtensionTile>{

    @Save
    private FramedDrawerModelData framedDrawerModelData;

    public FramedControllerExtensionTile(BasicTileBlock<FramedControllerExtensionTile> base, BlockEntityType<FramedControllerExtensionTile> entityType, BlockPos pos, BlockState state) {
        super(base, entityType, pos, state);
        this.framedDrawerModelData = new FramedDrawerModelData(new HashMap<>());
    }

    public FramedDrawerModelData getFramedDrawerModelData() {
        return framedDrawerModelData;
    }

    public void setFramedDrawerModelData(FramedDrawerModelData framedDrawerModelData) {
        this.framedDrawerModelData = framedDrawerModelData;
        markForUpdate();
        if(level.isClientSide) requestModelDataUpdate();
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder().with(FramedDrawerModelData.FRAMED_PROPERTY, framedDrawerModelData).build();
    }

    @NotNull
    @Override
    public FramedControllerExtensionTile getSelf() {
        return this;
    }
}
