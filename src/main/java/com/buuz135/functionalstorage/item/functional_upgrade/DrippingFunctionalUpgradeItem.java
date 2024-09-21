package com.buuz135.functionalstorage.item.functional_upgrade;

import com.buuz135.functionalstorage.item.FunctionalUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class DrippingFunctionalUpgradeItem extends FunctionalUpgradeItem {

    public DrippingFunctionalUpgradeItem() {
        super(new Properties());
    }

    @Override
    public void work(Level level, BlockPos pos) {
        super.work(level, pos);
        if (level.getGameTime() % 20 == 0) {
            var capability = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.UP);
            if (capability != null){
                capability.fill(new FluidStack(Fluids.LAVA, 10), IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }
}
