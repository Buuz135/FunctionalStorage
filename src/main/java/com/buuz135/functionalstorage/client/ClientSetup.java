package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.client.screen.container.BasicAddonScreen;
import com.hrznstudio.titanium.event.handler.EventManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class ClientSetup {
    public static void init() {
        EventManager.forge(ItemTooltipEvent.class).filter(
                event -> UpgradeItem.isDirectionUpgrade(event.getItemStack().getItem()) && event.getItemStack().has(FSAttachments.DIRECTION)
        ).filter(event -> Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof BasicAddonScreen bcs && (bcs.getMenu().getObject() instanceof ItemControllableDrawerTile<?> || bcs.getMenu().getObject() instanceof FluidDrawerTile))
            .process(event -> {
                var sc = (BasicAddonScreen) Minecraft.getInstance().screen;
                var direction = ((ControllableDrawerTile<?>) sc.getMenu().getObject()).getBlockState().getValue(RotatableBlock.FACING_HORIZONTAL);
                event.getToolTip().add(3, Component.translatable("drawer_upgrade.functionalstorage.relative_direction", UpgradeItem.getRelativeDirection(direction, UpgradeItem.getDirection(event.getItemStack()))).withStyle(ChatFormatting.YELLOW));
            }).subscribe();

        EventManager.forge(ItemTooltipEvent.class).process(event -> {
            var stack = event.getItemStack();
            var tooltip = event.getToolTip();

            var item = stack.get(FSAttachments.ITEM_STORAGE_MODIFIER);
            if (item != null) {
                tooltip.add(item.getTooltip(Component.translatable("storageupgrade.obj.item_storage")).copy().withStyle(ChatFormatting.GRAY));
            }
            var fluid = stack.get(FSAttachments.FLUID_STORAGE_MODIFIER);
            if (fluid != null) {
                tooltip.add(fluid.getTooltip(Component.translatable("storageupgrade.obj.fluid_storage")).copy().withStyle(ChatFormatting.GRAY));
            }
            var range = stack.get(FSAttachments.CONTROLLER_RANGE_MODIFIER);
            if (range != null) {
                tooltip.add(range.getTooltip(Component.translatable("storageupgrade.obj.controller_range")).copy().withStyle(ChatFormatting.GRAY));
            }
            var functional = stack.get(FSAttachments.FUNCTIONAL_BEHAVIOR);
            if (functional != null && stack.getItem() != FunctionalStorage.PUSHING_UPGRADE.get() && stack.getItem() != FunctionalStorage.PULLING_UPGRADE.get()) {
                tooltip.addAll(functional.getTooltip());
            }
        }).subscribe();
    }
}
