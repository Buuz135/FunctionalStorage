package com.buuz135.functionalstorage.compat.jade;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.block.tile.ArmoryCabinetTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public enum ArmoryComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockEntity> {
    INSTANCE;
    public static final ResourceLocation ID = new ResourceLocation(FunctionalStorage.MOD_ID, "armory");

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        var helper = IElementHelper.get();
        if (blockAccessor.getBlockEntity() instanceof ArmoryCabinetTile) {
            if (blockAccessor.getServerData().contains(this.getUid().toLanguageKey())) {
                int count = blockAccessor.getServerData().getInt(this.getUid().toLanguageKey());
                iTooltip.add(helper.text(Component.literal(count + " / " + FunctionalStorageConfig.ARMORY_CABINET_SIZE)).translate(new Vec2(4, (.86f * 18 - 10) / 2)));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag data, ServerPlayer serverPlayer, Level level, BlockEntity blockEntity, boolean b) {
        if (blockEntity instanceof ArmoryCabinetTile armory) {
            long count = armory.handler.stackList.stream().filter(stack -> !stack.isEmpty()).count();
            data.putLong(this.getUid().toLanguageKey(), count);
        }
    }

    @Override
    public int getDefaultPriority() {
        return 999;
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }

}