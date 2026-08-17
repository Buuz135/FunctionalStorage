package com.buuz135.functionalstorage.compat.jade;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.block.tile.ArmoryCabinetTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

public enum ArmoryComponentProvider implements IBlockComponentProvider {
    INSTANCE;
    public static final Identifier ID = com.buuz135.functionalstorage.util.Utils.resourceLocation(FunctionalStorage.MOD_ID, "armory");

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getBlockEntity() instanceof ArmoryCabinetTile) {
            if (blockAccessor.getServerData().contains(this.getUid().toLanguageKey())) {
                int count = blockAccessor.getServerData().getInt(this.getUid().toLanguageKey()).orElse(0);
                iTooltip.add(JadeUI.text(Component.literal(count + " / " + FunctionalStorageConfig.ARMORY_CABINET_SIZE)).offset(4, (int) ((.86f * 18 - 10) / 2)));
            }
        }
    }

    @Override
    public int getDefaultPriority() {
        return 999;
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}
