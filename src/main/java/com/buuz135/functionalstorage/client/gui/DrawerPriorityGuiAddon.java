package com.buuz135.functionalstorage.client.gui;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.network.DrawerPriorityMessage;
import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.util.AssetUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
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
    public void drawBackgroundLayer(GuiGraphicsExtractor guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        if (editBox == null) {
            init(guiX, guiY);
        }
        editBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawForegroundLayer(GuiGraphicsExtractor guiGraphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.text(Minecraft.getInstance().font, Component.translatable("gui.functionalstorage.priority").withStyle(ChatFormatting.DARK_GRAY), getPosX(), getPosY() + 4, 0xffffffff, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return editBox != null && editBox.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return editBox != null && editBox.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return editBox != null && editBox.charTyped(event);
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
