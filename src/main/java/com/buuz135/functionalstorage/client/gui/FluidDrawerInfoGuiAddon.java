package com.buuz135.functionalstorage.client.gui;

import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class FluidDrawerInfoGuiAddon extends BasicScreenAddon {

    private final Identifier gui;
    private final int slotAmount;
    private final Function<Integer, Pair<Integer, Integer>> slotPosition;
    private final Supplier<BigFluidHandler> fluidHandlerSupplier;
    private final Function<Integer, Integer> slotMaxAmount;

    public FluidDrawerInfoGuiAddon(int posX, int posY, Identifier gui, int slotAmount, Function<Integer, Pair<Integer, Integer>> slotPosition, Supplier<BigFluidHandler> fluidHandlerSupplier, Function<Integer, Integer> slotMaxAmount) {
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
    public void drawBackgroundLayer(GuiGraphicsExtractor guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        for (var i = 0; i < slotAmount; i++) {
            var fluidStack = fluidHandlerSupplier.get().getFluidInTank(i);
            if (fluidStack.isEmpty() && fluidHandlerSupplier.get().isDrawerLocked()) {
                fluidStack = fluidHandlerSupplier.get().getFilterStack()[i];
            }
            if (!fluidStack.isEmpty()) {
                renderFluid(guiGraphics, screen, guiX, guiY, fluidStack, i, slotAmount);
            }
        }
        var size = 16 * 2 + 16;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, gui, guiX + getPosX(), guiY + getPosY(), 0, 0, size, size, size, size);
        for (var i = 0; i < slotAmount; i++) {
            var fluidStack = fluidHandlerSupplier.get().getFluidInTank(i);
            if (!fluidStack.isEmpty()) {
                var x = guiX + slotPosition.apply(i).getLeft() + getPosX();
                var y = guiY + slotPosition.apply(i).getRight() + getPosY();
                var amount = NumberUtils.getFormatedFluidBigNumber(fluidStack.getAmount()) + "/" + NumberUtils.getFormatedFluidBigNumber(slotMaxAmount.apply(i));
                var scale = 0.5f;
                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().scale(scale, scale);
                guiGraphics.text(Minecraft.getInstance().font, amount, (int) ((x + 17 - Minecraft.getInstance().font.width(amount) / 2) * (1 / scale)), (int) ((y + 12) * (1 / scale)), 0xFFFFFFFF, true);
                guiGraphics.pose().popMatrix();
            }
        }
    }

    @Override
    public void drawForegroundLayer(GuiGraphicsExtractor guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        for (var i = 0; i < slotAmount; i++) {
            var rect = getSizeForHoverSlots(i, slotAmount);
            var x = rect.getX() + getPosX() + guiX;
            var y = rect.getY() + getPosY() + guiY;
            if (mouseX > x && mouseX < x + rect.getWidth() && mouseY > y && mouseY < y + rect.getHeight()) {
                x = getPosX() + rect.getX();
                y = getPosY() + rect.getY();
                guiGraphics.fill(x, y, x + rect.getWidth(), y + rect.getHeight(), -2130706433);
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
                guiGraphics.setTooltipForNextFrame(Minecraft.getInstance().font, componentList, Optional.empty(), mouseX - guiX, mouseY - guiY);
            }
        }
    }

    public void renderFluid(GuiGraphicsExtractor guiGraphics, Screen screen, int guiX, int guiY, FluidStack fluidStack, int slot, int slotAmount) {
        var fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidStack.getFluid().defaultFluidState());
        TextureAtlasSprite sprite = fluidModel.stillMaterial().sprite();
        if (sprite != null) {
                    Color color = new Color(fluidModel.tintSource().color(fluidStack.getFluid().defaultFluidState().createLegacyBlock()), true);
                    var rect = getSizeForSlots(slot, slotAmount);
                    int tint = color.getRGB();
                    for (int x = 0; x < rect.getWidth(); x += 16) {
                        for (int y = 0; y < rect.getHeight(); y += 16) {
                            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getPosX() + guiX + rect.getX() + x,
                                    this.getPosY() + guiY + rect.getY() + y,
                                    Math.min(16, rect.getWidth() - x),
                                    Math.min(16, rect.getHeight() - y),
                                    tint);
                        }
                    }
        }
    }


}
