package com.buuz135.functionalstorage.item.component;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public record GenerateFluidBehavior(FluidStack fluid) implements FunctionalUpgradeBehavior {
    public static final MapCodec<GenerateFluidBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            FluidStack.CODEC.fieldOf("fluid").forGetter(GenerateFluidBehavior::fluid)
    ).apply(in, GenerateFluidBehavior::new));

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack, int upgradeSlot) {
        var capability = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.UP);
        if (capability != null) {
            capability.fill(fluid.copy(), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
