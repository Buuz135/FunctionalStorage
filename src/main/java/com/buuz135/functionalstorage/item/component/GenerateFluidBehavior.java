package com.buuz135.functionalstorage.item.component;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

public record GenerateFluidBehavior(FluidStackTemplate fluid) implements FunctionalUpgradeBehavior {
    public static final MapCodec<GenerateFluidBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            FluidStackTemplate.CODEC.fieldOf("fluid").forGetter(GenerateFluidBehavior::fluid)
    ).apply(in, GenerateFluidBehavior::new));

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack, int upgradeSlot) {
        var capability = level.getCapability(Capabilities.Fluid.BLOCK, pos, Direction.UP);
        if (capability != null) {
            try (Transaction transaction = Transaction.openRoot()) {
                capability.insert(FluidResource.of(fluid), fluid.amount(), transaction);
                transaction.commit();
            }
        }
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }

    @Override
    public List<Component> getTooltip() {
        var list = FunctionalUpgradeBehavior.super.getTooltip();
        var stack = fluid.create();
        list.add(Component.translatable("functionalupgrade.desc.generate_fluid", stack.getAmount(), stack.getHoverName().getString()));
        return list;
    }
}
