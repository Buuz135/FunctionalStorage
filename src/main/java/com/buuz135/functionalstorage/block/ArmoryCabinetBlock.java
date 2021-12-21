package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ArmoryCabinetTile;
import com.hrznstudio.titanium.block.RotatableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

public class ArmoryCabinetBlock extends RotatableBlock<ArmoryCabinetTile> {

    public ArmoryCabinetBlock() {
        super("armory_cabinet", Properties.copy(Blocks.IRON_BLOCK), ArmoryCabinetTile.class);
        setItemGroup(FunctionalStorage.TAB);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (p_155268_, p_155269_) -> new ArmoryCabinetTile(this, p_155268_, p_155269_);
    }

    @NotNull
    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }
}
