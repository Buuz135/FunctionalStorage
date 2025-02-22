package com.buuz135.functionalstorage.compat.top;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.block.tile.ArmoryCabinetTile;
import mcjty.theoneprobe.api.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public class FunctionalArmoryProvider implements IProbeInfoProvider {

    public static final Function<ITheOneProbe, Void> REGISTER = iTheOneProbe -> {
        iTheOneProbe.registerProvider(new FunctionalArmoryProvider());
        return null;
    };

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(FunctionalStorage.MOD_ID, "armory");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level, BlockState blockState, IProbeHitData iProbeHitData) {
        BlockEntity blockEntity = level.getBlockEntity(iProbeHitData.getPos());
        if (blockEntity instanceof ArmoryCabinetTile armory) {
            long count = armory.handler.stackList.stream().filter(stack -> !stack.isEmpty()).count();
            iProbeInfo.vertical().mcText(Component.nullToEmpty(count + " / " + FunctionalStorageConfig.ARMORY_CABINET_SIZE));
        }
    }
}