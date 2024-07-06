package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.client.screen.container.BasicAddonScreen;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.util.FacingUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class ClientSetup {
    public static void init() {
        EventManager.forge(ItemTooltipEvent.class).filter(
                event -> UpgradeItem.isDirectionUpgrade(event.getItemStack().getItem()) && event.getItemStack().has(FSAttachments.DIRECTION)
        ).filter(event -> Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof BasicAddonScreen bcs && bcs.getMenu().getObject() instanceof ItemControllableDrawerTile<?>)
            .process(event -> {
                var sc = (BasicAddonScreen) Minecraft.getInstance().screen;
                var direction = ((ItemControllableDrawerTile<?>) sc.getMenu().getObject()).getBlockState().getValue(RotatableBlock.FACING_HORIZONTAL);
                event.getToolTip().add(3, Component.translatable("drawer_upgrade.functionalstorage.relative_direction", UpgradeItem.getRelativeDirection(direction, UpgradeItem.getDirection(event.getItemStack()))).withStyle(ChatFormatting.YELLOW));
            }).subscribe();
    }
}
