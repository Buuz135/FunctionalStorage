package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import com.buuz135.functionalstorage.util.DrawerWoodType;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import javax.annotation.Nonnull;
import java.util.HashMap;

public class FramedDrawerTile extends DrawerTile implements FramedTile {
    @Save
    private FramedDrawerModelData framedDrawerModelData;

    public FramedDrawerTile(BasicTileBlock<DrawerTile> base, BlockEntityType<DrawerTile> blockEntityType, BlockPos pos, BlockState state, FunctionalStorage.DrawerType type) {
        super(base, blockEntityType, pos, state, type, DrawerWoodType.FRAMED);
        this.framedDrawerModelData = new FramedDrawerModelData(new HashMap<>());
    }

    public FramedDrawerModelData getFramedDrawerModelData() {
        return framedDrawerModelData;
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
