package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.Drawer;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Optional;

public class ClientSetup {
    public static void init() {
        EventManager.forge(ItemTooltipEvent.class).filter(
                event -> UpgradeItem.isDirectionUpgrade(event.getItemStack().getItem()) && event.getItemStack().has(FSAttachments.DIRECTION)
        ).filter(event -> Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof BasicAddonScreen bcs && (bcs.getMenu().getObject() instanceof ItemControllableDrawerTile<?> || bcs.getMenu().getObject() instanceof FluidDrawerTile))
            .process(event -> {
                var sc = (BasicAddonScreen) Minecraft.getInstance().screen;
                var blockstate = ((ControllableDrawerTile<?>) sc.getMenu().getObject()).getBlockState();
                if (blockstate.hasProperty(Drawer.FACING_HORIZONTAL_CUSTOM)) {
                    var direction = blockstate.getValue(Drawer.FACING_HORIZONTAL_CUSTOM);
                    if (blockstate.hasProperty(Drawer.FACING_ALL) && (direction == Direction.UP || direction == Direction.DOWN)) {
                        var subdirection = blockstate.getValue(RotatableBlock.FACING_ALL);
                        event.getToolTip().add(3, Component.translatable("drawer_upgrade.functionalstorage.relative_direction", UpgradeItem.getRelativeDirectionVertical(direction, subdirection, UpgradeItem.getDirection(event.getItemStack()))).withStyle(ChatFormatting.YELLOW));
                    } else {
                        event.getToolTip().add(3, Component.translatable("drawer_upgrade.functionalstorage.relative_direction", UpgradeItem.getRelativeDirection(direction, UpgradeItem.getDirection(event.getItemStack()))).withStyle(ChatFormatting.YELLOW));
                    }
                } else if (blockstate.hasProperty(Drawer.FACING_HORIZONTAL)) {
                    var direction = blockstate.getValue(Drawer.FACING_HORIZONTAL);
                    event.getToolTip().add(3, Component.translatable("drawer_upgrade.functionalstorage.relative_direction", UpgradeItem.getRelativeDirection(direction, UpgradeItem.getDirection(event.getItemStack()))).withStyle(ChatFormatting.YELLOW));
                }
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

        EventManager.forge(RenderGuiEvent.Post.class).process(event -> renderFluidDrawerHint(event.getGuiGraphics())).subscribe();
    }

    private static void renderFluidDrawerHint(GuiGraphics guiGraphics) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.screen != null || minecraft.player == null || minecraft.level == null) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        if (!(minecraft.level.getBlockEntity(blockHitResult.getBlockPos()) instanceof FluidDrawerTile)) {
            return;
        }
        if (!isFluidContainer(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND)) && !isFluidContainer(minecraft.player.getItemInHand(InteractionHand.OFF_HAND))) {
            return;
        }

        List<Component> tooltip = List.of(
                Component.translatable("gui.functionalstorage.fluid_drawer_hint").withStyle(ChatFormatting.GOLD),
                Component.translatable("gui.functionalstorage.fluid_drawer_hint.empty", minecraft.options.keyUse.getTranslatedKeyMessage()).withStyle(ChatFormatting.GRAY),
                Component.translatable("gui.functionalstorage.fluid_drawer_hint.fill", minecraft.options.keyAttack.getTranslatedKeyMessage()).withStyle(ChatFormatting.GRAY)
        );
        guiGraphics.renderTooltip(minecraft.font, tooltip, Optional.empty(), guiGraphics.guiWidth() / 2 + 12, guiGraphics.guiHeight() / 2 + 12);
    }

    private static boolean isFluidContainer(ItemStack stack) {
        return !stack.isEmpty() && stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
    }
}
