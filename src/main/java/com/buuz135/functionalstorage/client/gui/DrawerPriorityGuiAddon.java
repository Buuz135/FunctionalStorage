package com.buuz135.functionalstorage.client.gui;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.network.DrawerPriorityMessage;
import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.util.AssetUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class DrawerPriorityGuiAddon extends BasicScreenAddon {

    private final Supplier<Integer> priority;
    private final Supplier<BlockPos> pos;
    private EditBox editBox;

    public DrawerPriorityGuiAddon(int posX, int posY, Supplier<Integer> priority, Supplier<BlockPos> pos) {
        super(posX, posY);
        this.priority = priority;
        this.pos = pos;
    }

    @Override
    public void init(int guiX, int guiY) {
        this.editBox = new EditBox(Minecraft.getInstance().font, guiX + getPosX() + 1, guiY + getPosY() + 14, 50, 16, Component.translatable("gui.functionalstorage.priority"));
        this.editBox.setBordered(true);
        this.editBox.setMaxLength(9);
        this.editBox.setFilter(value -> value.chars().allMatch(Character::isDigit));
        this.editBox.setValue(String.valueOf(priority.get()));
        this.editBox.setResponder(this::onPriorityChanged);
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        if (editBox == null) {
            init(guiX, guiY);
        }
        editBox.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.functionalstorage.priority").withStyle(ChatFormatting.DARK_GRAY), getPosX(), getPosY() + 4, 0xffffffff, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return editBox != null && editBox.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return editBox != null && editBox.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return editBox != null && editBox.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isFocused() {
        return editBox != null && editBox.isFocused();
    }

    @Override
    public void setFocused(boolean focused) {
        if (editBox != null) {
            editBox.setFocused(focused);
        }
    }

    @Override
    public int getXSize() {
        return 110;
    }

    @Override
    public int getYSize() {
        return 30;
    }

    private void onPriorityChanged(String value) {
        FunctionalStorage.NETWORK.sendToServer(new DrawerPriorityMessage(pos.get(), value.isEmpty() ? 0 : Integer.parseInt(value)));
    }
}
