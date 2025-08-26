package com.buuz135.functionalstorage.client.gui;

import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class FluidDrawerInfoGuiAddon extends BasicScreenAddon {

    private final ResourceLocation gui;
    private final int slotAmount;
    private final Function<Integer, Pair<Integer, Integer>> slotPosition;
    private final Supplier<BigFluidHandler> fluidHandlerSupplier;
    private final Function<Integer, Integer> slotMaxAmount;

    public FluidDrawerInfoGuiAddon(int posX, int posY, ResourceLocation gui, int slotAmount, Function<Integer, Pair<Integer, Integer>> slotPosition, Supplier<BigFluidHandler> fluidHandlerSupplier, Function<Integer, Integer> slotMaxAmount) {
        super(posX, posY);
        this.gui = gui;
        this.slotAmount = slotAmount;
        this.slotPosition = slotPosition;
        this.fluidHandlerSupplier = fluidHandlerSupplier;
        this.slotMaxAmount = slotMaxAmount;
    }

    public static Rect2i getSizeForSlots(int currentSlot, int slotAmount) {
        if (slotAmount == 1) {
            return new Rect2i(9, 9, 30, 30);
        }
        if (slotAmount == 2) {
            if (currentSlot == 0) return new Rect2i(0, 30, 48, 13);
            if (currentSlot == 1) return new Rect2i(0, 6, 48, 13);
        }
        if (slotAmount == 4) {
            if (currentSlot == 0) return new Rect2i(30, 30, 16, 16);
            if (currentSlot == 1) return new Rect2i(2, 30, 16, 16);
            if (currentSlot == 2) return new Rect2i(30, 2, 16, 16);
            if (currentSlot == 3) return new Rect2i(2, 2, 16, 16);
        }
        return new Rect2i(0, 0, 0, 0);
    }

    public static Rect2i getSizeForHoverSlots(int currentSlot, int slotAmount) {
        if (slotAmount == 1) {
            return new Rect2i(9, 9, 30, 30);
        }
        if (slotAmount == 2) {
            if (currentSlot == 0) return new Rect2i(6, 30, 36, 12);
            if (currentSlot == 1) return new Rect2i(6, 6, 36, 12);
        }
        if (slotAmount == 4) {
            if (currentSlot == 0) return new Rect2i(30, 30, 12, 12);
            if (currentSlot == 1) return new Rect2i(6, 30, 12, 12);
            if (currentSlot == 2) return new Rect2i(30, 6, 12, 12);
            if (currentSlot == 3) return new Rect2i(6, 6, 12, 12);
        }
        return new Rect2i(0, 0, 0, 0);
    }

    @Override
    public int getXSize() {
        return 0;
    }

    @Override
    public int getYSize() {
        return 0;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        for (var i = 0; i < slotAmount; i++) {
            var fluidStack = fluidHandlerSupplier.get().getFluidInTank(i);
            if (fluidStack.isEmpty() && fluidHandlerSupplier.get().isDrawerLocked()) {
                fluidStack = fluidHandlerSupplier.get().getFilterStack()[i];
            }
            if (!fluidStack.isEmpty()) {
                renderFluid(guiGraphics, screen, guiX, guiY, fluidStack, i, slotAmount);
            }
        }
        RenderSystem.setShaderTexture(0, gui);
        var size = 16 * 2 + 16;
        guiGraphics.blit(gui,guiX + getPosX(), guiY + getPosY(), 0, 0, size, size, size, size);
        for (var i = 0; i < slotAmount; i++) {
            var fluidStack = fluidHandlerSupplier.get().getFluidInTank(i);
            if (!fluidStack.isEmpty()) {
                var x = guiX + slotPosition.apply(i).getLeft() + getPosX();
                var y = guiY + slotPosition.apply(i).getRight() + getPosY();
                var amount = NumberUtils.getFormatedFluidBigNumber(fluidStack.getAmount()) + "/" + NumberUtils.getFormatedFluidBigNumber(slotMaxAmount.apply(i));
                var scale = 0.5f;
                guiGraphics.pose().translate(0, 0, 200);
                guiGraphics.pose().scale(scale, scale, scale);
                guiGraphics.drawString(Minecraft.getInstance().font, amount, (x + 17 - Minecraft.getInstance().font.width(amount) / 2) * (1 / scale), (y + 12) * (1 / scale), 0xFFFFFF, true);
                guiGraphics.pose().scale(1 / scale, 1 / scale, 1 / scale);
                guiGraphics.pose().translate(0, 0, -200);
            }
        }
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        for (var i = 0; i < slotAmount; i++) {
            var rect = getSizeForHoverSlots(i, slotAmount);
            var x = rect.getX() + getPosX() + guiX;
            var y = rect.getY() + getPosY() + guiY;
            if (mouseX > x && mouseX < x + rect.getWidth() && mouseY > y && mouseY < y + rect.getHeight()) {
                x = getPosX() + rect.getX();
                y = getPosY() + rect.getY();
                guiGraphics.pose().translate(0, 0, 200);
                guiGraphics.fill(x, y, x + rect.getWidth(), y + rect.getHeight(), -2130706433);
                guiGraphics.pose().translate(0, 0, -200);
                var componentList = new ArrayList<Component>();
                var over = fluidHandlerSupplier.get().getFluidInTank(i);
                if (over.isEmpty() && fluidHandlerSupplier.get().isDrawerLocked()) {
                    over = fluidHandlerSupplier.get().getFilterStack()[i];
                }
                if (over.isEmpty()) {
                    componentList.add(Component.translatable("gui.functionalstorage.fluid").withStyle(ChatFormatting.GOLD).append(Component.translatable("gui.functionalstorage.empty").withStyle(ChatFormatting.WHITE)));
                } else {
                    componentList.add(Component.translatable("gui.functionalstorage.fluid").withStyle(ChatFormatting.GOLD).append(over.getHoverName().copy().withStyle(ChatFormatting.WHITE)));
                    var amount = NumberUtils.getFormattedFluid(fluidHandlerSupplier.get().getFluidInTank(i).getAmount()) + "/" + NumberUtils.getFormattedFluid(slotMaxAmount.apply(i));
                    componentList.add(Component.translatable("gui.functionalstorage.amount").withStyle(ChatFormatting.GOLD).append(Component.literal(amount).withStyle(ChatFormatting.WHITE)));
                }
                componentList.add(Component.translatable("gui.functionalstorage.slot").withStyle(ChatFormatting.GOLD).append(Component.literal(i + "").withStyle(ChatFormatting.WHITE)));
                guiGraphics.renderTooltip(Minecraft.getInstance().font, componentList, Optional.empty(), mouseX - guiX, mouseY - guiY);
            }
        }
    }

    public void renderFluid(GuiGraphics guiGraphics, Screen screen, int guiX, int guiY, FluidStack fluidStack, int slot, int slotAmount) {
        IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation flowing = renderProperties.getStillTexture(fluidStack);
        if (flowing != null) {
            AbstractTexture texture = screen.getMinecraft().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS); //getAtlasSprite
            if (texture instanceof TextureAtlas) {
                TextureAtlasSprite sprite = ((TextureAtlas) texture).getSprite(flowing);
                if (sprite != null) {
                    //RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
                    Color color = new Color(renderProperties.getTintColor(fluidStack));
                    var rect = getSizeForSlots(slot, slotAmount);
                    RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
                    RenderSystem.enableBlend();
                    for (int x = 0; x < rect.getWidth(); x += 16) {
                        for (int y = 0; y < rect.getHeight(); y += 16) {
                            guiGraphics.blit( this.getPosX() + guiX + rect.getX() + x,
                                    this.getPosY() + guiY + rect.getY() + y,
                                    0,
                                    Math.min(16, rect.getWidth() - x),
                                    Math.min(16, rect.getHeight() - y),
                                    sprite);
                        }
                    }
                    RenderSystem.disableBlend();
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                }
            }
        }
    }


}
