package com.buuz135.functionalstorage.compat;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.EnderDrawerBlock;
import com.buuz135.functionalstorage.block.tile.*;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.CompactingInventoryHandler;
import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.buuz135.functionalstorage.world.EnderSavedData;
import com.hrznstudio.titanium.annotation.plugin.FeaturePlugin;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.plugin.FeaturePluginInstance;
import com.hrznstudio.titanium.plugin.PluginPhase;
import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.elements.AbstractElementPanel;
import mcjty.theoneprobe.apiimpl.elements.ElementHorizontal;
import mcjty.theoneprobe.apiimpl.elements.ElementVertical;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import mcjty.theoneprobe.config.Config;
import mcjty.theoneprobe.network.NetworkTools;
import mcjty.theoneprobe.rendering.RenderHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.Color;
import java.util.Locale;
import java.util.function.Function;

@FeaturePlugin(value = "theoneprobe", type = FeaturePlugin.FeaturePluginType.MOD)
public class TOPPlugin implements FeaturePluginInstance{
    @Override
    public void execute(PluginPhase phase) {
        if (phase == PluginPhase.CONSTRUCTION){
            EventManager.mod(InterModEnqueueEvent.class).process(interModEnqueueEvent -> {
                InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> (Function<ITheOneProbe, Void>) iTheOneProbe -> {
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
                });
            }).subscribe();
        }
    }

    public static class FunctionalDrawerProvider implements IProbeInfoProvider{

        @Override
        public ResourceLocation getID() {
            return new ResourceLocation(FunctionalStorage.MOD_ID, "drawer");
        }

        @Override
        public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level, BlockState blockState, IProbeHitData iProbeHitData) {
            BlockEntity blockEntity = level.getBlockEntity(iProbeHitData.getPos());
            if (blockEntity instanceof ControllableDrawerTile && !(blockEntity instanceof DrawerControllerTile)){
                iProbeInfo.getElements().removeIf(iElement -> iElement instanceof ElementVertical);
                ElementVertical vertical = new ElementVertical();
                if (blockEntity instanceof DrawerTile){
                    ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().spacing(8).leftPadding(7).rightPadding(7));
                    abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    BigInventoryHandler handler = ((DrawerTile) blockEntity).getHandler();
                    for (int i = 0; i < handler.getStoredStacks().size(); i++) {
                        BigInventoryHandler.BigStack storedStack = handler.getStoredStacks().get(i);
                        if (storedStack.getAmount() > 0 || (handler.isLocked() && !storedStack.getStack().isEmpty())){
                            abstractElementPanel.element(new CustomElementItemStack(storedStack.getStack(), NumberUtils.getFormatedBigNumber(storedStack.getAmount()) + "/" + NumberUtils.getFormatedBigNumber(handler.getSlotLimit(i)), iProbeInfo.defaultItemStyle()));
                        }
                    }
                    if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                    vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
                }
                if (blockEntity instanceof EnderDrawerTile){
                    ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().spacing(8).leftPadding(7).rightPadding(7));
                    abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    EnderInventoryHandler savedData = EnderSavedData.getInstance(level).getFrequency(((EnderDrawerTile) blockEntity).getFrequency());
                    for (int i = 0; i < savedData.getStoredStacks().size(); i++) {
                        BigInventoryHandler.BigStack storedStack = savedData.getStoredStacks().get(i);
                        if (storedStack.getAmount() > 0 || (savedData.isLocked() && !storedStack.getStack().isEmpty())){
                            abstractElementPanel.element(new CustomElementItemStack(storedStack.getStack(), NumberUtils.getFormatedBigNumber(storedStack.getAmount()) + "/" + NumberUtils.getFormatedBigNumber(savedData.getSlotLimit(i)), iProbeInfo.defaultItemStyle()));
                        }
                    }
                    if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                    ElementVertical elementVertical = new ElementVertical(iProbeInfo.defaultLayoutStyle());
                    elementVertical.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    elementVertical.text(new TranslatableComponent("linkingtool.ender.frequency"));
                    vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
                    abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().leftPadding(4).topPadding(2).rightPadding(4));
                    for (ItemStack stack : EnderDrawerBlock.getFrequencyDisplay(((EnderDrawerTile) blockEntity).getFrequency())) {
                        abstractElementPanel.element(new CustomElementItemStack(stack, "", iProbeInfo.defaultItemStyle()));
                    }
                    elementVertical.element(abstractElementPanel);
                    vertical.element(elementVertical);
                    vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
                }
                if (blockEntity instanceof CompactingDrawerTile){
                    CompactingInventoryHandler inventoryHandler = ((CompactingDrawerTile) blockEntity).getHandler();
                    if (player.isShiftKeyDown() || probeMode == ProbeMode.EXTENDED){
                        ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().spacing(12).leftPadding(7).rightPadding(7));
                        abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                        abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(2).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(2).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(2)), iProbeInfo.defaultItemStyle()));
                        abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(1).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(1).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(1)), iProbeInfo.defaultItemStyle()));
                        abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(0).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(0).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(0)), iProbeInfo.defaultItemStyle()));
                        if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                    } else {
                        ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().spacing(8).leftPadding(7).rightPadding(7));
                        abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                        int amount = inventoryHandler.getAmount();
                        abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(2).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(2).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(2)), iProbeInfo.defaultItemStyle()));
                        amount -= inventoryHandler.getResultList().get(2).getNeeded() * inventoryHandler.getStackInSlot(2).getCount();
                        abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(1).getResult(), NumberUtils.getFormatedBigNumber((int) Math.floor(amount / inventoryHandler.getResultList().get(1).getNeeded())), iProbeInfo.defaultItemStyle()));
                        amount -= inventoryHandler.getResultList().get(1).getNeeded() *  Math.floor(amount / inventoryHandler.getResultList().get(1).getNeeded());
                        abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(0).getResult(), NumberUtils.getFormatedBigNumber((int) Math.floor(amount / inventoryHandler.getResultList().get(0).getNeeded())), iProbeInfo.defaultItemStyle()));
                        if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                    }
                    vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
                }
                if (player.isShiftKeyDown() || probeMode == ProbeMode.EXTENDED){
                    ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().topPadding(0).spacing(8).leftPadding(7).rightPadding(7));
                    abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    for (int i = 0; i < ((ControllableDrawerTile) blockEntity).getUtilityUpgrades().getSlots(); i++) {
                        ItemStack stack = ((ControllableDrawerTile) blockEntity).getUtilityUpgrades().getStackInSlot(i);
                        if (!stack.isEmpty()){
                            String extra = "";
                            if (stack.is(FunctionalStorage.PUSHING_UPGRADE.get()) || stack.is(FunctionalStorage.PULLING_UPGRADE.get()) || stack.is(FunctionalStorage.COLLECTOR_UPGRADE.get())){
                                extra = WordUtils.capitalize(UpgradeItem.getDirection(stack).name().toLowerCase(Locale.ROOT));
                                if (extra.equals("Up")){
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

    public static class CustomElementItemStack implements IElement {

        public static ResourceLocation RL = new ResourceLocation(FunctionalStorage.MOD_ID, "drawer_element");

        private final ItemStack itemStack;
        private final IItemStyle style;
        private final String display;

        public CustomElementItemStack(ItemStack itemStack, String display, IItemStyle style) {
            this.itemStack = itemStack;
            this.style = style;
            this.display = display;
        }

        public CustomElementItemStack(FriendlyByteBuf buf) {
            if (buf.readBoolean()) {
                this.itemStack = NetworkTools.readItemStack(buf);
            } else {
                this.itemStack = ItemStack.EMPTY;
            }

            this.style = (new ItemStyle()).width(buf.readInt()).height(buf.readInt());
            this.display = buf.readUtf();
        }

        public void render(PoseStack matrixStack, int x, int y) {
            ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
            if (!itemStack.isEmpty()) {
                int size = itemStack.getCount();

                if (!RenderHelper.renderItemStack(Minecraft.getInstance(), itemRender, itemStack, matrixStack, x + (style.getWidth() - 18) / 2, y + (style.getHeight() - 18) / 2, display)) {
                    Minecraft var10000 = Minecraft.getInstance();
                    ChatFormatting var10004 = ChatFormatting.RED;
                    RenderHelper.renderText(var10000, matrixStack, x, y, var10004 + "ERROR: " + itemStack.getHoverName());
                }
            }

        }

        public int getWidth() {
            return this.style.getWidth();
        }

        public int getHeight() {
            return this.style.getHeight();
        }

        public void toBytes(FriendlyByteBuf buf) {
            if (!this.itemStack.isEmpty()) {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, this.itemStack);
            } else {
                buf.writeBoolean(false);
            }

            buf.writeInt(this.style.getWidth());
            buf.writeInt(this.style.getHeight());
            buf.writeUtf(this.display);
        }

        public ResourceLocation getID() {
            return RL;
        }
    }
}
