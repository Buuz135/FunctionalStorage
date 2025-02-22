package com.buuz135.functionalstorage.compat.jade;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.block.tile.ArmoryCabinetTile;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.impl.ui.ElementHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;

public enum ArmoryComponentProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {
    INSTANCE;
    public static final String ID = FunctionalStorage.MOD_ID + "." + "armory";

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        var helper = ElementHelper.INSTANCE;
        if (blockAccessor.getBlockEntity() instanceof ArmoryCabinetTile) {
            if (blockAccessor.getServerData().contains(ID)) {
                int count = blockAccessor.getServerData().getInt(ID);
                iTooltip.add(helper.text(Component.nullToEmpty(count + " / " + FunctionalStorageConfig.ARMORY_CABINET_SIZE)).translate(new Vec2(4, (.86f * 18 - 10) / 2)));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag data, ServerPlayer serverPlayer, Level level, BlockEntity blockEntity, boolean b) {
        if (blockEntity instanceof ArmoryCabinetTile armory) {
            long count = armory.handler.stackList.stream().filter(stack -> !stack.isEmpty()).count();
            data.putLong(ID, count);
        }
    }

}