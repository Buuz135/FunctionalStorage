package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class FramedSimpleCompactingDrawerTile extends SimpleCompactingDrawerTile {

    @Save
    private FramedDrawerModelData framedDrawerModelData;

    public FramedSimpleCompactingDrawerTile(BasicTileBlock<SimpleCompactingDrawerTile> base, BlockEntityType<SimpleCompactingDrawerTile> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state);
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
