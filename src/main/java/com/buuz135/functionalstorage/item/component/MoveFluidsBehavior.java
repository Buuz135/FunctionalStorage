package com.buuz135.functionalstorage.item.component;

import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
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
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public record MoveFluidsBehavior(boolean drawerIsSource, int fluidPerOperation) implements FunctionalUpgradeBehavior {
    public static final MapCodec<MoveFluidsBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            Codec.BOOL.fieldOf("drawer_is_source").forGetter(MoveFluidsBehavior::drawerIsSource),
            Codec.INT.fieldOf("fluid_per_operation").forGetter(MoveFluidsBehavior::fluidPerOperation)
    ).apply(in, MoveFluidsBehavior::new));

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> dr, ItemStack upgradeStack, int upgradeSlot) {
        if (!(dr instanceof FluidDrawerTile drawer)) return;

        Direction direction = UpgradeItem.getDirection(upgradeStack);
        var otherFluidHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos.relative(direction), direction.getOpposite());

        if (otherFluidHandler != null) {
            if (drawerIsSource) {
                for (int tankId = 0; tankId < drawer.getFluidHandler().getTanks(); tankId++) {
                    var fluidTank = drawer.fluidHandler.getTankList()[tankId];
                    if (fluidTank.getFluid().isEmpty()) continue;
                    var extracted = fluidTank.drain(FunctionalStorageConfig.UPGRADE_PUSH_FLUID, IFluidHandler.FluidAction.SIMULATE);
                    if (extracted.isEmpty()) continue;
                    var insertedAmount = otherFluidHandler.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
                    if (insertedAmount > 0) {
                        fluidTank.drain(insertedAmount, IFluidHandler.FluidAction.EXECUTE);
                        drawer.fluidHandler.onChange();
                        break;
                    }
                }
            } else {
                for (int tankId = 0; tankId < drawer.getFluidHandler().getTanks(); tankId++) {
                    var fluidTank = drawer.fluidHandler.getTankList()[tankId];
                    var extracted = otherFluidHandler.drain(FunctionalStorageConfig.UPGRADE_PULL_FLUID, IFluidHandler.FluidAction.SIMULATE);
                    if (extracted.isEmpty()) continue;
                    var insertedAmount = fluidTank.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
                    if (insertedAmount > 0) {
                        otherFluidHandler.drain(insertedAmount, IFluidHandler.FluidAction.EXECUTE);
                        drawer.fluidHandler.onChange();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
