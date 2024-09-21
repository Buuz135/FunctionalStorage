package com.buuz135.functionalstorage.item.functional_upgrade;

import com.buuz135.functionalstorage.item.FunctionalUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class WaterGeneratorFunctionalUpgrade extends FunctionalUpgradeItem {

    public WaterGeneratorFunctionalUpgrade() {
        super(new Properties());
    }

    @Override
    public void work(Level level, BlockPos pos) {
        super.work(level, pos);
        var capability = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.UP);
        if (capability != null){
            capability.fill(new FluidStack(Fluids.WATER, 2000), IFluidHandler.FluidAction.EXECUTE);
        }
    }
}
