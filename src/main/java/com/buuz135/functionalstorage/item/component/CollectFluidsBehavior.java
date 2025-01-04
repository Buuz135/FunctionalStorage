package com.buuz135.functionalstorage.item.component;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.wrappers.BucketPickupHandlerWrapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record CollectFluidsBehavior() implements FunctionalUpgradeBehavior {
    public static final CollectFluidsBehavior INSTANCE = new CollectFluidsBehavior();
    public static final MapCodec<CollectFluidsBehavior> CODEC = MapCodec.unit(INSTANCE);

    private static final GameProfile FP = new GameProfile(UUID.nameUUIDFromBytes("FunctionalStorage-Pickup".getBytes(StandardCharsets.UTF_8)), "FunctionalStorage-Pickp");

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> dr, ItemStack upgradeStack, int upgradeSlot) {
        if (!(dr instanceof FluidDrawerTile drawer)) return;

        var direction = UpgradeItem.getDirection(upgradeStack);
        var fluidstate = level.getFluidState(pos.relative(direction));
        if (!fluidstate.isEmpty() && fluidstate.isSource()) {
            BlockState state = level.getBlockState(pos.relative(direction));
            Block block = state.getBlock();
            IFluidHandler targetFluidHandler = null;
            if (block instanceof BucketPickup) {
                targetFluidHandler = new BucketPickupHandlerWrapper(FakePlayerFactory.get((ServerLevel) level, FP), (BucketPickup) block, level, pos.relative(direction));
            }
            if (targetFluidHandler != null) {
                var drained = targetFluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                if (!drained.isEmpty()) {
                    for (int tankId = 0; tankId < drawer.getFluidHandler().getTanks(); tankId++) {
                        var fluidTank = drawer.fluidHandler.getTankList()[tankId];
                        var insertedAmount = fluidTank.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                        if (insertedAmount == drained.getAmount()) {
                            fluidTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                            if (!fluidstate.getType().canConvertToSource(fluidstate, level, pos.relative(direction)))
                                targetFluidHandler.drain(insertedAmount, IFluidHandler.FluidAction.EXECUTE);
                            drawer.fluidHandler.onChange();
                            break;
                        }
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
