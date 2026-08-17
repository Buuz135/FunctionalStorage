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
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record CollectFluidsBehavior() implements FunctionalUpgradeBehavior {
    public static final CollectFluidsBehavior INSTANCE = new CollectFluidsBehavior();
    public static final MapCodec<CollectFluidsBehavior> CODEC = MapCodec.unit(INSTANCE);

    private static final GameProfile FP = new GameProfile(UUID.nameUUIDFromBytes("FunctionalStorage-Pickup".getBytes(StandardCharsets.UTF_8)), "FunctionalStorage-Pickp");

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> dr, ItemStack upgradeStack, int upgradeSlot) {
        if (!(dr instanceof FluidDrawerTile drawer) || !(level instanceof ServerLevel serverLevel)) return;

        var direction = UpgradeItem.getDirection(upgradeStack);
        var fluidstate = level.getFluidState(pos.relative(direction));
        if (!fluidstate.isEmpty() && fluidstate.isSource()) {
            FluidUtil.tryPickupFluid(drawer.getFluidHandler(), FakePlayerFactory.get(serverLevel, FP), level, pos.relative(direction), direction.getOpposite(), null);
        }
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
