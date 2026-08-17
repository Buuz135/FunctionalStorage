package com.buuz135.functionalstorage.item.component;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public record MoveFluidsBehavior(boolean drawerIsSource, int fluidPerOperation) implements FunctionalUpgradeBehavior {
    public static final MapCodec<MoveFluidsBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            Codec.BOOL.fieldOf("drawer_is_source").forGetter(MoveFluidsBehavior::drawerIsSource),
            Codec.INT.fieldOf("fluid_per_operation").forGetter(MoveFluidsBehavior::fluidPerOperation)
    ).apply(in, MoveFluidsBehavior::new));

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> dr, ItemStack upgradeStack, int upgradeSlot) {
        if (!(dr instanceof FluidDrawerTile drawer)) return;

        Direction direction = UpgradeItem.getDirection(upgradeStack);
        var otherResourceHandler = level.getCapability(Capabilities.Fluid.BLOCK, pos.relative(direction), direction.getOpposite());

        if (otherResourceHandler != null) {
            ResourceHandler<FluidResource> source = drawerIsSource ? drawer.getFluidHandler() : otherResourceHandler;
            ResourceHandler<FluidResource> destination = drawerIsSource ? otherResourceHandler : drawer.getFluidHandler();
            ResourceHandlerUtil.move(source, destination, resource -> true, fluidPerOperation, null);
        }
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
