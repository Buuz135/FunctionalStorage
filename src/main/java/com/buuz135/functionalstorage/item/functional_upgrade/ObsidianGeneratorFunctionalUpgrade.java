package com.buuz135.functionalstorage.item.functional_upgrade;

import com.buuz135.functionalstorage.item.FunctionalUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class ObsidianGeneratorFunctionalUpgrade extends FunctionalUpgradeItem {

    public ObsidianGeneratorFunctionalUpgrade() {
        super(new Properties());
    }

    @Override
    public void work(Level level, BlockPos pos, ItemStack stack) {
        super.work(level, pos, stack);
        if (level.getGameTime() % 300 == 0) {
            var capability = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
            if (capability != null){
                ItemHandlerHelper.insertItem(capability, new ItemStack(Blocks.OBSIDIAN), false);
            }
        }
    }
}
