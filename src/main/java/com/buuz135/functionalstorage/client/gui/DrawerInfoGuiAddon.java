package com.buuz135.functionalstorage.client.gui;

import com.buuz135.functionalstorage.util.NumberUtils;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import var;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

public class DrawerInfoGuiAddon extends BasicScreenAddon {

    private final ResourceLocation gui;
    private final int slotAmount;
    private final Function<Integer, Pair<Integer, Integer>> slotPosition;
    private final Function<Integer, ItemStack> slotStack;
    private final Function<Integer, Integer> slotMaxAmount;

    public DrawerInfoGuiAddon(int posX, int posY, ResourceLocation gui, int slotAmount, Function<Integer, Pair<Integer, Integer>> slotPosition, Function<Integer, ItemStack> slotStack, Function<Integer, Integer> slotMaxAmount) {
        super(posX, posY);
        this.gui = gui;
        this.slotAmount = slotAmount;
        this.slotPosition = slotPosition;
        this.slotStack = slotStack;
        this.slotMaxAmount = slotMaxAmount;
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
        var size = 16 * 2 + 16;
        guiGraphics.blit(gui, guiX + getPosX(), guiY + getPosY(), 0, 0, size, size, size, size);
        for (var i = 0; i < slotAmount; i++) {
            var itemStack = slotStack.apply(i);
            if (!itemStack.isEmpty()) {
                var x = guiX + slotPosition.apply(i).getLeft() + getPosX();
                var y = guiY + slotPosition.apply(i).getRight() + getPosY();
                guiGraphics.renderItem(slotStack.apply(i), x, y);
                var amount = NumberUtils.getFormatedBigNumber(itemStack.getCount()) + "/" + NumberUtils.getFormatedBigNumber(slotMaxAmount.apply(i));
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
            var x = slotPosition.apply(i).getLeft() + getPosX() + guiX;
            var y = slotPosition.apply(i).getRight() + getPosY() + guiY;
            if (mouseX > x && mouseX < x + 18 && mouseY > y && mouseY < y + 18) {
                x = slotPosition.apply(i).getLeft() + getPosX();
                y = slotPosition.apply(i).getRight() + getPosY();
                guiGraphics.pose().translate(0, 0, 200);
                guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, -2130706433);
                guiGraphics.pose().translate(0, 0, -200);
                var componentList = new ArrayList<Component>();
                var over = slotStack.apply(i);
                if (over.isEmpty()) {
                    componentList.add(Component.translatable("gui.functionalstorage.item").withStyle(ChatFormatting.GOLD).append(Component.literal("Empty").withStyle(ChatFormatting.WHITE)));
                } else {
                    componentList.add(Component.translatable("gui.functionalstorage.item").withStyle(ChatFormatting.GOLD).append(over.getHoverName().copy().withStyle(ChatFormatting.WHITE)));
                    var amount = NumberUtils.getFormatedBigNumber(over.getCount()) + "/" + NumberUtils.getFormatedBigNumber(slotMaxAmount.apply(i));
                    componentList.add(Component.translatable("gui.functionalstorage.amount").withStyle(ChatFormatting.GOLD).append(Component.literal(amount).withStyle(ChatFormatting.WHITE)));
                }
                componentList.add(Component.translatable("gui.functionalstorage.slot").withStyle(ChatFormatting.GOLD).append(Component.literal(i + "").withStyle(ChatFormatting.WHITE)));
                guiGraphics.renderTooltip(Minecraft.getInstance().font, componentList, Optional.empty(), mouseX - guiX, mouseY - guiY);
            }
        }
    }

}
