package com.buuz135.functionalstorage.compat.top;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.EnderDrawerBlock;
import com.buuz135.functionalstorage.block.tile.*;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.CompactingInventoryHandler;
import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.buuz135.functionalstorage.world.EnderSavedData;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.elements.ElementHorizontal;
import mcjty.theoneprobe.apiimpl.elements.ElementTank;
import mcjty.theoneprobe.apiimpl.elements.ElementVertical;
import mcjty.theoneprobe.apiimpl.styles.ProgressStyle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.text.WordUtils;
import var;
import java.awt.Color;
import java.util.Locale;
import java.util.function.Function;

public class FunctionalDrawerProvider implements IProbeInfoProvider {

    public static Function<ITheOneProbe, Void> REGISTER = iTheOneProbe -> {
        iTheOneProbe.registerProvider(new FunctionalDrawerProvider());
        iTheOneProbe.registerElementFactory(new IElementFactory() {
            @Override
            public IElement createElement(FriendlyByteBuf friendlyByteBuf) {
                return new CustomElementItemStack(friendlyByteBuf);
            }

            @Override
            public ResourceLocation getId() {
                return CustomElementItemStack.RL;
            }
        });
        return null;
    };

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(FunctionalStorage.MOD_ID, "drawer");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level, BlockState blockState, IProbeHitData iProbeHitData) {
        BlockEntity blockEntity = level.getBlockEntity(iProbeHitData.getPos());
        if (blockEntity instanceof ControllableDrawerTile && !(blockEntity instanceof StorageControllerTile) && !(blockEntity instanceof StorageControllerExtensionTile)) {
            iProbeInfo.getElements().removeIf(iElement -> iElement instanceof ElementVertical);
            ElementVertical vertical = new ElementVertical();
            if (blockEntity instanceof DrawerTile) {
                BigInventoryHandler handler = ((DrawerTile) blockEntity).getHandler();
                if (handler.getSlots() == 1 || player.isShiftKeyDown() || probeMode == ProbeMode.EXTENDED) {
                    ElementVertical elementVertical = new ElementVertical(iProbeInfo.defaultLayoutStyle().spacing(2).leftPadding(7).rightPadding(7));
                    elementVertical.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    for (int i = 0; i < handler.getStoredStacks().size(); i++) {
                        BigInventoryHandler.BigStack storedStack = handler.getStoredStacks().get(i);
                        if (storedStack.getAmount() > 0 || (handler.isLocked() && !storedStack.getStack().isEmpty())) {
                            elementVertical.element(new CustomElementItemStack(storedStack.getStack(), NumberUtils.getFormatedBigNumber(handler.getStackInSlot(i).getCount()) + "/" + NumberUtils.getFormatedBigNumber(handler.getSlotLimit(i)), iProbeInfo.defaultItemStyle(), true));
                        }
                    }
                    if (elementVertical.getElements().size() > 0) vertical.element(elementVertical);
                    vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
                } else {
                    ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().spacing(8).leftPadding(7).rightPadding(7));
                    abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    for (int i = 0; i < handler.getStoredStacks().size(); i++) {
                        BigInventoryHandler.BigStack storedStack = handler.getStoredStacks().get(i);
                        if (storedStack.getAmount() > 0 || (handler.isLocked() && !storedStack.getStack().isEmpty())) {
                            abstractElementPanel.element(new CustomElementItemStack(storedStack.getStack(), NumberUtils.getFormatedBigNumber(handler.getStackInSlot(i).getCount()) + "/" + NumberUtils.getFormatedBigNumber(handler.getSlotLimit(i)), iProbeInfo.defaultItemStyle()));
                        }
                    }
                    if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                    vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
                }
            }
            if (blockEntity instanceof EnderDrawerTile) {
                ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().spacing(8).leftPadding(7).rightPadding(7));
                abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                EnderInventoryHandler savedData = EnderSavedData.getInstance(level).getFrequency(((EnderDrawerTile) blockEntity).getFrequency());
                for (int i = 0; i < savedData.getStoredStacks().size(); i++) {
                    BigInventoryHandler.BigStack storedStack = savedData.getStoredStacks().get(i);
                    if (storedStack.getAmount() > 0 || (savedData.isLocked() && !storedStack.getStack().isEmpty())) {
                        abstractElementPanel.element(new CustomElementItemStack(storedStack.getStack(), NumberUtils.getFormatedBigNumber(storedStack.getAmount()) + "/" + NumberUtils.getFormatedBigNumber(savedData.getSlotLimit(i)), iProbeInfo.defaultItemStyle(), player.isShiftKeyDown() || probeMode == ProbeMode.EXTENDED));
                    }
                }
                if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                ElementVertical elementVertical = new ElementVertical(iProbeInfo.defaultLayoutStyle());
                elementVertical.getStyle().borderColor(Color.CYAN.darker().getRGB());
                elementVertical.text(Component.translatable("linkingtool.ender.frequency"));
                vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
                abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().leftPadding(4).topPadding(2).rightPadding(4));
                for (ItemStack stack : EnderDrawerBlock.getFrequencyDisplay(((EnderDrawerTile) blockEntity).getFrequency())) {
                    abstractElementPanel.element(new CustomElementItemStack(stack, "", iProbeInfo.defaultItemStyle()));
                }
                elementVertical.element(abstractElementPanel);
                vertical.element(elementVertical);
                vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
            }
            if (blockEntity instanceof CompactingDrawerTile) {
                CompactingInventoryHandler inventoryHandler = ((CompactingDrawerTile) blockEntity).getHandler();
                if (player.isShiftKeyDown() || probeMode == ProbeMode.EXTENDED || inventoryHandler.isCreative()) {
                    ElementVertical abstractElementPanel = new ElementVertical(iProbeInfo.defaultLayoutStyle().spacing(2).leftPadding(7).rightPadding(7));
                    abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(2).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(2).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(2)), iProbeInfo.defaultItemStyle(), true));
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(1).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(1).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(1)), iProbeInfo.defaultItemStyle(), true));
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(0).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(0).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(0)), iProbeInfo.defaultItemStyle(), true));
                    if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                } else {
                    ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().spacing(8).leftPadding(7).rightPadding(7));
                    abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    int amount = inventoryHandler.getAmount();
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(2).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(2).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(2)), iProbeInfo.defaultItemStyle()));
                    amount -= inventoryHandler.getResultList().get(2).getNeeded() * inventoryHandler.getStackInSlot(2).getCount();
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(1).getResult(), NumberUtils.getFormatedBigNumber((int) Math.floor(amount / inventoryHandler.getResultList().get(1).getNeeded())), iProbeInfo.defaultItemStyle()));
                    amount -= inventoryHandler.getResultList().get(1).getNeeded() * Math.floor(amount / inventoryHandler.getResultList().get(1).getNeeded());
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(0).getResult(), NumberUtils.getFormatedBigNumber((int) Math.floor(amount / inventoryHandler.getResultList().get(0).getNeeded())), iProbeInfo.defaultItemStyle()));
                    if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                }
                vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
            }
            if (blockEntity instanceof SimpleCompactingDrawerTile) {
                CompactingInventoryHandler inventoryHandler = ((SimpleCompactingDrawerTile) blockEntity).getHandler();
                if (player.isShiftKeyDown() || probeMode == ProbeMode.EXTENDED || inventoryHandler.isCreative()) {
                    ElementVertical abstractElementPanel = new ElementVertical(iProbeInfo.defaultLayoutStyle().spacing(2).leftPadding(7).rightPadding(7));
                    abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(1).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(1).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(1)), iProbeInfo.defaultItemStyle(), true));
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(0).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(0).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(0)), iProbeInfo.defaultItemStyle(), true));
                    if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                } else {
                    ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().spacing(8).leftPadding(7).rightPadding(7));
                    abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    int amount = inventoryHandler.getAmount();
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(1).getResult(), NumberUtils.getFormatedBigNumber((int) Math.floor(amount / inventoryHandler.getResultList().get(1).getNeeded())), iProbeInfo.defaultItemStyle()));
                    amount -= inventoryHandler.getResultList().get(1).getNeeded() * Math.floor(amount / inventoryHandler.getResultList().get(1).getNeeded());
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(0).getResult(), NumberUtils.getFormatedBigNumber((int) Math.floor(amount / inventoryHandler.getResultList().get(0).getNeeded())), iProbeInfo.defaultItemStyle()));
                    if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                }
                vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
            }
            if (blockEntity instanceof FluidDrawerTile fluidDrawerTile && (player.isShiftKeyDown() || probeMode == ProbeMode.EXTENDED)) {
                ElementVertical tanksVertical = new ElementVertical(iProbeInfo.defaultLayoutStyle().spacing(2));
                for (int i = 0; i < fluidDrawerTile.getFluidHandler().getTanks(); i++) {
                    var tankReference = TankReference.createTank(fluidDrawerTile.getFluidHandler().getTankList()[i]);
                    tanksVertical.element(new ElementTank(tankReference, new ProgressStyle().numberFormat(NumberFormat.COMPACT)));
                }
                iProbeInfo.element(tanksVertical);
            }
            if (player.isShiftKeyDown() || probeMode == ProbeMode.EXTENDED) {
                ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().topPadding(0).spacing(8).leftPadding(7).rightPadding(7));
                abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                for (int i = 0; i < ((ControllableDrawerTile) blockEntity).getUtilityUpgrades().getSlots(); i++) {
                    ItemStack stack = ((ControllableDrawerTile) blockEntity).getUtilityUpgrades().getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        String extra = "";
                        if (stack.is(FunctionalStorage.PUSHING_UPGRADE.get()) || stack.is(FunctionalStorage.PULLING_UPGRADE.get()) || stack.is(FunctionalStorage.COLLECTOR_UPGRADE.get())) {
                            extra = WordUtils.capitalize(UpgradeItem.getDirection(stack).name().toLowerCase(Locale.ROOT));
                            if (extra.equals("Up")) {
                                extra = "   " + extra;
                            }
                        }
                        abstractElementPanel.element(new CustomElementItemStack(stack, extra, iProbeInfo.defaultItemStyle()));
                    }
                }
                if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
            }
            iProbeInfo.element(vertical);
        }

    }
}